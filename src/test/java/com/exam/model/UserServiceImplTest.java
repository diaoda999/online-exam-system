package com.exam.model;

import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.common.util.JwtUtil;
import com.exam.model.dto.user.LoginRequest;
import com.exam.model.dto.user.RegisterRequest;
import com.exam.model.entity.Role;
import com.exam.model.entity.User;
import com.exam.model.mapper.RoleMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.user.LoginVO;
import com.exam.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 * 覆盖登录和注册核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private BCryptPasswordEncoder passwordEncoder;
    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        mockRole = Role.builder()
                .id(1L)
                .roleName("学生")
                .roleCode("STUDENT")
                .build();

        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .realName("测试用户")
                .roleId(1L)
                .status(1)
                .build();
    }

    // ==================== 登录测试 ====================

    @Nested
    @DisplayName("登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功 - 正确的用户名和密码")
        void loginSuccess() {
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(userMapper.selectOne(any())).thenReturn(mockUser);
            when(roleMapper.selectById(1L)).thenReturn(mockRole);
            when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("mock-jwt-token");

            LoginVO result = userService.login(request);

            assertNotNull(result);
            assertEquals("mock-jwt-token", result.getToken());
            assertEquals(1L, result.getUserId());
            assertEquals("testuser", result.getUsername());
            assertEquals("测试用户", result.getRealName());
            assertEquals("STUDENT", result.getRoleCode());

            verify(userMapper).selectOne(any());
            verify(roleMapper).selectById(1L);
            verify(jwtUtil).generateToken(1L, "testuser", "STUDENT");
        }

        @Test
        @DisplayName("登录失败 - 用户不存在")
        void loginFailUserNotFound() {
            LoginRequest request = LoginRequest.builder()
                    .username("nonexistent")
                    .password("password123")
                    .build();

            when(userMapper.selectOne(any())).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.login(request));
            assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode());
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void loginFailWrongPassword() {
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("wrongpassword")
                    .build();

            when(userMapper.selectOne(any())).thenReturn(mockUser);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.login(request));
            assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode());
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("登录失败 - 账号已被禁用")
        void loginFailAccountDisabled() {
            mockUser.setStatus(0);
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(userMapper.selectOne(any())).thenReturn(mockUser);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.login(request));
            assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
            assertEquals("账号已被禁用", exception.getMessage());
        }

        @Test
        @DisplayName("登录成功 - 角色为null时默认STUDENT")
        void loginSuccessRoleNull() {
            when(userMapper.selectOne(any())).thenReturn(mockUser);
            when(roleMapper.selectById(1L)).thenReturn(null);
            when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("token");

            LoginVO result = userService.login(LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build());

            assertEquals("STUDENT", result.getRoleCode());
        }

        @Test
        @DisplayName("登录成功 - 管理员角色")
        void loginSuccessAdminRole() {
            Role adminRole = Role.builder().id(2L).roleName("管理员").roleCode("ADMIN").build();
            mockUser.setRoleId(2L);

            when(userMapper.selectOne(any())).thenReturn(mockUser);
            when(roleMapper.selectById(2L)).thenReturn(adminRole);
            when(jwtUtil.generateToken(1L, "testuser", "ADMIN")).thenReturn("admin-token");

            LoginVO result = userService.login(LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build());

            assertEquals("ADMIN", result.getRoleCode());
        }

        @Test
        @DisplayName("登录成功 - status为null时不应抛出禁用异常")
        void loginSuccessStatusNull() {
            mockUser.setStatus(null);
            when(userMapper.selectOne(any())).thenReturn(mockUser);
            when(roleMapper.selectById(1L)).thenReturn(mockRole);
            when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("token");

            LoginVO result = userService.login(LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build());

            assertNotNull(result);
        }
    }

    // ==================== 注册测试 ====================

    @Nested
    @DisplayName("注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功 - 标准学生注册")
        void registerSuccess() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .password("password123")
                    .realName("新用户")
                    .roleCode("STUDENT")
                    .build();

            when(userMapper.selectCount(any())).thenReturn(0L);
            when(roleMapper.selectOne(any())).thenReturn(mockRole);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            assertDoesNotThrow(() -> userService.register(request));

            verify(userMapper).selectCount(any());
            verify(roleMapper).selectOne(any());
            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void registerFailUsernameExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .realName("新用户")
                    .roleCode("STUDENT")
                    .build();

            when(userMapper.selectCount(any())).thenReturn(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.register(request));
            assertEquals(ResultCode.CONFLICT.getCode(), exception.getCode());
            assertEquals("用户名已存在", exception.getMessage());

            verify(userMapper, never()).insert(any());
        }

        @Test
        @DisplayName("注册失败 - 无效的角色编码")
        void registerFailInvalidRoleCode() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .password("password123")
                    .realName("新用户")
                    .roleCode("INVALID_ROLE")
                    .build();

            when(userMapper.selectCount(any())).thenReturn(0L);
            when(roleMapper.selectOne(any())).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.register(request));
            assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
            assertEquals("无效的角色编码", exception.getMessage());

            verify(userMapper, never()).insert(any());
        }

        @Test
        @DisplayName("注册成功 - 教师角色注册")
        void registerSuccessTeacherRole() {
            Role teacherRole = Role.builder().id(2L).roleName("教师").roleCode("TEACHER").build();
            RegisterRequest request = RegisterRequest.builder()
                    .username("teacher1")
                    .password("password123")
                    .realName("张老师")
                    .roleCode("TEACHER")
                    .build();

            when(userMapper.selectCount(any())).thenReturn(0L);
            when(roleMapper.selectOne(any())).thenReturn(teacherRole);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            assertDoesNotThrow(() -> userService.register(request));
        }

        @Test
        @DisplayName("注册成功 - 密码经过BCrypt加密存储")
        void registerSuccessPasswordEncoded() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .password("plainpassword")
                    .realName("新用户")
                    .roleCode("STUDENT")
                    .build();

            when(userMapper.selectCount(any())).thenReturn(0L);
            when(roleMapper.selectOne(any())).thenReturn(mockRole);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                assertTrue(passwordEncoder.matches("plainpassword", savedUser.getPassword()),
                        "密码应经过BCrypt加密");
                assertNotEquals("plainpassword", savedUser.getPassword(),
                        "存储的密码不应是明文");
                return 1;
            });

            userService.register(request);
        }
    }
}
