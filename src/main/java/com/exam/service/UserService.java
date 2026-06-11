package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.user.LoginRequest;
import com.exam.model.dto.user.RegisterRequest;
import com.exam.model.dto.user.UserUpdateRequest;
import com.exam.model.vo.user.AdminUserVO;
import com.exam.model.vo.user.LoginVO;
import com.exam.model.vo.user.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录返回信息
     */
    LoginVO login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     */
    void register(RegisterRequest request);

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户视图对象
     */
    UserVO getUserById(Long id);

    /**
     * 分页查询用户列表
     *
     * @param roleCode 角色编码（可选）
     * @param status   状态（可选）
     * @param page     页码
     * @param size     每页数量
     * @return 分页结果
     */
    IPage<UserVO> listUsers(String roleCode, Integer status, int page, int size);

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param request 更新请求
     */
    void updateUser(Long id, UserUpdateRequest request);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 管理员查询用户列表（含密码哈希）
     */
    IPage<AdminUserVO> listUsersForAdmin(String roleCode, Integer status, int page, int size);
}
