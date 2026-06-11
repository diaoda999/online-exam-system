package com.exam.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员用户视图对象（含明文密码）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 明文密码 */
    private String plainPassword;

    /** 真实姓名 */
    private String realName;

    /** 角色编码 */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
