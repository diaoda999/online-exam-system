package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.entity.User;
import com.exam.model.vo.user.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据角色查询用户列表（含角色名称）
     *
     * @param page     分页参数
     * @param roleCode 角色编码
     * @param status   状态
     * @return 分页用户视图列表
     */
    IPage<UserVO> selectUserListWithRole(IPage<UserVO> page,
                                          @Param("roleCode") String roleCode,
                                          @Param("status") Integer status);
}
