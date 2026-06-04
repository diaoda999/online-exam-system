package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.paper.PaperCreateRequest;
import com.exam.model.vo.paper.PaperDetailVO;
import com.exam.model.vo.paper.PaperVO;

/**
 * 试卷服务接口
 */
public interface PaperService {

    /**
     * 创建试卷
     *
     * @param request   创建试卷请求
     * @param creatorId 创建者ID
     * @return 试卷详情视图对象
     */
    PaperDetailVO createPaper(PaperCreateRequest request, Long creatorId);

    /**
     * 更新试卷
     *
     * @param id      试卷ID
     * @param request 更新请求
     */
    void updatePaper(Long id, PaperCreateRequest request);

    /**
     * 删除试卷
     *
     * @param id 试卷ID
     */
    void deletePaper(Long id);

    /**
     * 根据ID获取试卷详情
     *
     * @param id 试卷ID
     * @return 试卷详情视图对象
     */
    PaperDetailVO getPaperById(Long id);

    /**
     * 分页查询试卷列表
     *
     * @param creatorId 创建者ID（可选）
     * @param page      页码
     * @param size      每页数量
     * @return 分页结果
     */
    IPage<PaperVO> listPapers(Long creatorId, int page, int size);
}
