package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.paper.PaperCreateRequest;
import com.exam.model.vo.paper.PaperDetailVO;
import com.exam.model.vo.paper.PaperVO;
import com.exam.service.PaperService;
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
 * 试卷控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/paper")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    /**
     * 创建试卷
     */
    @PostMapping
    public Result<PaperDetailVO> createPaper(@Valid @RequestBody PaperCreateRequest request,
                                              HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有教师和管理员可以创建试卷
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        PaperDetailVO paperDetailVO = paperService.createPaper(request, userId);
        return Result.success(paperDetailVO);
    }

    /**
     * 更新试卷
     */
    @PutMapping("/{id}")
    public Result<Void> updatePaper(@PathVariable Long id,
                                    @RequestBody PaperCreateRequest request,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有试卷，教师只能操作自己的
        if (!"ADMIN".equals(roleCode)) {
            PaperDetailVO paper = paperService.getPaperById(id);
            if (!"TEACHER".equals(roleCode)) {
                return Result.error(403, "无权操作");
            }
        }

        paperService.updatePaper(id, request);
        return Result.success();
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePaper(@PathVariable Long id,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有管理员可以删除试卷
        if (!"ADMIN".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        paperService.deletePaper(id);
        return Result.success();
    }

    /**
     * 根据ID获取试卷详情
     */
    @GetMapping("/{id}")
    public Result<PaperDetailVO> getPaperById(@PathVariable Long id) {
        PaperDetailVO paperDetailVO = paperService.getPaperById(id);
        return Result.success(paperDetailVO);
    }

    /**
     * 分页查询试卷列表
     */
    @GetMapping("/list")
    public Result<IPage<PaperVO>> listPapers(
            @RequestParam(required = false) Long creatorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<PaperVO> result = paperService.listPapers(creatorId, page, size);
        return Result.success(result);
    }
}
