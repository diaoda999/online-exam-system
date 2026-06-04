package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.bank.BankCreateRequest;
import com.exam.model.dto.bank.BankUpdateRequest;
import com.exam.model.vo.bank.BankDetailVO;
import com.exam.model.vo.bank.BankVO;
import com.exam.service.QuestionBankService;
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

import java.util.List;
import java.util.Map;

/**
 * 题库控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    /**
     * 创建题库
     */
    @PostMapping
    public Result<BankVO> createBank(@Valid @RequestBody BankCreateRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有教师和管理员可以创建题库
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        BankVO bankVO = questionBankService.createBank(request, userId);
        return Result.success(bankVO);
    }

    /**
     * 更新题库
     */
    @PutMapping("/{id}")
    public Result<Void> updateBank(@PathVariable Long id,
                                   @RequestBody BankUpdateRequest request,
                                   HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 管理员可操作所有题库，教师只能操作自己的
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        questionBankService.updateBank(id, request);
        return Result.success();
    }

    /**
     * 删除题库
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBank(@PathVariable Long id,
                                   HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有管理员可以删除题库
        if (!"ADMIN".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        questionBankService.deleteBank(id);
        return Result.success();
    }

    /**
     * 根据ID获取题库详情
     */
    @GetMapping("/{id}")
    public Result<BankDetailVO> getBankById(@PathVariable Long id) {
        BankDetailVO bankDetailVO = questionBankService.getBankById(id);
        return Result.success(bankDetailVO);
    }

    /**
     * 分页查询题库列表
     */
    @GetMapping("/list")
    public Result<IPage<BankVO>> listBanks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<BankVO> result = questionBankService.listBanks(keyword, page, size);
        return Result.success(result);
    }

    /**
     * 添加题目到题库
     */
    @PostMapping("/{id}/questions")
    public Result<Void> addQuestions(@PathVariable Long id,
                                     @RequestBody Map<String, List<Long>> body,
                                     HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有教师和管理员可以操作
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        List<Long> questionIds = body.get("questionIds");
        if (questionIds == null || questionIds.isEmpty()) {
            return Result.error(400, "题目ID列表不能为空");
        }

        questionBankService.addQuestions(id, questionIds);
        return Result.success();
    }

    /**
     * 从题库移除题目
     */
    @DeleteMapping("/{id}/questions")
    public Result<Void> removeQuestions(@PathVariable Long id,
                                       @RequestBody Map<String, List<Long>> body,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有教师和管理员可以操作
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        List<Long> questionIds = body.get("questionIds");
        if (questionIds == null || questionIds.isEmpty()) {
            return Result.error(400, "题目ID列表不能为空");
        }

        questionBankService.removeQuestions(id, questionIds);
        return Result.success();
    }
}
