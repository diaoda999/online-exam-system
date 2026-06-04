package com.exam.interceptor;

import com.exam.common.constant.RedisKeyConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.exam.common.result.Result;
import com.exam.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 考试 Token 校验拦截器
 * 通过 Redis 校验考试 Token 的合法性
 * 适用于交卷、保存进度等需要验证考试会话的接口
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamTokenInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String EXAM_TOKEN_HEADER = "X-Exam-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从 Header 提取考试 Token
        String examToken = request.getHeader(EXAM_TOKEN_HEADER);
        if (examToken == null || examToken.isBlank()) {
            writeForbiddenResponse(response, "缺少考试Token");
            return false;
        }

        // 从 request attribute 获取 userId（由 AuthInterceptor 设置）
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            writeForbiddenResponse(response, "用户信息缺失");
            return false;
        }

        // 通过 Redis 验证 Token：使用 "exam:token:value:" + token 格式查找
        String tokenKey = "exam:token:value:" + examToken;
        try {
            Object tokenValue = redisTemplate.opsForValue().get(tokenKey);
            if (tokenValue == null) {
                writeForbiddenResponse(response, "考试Token无效或已过期");
                return false;
            }

            // tokenValue 格式: "examId:userId"
            String[] parts = tokenValue.toString().split(":");
            if (parts.length != 2) {
                writeForbiddenResponse(response, "考试Token数据异常");
                return false;
            }

            Long examId = Long.parseLong(parts[0]);
            Long tokenUserId = Long.parseLong(parts[1]);

            // 校验 userId 一致性，防止 Token 被他人使用
            if (!tokenUserId.equals(userId)) {
                writeForbiddenResponse(response, "考试Token与当前用户不匹配");
                return false;
            }

            // 将考试信息放入 request attribute，供后续 Controller 使用
            request.setAttribute("examId", examId);
            request.setAttribute("examToken", examToken);

            log.debug("考试Token校验通过: examId={}, userId={}", examId, userId);
            return true;

        } catch (NumberFormatException e) {
            writeForbiddenResponse(response, "考试Token数据格式异常");
            return false;
        } catch (Exception e) {
            log.error("Redis验证考试Token异常: token={}", examToken, e);
            writeForbiddenResponse(response, "考试Token验证失败");
            return false;
        }
    }

    /**
     * 写入 403 禁止响应
     */
    private void writeForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(ResultCode.FORBIDDEN, message);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}
