package com.exam.config;

import com.exam.interceptor.AuthInterceptor;
import com.exam.interceptor.ExamTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final ExamTokenInterceptor examTokenInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor, ExamTokenInterceptor examTokenInterceptor) {
        this.authInterceptor = authInterceptor;
        this.examTokenInterceptor = examTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 登录态拦截器（排除登录和注册接口）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register"
                );

        // 考试 Token 校验拦截器（拦截需要验证考试会话的接口）
        // /api/exam/progress — 保存答题进度
        // /api/exam/submit  — 提交考试
        registry.addInterceptor(examTokenInterceptor)
                .addPathPatterns(
                        "/api/exam/progress",
                        "/api/exam/submit"
                );
    }
}
