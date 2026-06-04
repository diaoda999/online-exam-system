package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.exam.GradeRequest;
import com.exam.model.vo.exam.ExamRecordDetailVO;
import com.exam.model.vo.exam.ExamRecordVO;

/**
 * 考试记录服务接口
 */
public interface ExamRecordService {

    /**
     * 分页查询考试记录列表
     */
    IPage<ExamRecordVO> listRecords(Long examId, String status, int page, int size);

    /**
     * 获取记录详情
     */
    ExamRecordDetailVO getRecordDetail(Long recordId);

    /**
     * 自动批改客观题
     */
    void gradeObjective(Long examId);

    /**
     * 手动批改主观题
     */
    void gradeSubjective(GradeRequest request);

    /**
     * 根据考试ID和用户ID获取记录
     */
    ExamRecordVO getRecordByExamAndUser(Long examId, Long userId);
}
