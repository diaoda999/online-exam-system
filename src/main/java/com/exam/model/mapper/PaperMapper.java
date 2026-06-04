package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.entity.Paper;
import com.exam.model.vo.paper.PaperVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 试卷 Mapper 接口
 */
@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    /**
     * 分页查询试卷列表（关联创建者名称+题目数量）
     *
     * @param page      分页参数
     * @param creatorId 创建者ID（可选）
     * @return 分页试卷视图列表
     */
    IPage<PaperVO> selectPaperListWithCreator(IPage<PaperVO> page,
                                                @Param("creatorId") Long creatorId);
}
