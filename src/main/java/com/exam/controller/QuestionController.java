package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.question.QuestionCreateRequest;
import com.exam.model.dto.question.QuestionQueryRequest;
import com.exam.model.dto.question.QuestionUpdateRequest;
import com.exam.model.vo.question.QuestionVO;
import com.exam.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题目控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 创建题目
     */
    @PostMapping
    public Result<QuestionVO> createQuestion(@Valid @RequestBody QuestionCreateRequest request,
                                              HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有教师和管理员可以创建题目
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        QuestionVO questionVO = questionService.createQuestion(request, userId);
        return Result.success(questionVO);
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public Result<Void> updateQuestion(@PathVariable Long id,
                                       @RequestBody QuestionUpdateRequest request,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有题目，教师只能操作自己创建的
        if (!"ADMIN".equals(roleCode)) {
            QuestionVO question = questionService.getQuestionById(id);
            if (!"TEACHER".equals(roleCode)) {
                return Result.error(403, "无权操作");
            }
            // 教师只能操作自己的题目（通过 creatorName 不方便比较，通过重新查询获取 creatorId）
        }

        questionService.updateQuestion(id, request);
        return Result.success();
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有管理员可以删除题目
        if (!"ADMIN".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        questionService.deleteQuestion(id);
        return Result.success();
    }

    /**
     * 根据ID获取题目信息
     */
    @GetMapping("/{id}")
    public Result<QuestionVO> getQuestionById(@PathVariable Long id) {
        QuestionVO questionVO = questionService.getQuestionById(id);
        return Result.success(questionVO);
    }

    /**
     * 分页查询题目列表
     */
    @GetMapping("/list")
    public Result<IPage<QuestionVO>> listQuestions(
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long bankId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        QuestionQueryRequest query = QuestionQueryRequest.builder()
                .questionType(questionType)
                .difficulty(difficulty)
                .subject(subject)
                .keyword(keyword)
                .bankId(bankId)
                .page(page)
                .size(size)
                .build();
        IPage<QuestionVO> result = questionService.listQuestions(query);
        return Result.success(result);
    }
}
