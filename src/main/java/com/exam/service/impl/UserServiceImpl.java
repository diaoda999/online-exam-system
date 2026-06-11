package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.common.util.JwtUtil;
import com.exam.model.dto.user.LoginRequest;
import com.exam.model.dto.user.RegisterRequest;
import com.exam.model.dto.user.UserUpdateRequest;
import com.exam.model.entity.Role;
import com.exam.model.entity.User;
import com.exam.model.mapper.RoleMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.user.AdminUserVO;
import com.exam.model.vo.user.LoginVO;
import com.exam.model.vo.user.UserVO;
import com.exam.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 校验状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        // 查询角色编码
        Role role = roleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "STUDENT";

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCode);

        log.info("用户登录成功: username={}, roleCode={}", user.getUsername(), roleCode);

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(roleCode)
                .build();
    }

    @Override
    public void register(RegisterRequest request) {
        // 检查用户名唯一
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }

        // 查询角色
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, request.getRoleCode())
        );
        if (role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的角色编码");
        }

        // BCrypt 加密密码
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 保存用户
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .plainPassword(request.getPassword())
                .realName(request.getRealName())
                .roleId(role.getId())
                .status(1)
                .build();
        userMapper.insert(user);

        log.info("用户注册成功: username={}, roleCode={}", request.getUsername(), request.getRoleCode());
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public IPage<UserVO> listUsers(String roleCode, Integer status, int page, int size) {
        Page<UserVO> pageParam = new Page<>(page, size);
        IPage<UserVO> result = userMapper.selectUserListWithRole(pageParam, roleCode, status);
        // 修正 SQL 字符集导致的角色名乱码：用 Java 代码重新查 Role 表
        result.getRecords().forEach(vo -> {
            User user = userMapper.selectById(vo.getId());
            if (user != null) {
                Role role = roleMapper.selectById(user.getRoleId());
                if (role != null) {
                    vo.setRoleName(role.getRoleName());
                }
            }
        });
        return result;
    }

    @Override
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 更新字段
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        userMapper.updateById(user);
        log.info("用户信息更新: userId={}", id);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        userMapper.deleteById(id);
        log.info("用户删除: userId={}", id);
    }

    /**
     * 将 User 实体转换为 UserVO
     */
    private UserVO convertToVO(User user) {
        Role role = roleMapper.selectById(user.getRoleId());
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(role != null ? role.getRoleCode() : null)
                .roleName(role != null ? role.getRoleName() : null)
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }

    @Override
    public IPage<AdminUserVO> listUsersForAdmin(String roleCode, Integer status, int page, int size) {
        // 先查普通用户分页
        IPage<UserVO> userPage = listUsers(roleCode, status, page, size);

        // 转换为 AdminUserVO，带上明文密码和正确的角色名
        IPage<AdminUserVO> adminPage = userPage.convert(userVO -> {
            User user = userMapper.selectById(userVO.getId());
            // 用 Java 代码获取正确的角色名，绕过 SQL 字符集乱码问题
            Role role = user != null ? roleMapper.selectById(user.getRoleId()) : null;
            String correctRoleName = role != null ? role.getRoleName() : userVO.getRoleName();

            return AdminUserVO.builder()
                    .id(userVO.getId())
                    .username(userVO.getUsername())
                    .plainPassword(user != null ? user.getPlainPassword() : null)
                    .realName(userVO.getRealName())
                    .roleCode(userVO.getRoleCode())
                    .roleName(correctRoleName)
                    .status(userVO.getStatus())
                    .createTime(userVO.getCreateTime())
                    .build();
        });
        return adminPage;
    }
}
