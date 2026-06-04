package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.exam.ExamCreateRequest;
import com.exam.model.dto.exam.ExamSaveProgressRequest;
import com.exam.model.dto.exam.ExamSubmitRequest;
import com.exam.model.dto.exam.ExamUpdateRequest;
import com.exam.model.vo.exam.ExamDetailVO;
import com.exam.model.vo.exam.ExamEnterVO;
import com.exam.model.vo.exam.ExamVO;

/**
 * 考试服务接口
 */
public interface ExamService {

    /**
     * 创建考试
     */
    ExamVO createExam(ExamCreateRequest request, Long creatorId);

    /**
     * 更新考试
     */
    void updateExam(Long id, ExamUpdateRequest request);

    /**
     * 删除考试
     */
    void deleteExam(Long id);

    /**
     * 根据ID获取考试详情
     */
    ExamDetailVO getExamById(Long id);

    /**
     * 分页查询考试列表
     */
    IPage<ExamVO> listExams(Long creatorId, String status, int page, int size);

    /**
     * 发布考试
     */
    void publishExam(Long id);

    /**
     * 学生进入考试
     */
    ExamEnterVO enterExam(Long examId, Long userId);

    /**
     * 保存单题答题进度到Redis
     */
    void saveProgress(ExamSaveProgressRequest request, Long userId);

    /**
     * 提交考试
     */
    void submitExam(ExamSubmitRequest request, Long userId);

    /**
     * MQ消费：自动提交考试
     */
    void autoSubmitExam(Long examId, Long userId);

    /**
     * 获取剩余秒数
     */
    Long getRemainingTime(Long examId, Long userId);

    /**
     * 结束考试
     */
    void endExam(Long id);
}
