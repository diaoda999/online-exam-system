package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.entity.ExamRecord;
import com.exam.model.vo.exam.ExamRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考试记录 Mapper 接口
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    /**
     * 分页查询考试记录列表（关联考试名/用户名/真实姓名）
     *
     * @param page   分页参数
     * @param examId 考试ID（可选）
     * @param status 记录状态（可选）
     * @return 分页记录视图列表
     */
    IPage<ExamRecordVO> selectRecordList(IPage<ExamRecordVO> page,
                                           @Param("examId") Long examId,
                                           @Param("status") String status);
}
