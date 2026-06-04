package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.user.LoginRequest;
import com.exam.model.dto.user.RegisterRequest;
import com.exam.model.dto.user.UserUpdateRequest;
import com.exam.model.vo.user.LoginVO;
import com.exam.model.vo.user.UserVO;
import com.exam.service.UserService;
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
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = userService.login(request);
        return Result.success(loginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public Result<IPage<UserVO>> listUsers(
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<UserVO> result = userService.listUsers(roleCode, status, page, size);
        return Result.success(result);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @RequestBody UserUpdateRequest request,
                                   HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有管理员或本人可以更新用户信息
        if (!"ADMIN".equals(roleCode) && !id.equals(currentUserId)) {
            return Result.error(403, "无权操作");
        }

        // 非管理员不能修改状态
        if (!"ADMIN".equals(roleCode) && request.getStatus() != null) {
            return Result.error(403, "无权修改用户状态");
        }

        userService.updateUser(id, request);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id,
                                   HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有管理员可以删除用户
        if (!"ADMIN".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        userService.deleteUser(id);
        return Result.success();
    }
}
