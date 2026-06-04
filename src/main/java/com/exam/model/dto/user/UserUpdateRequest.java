package com.exam.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户更新请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /** 真实姓名 */
    private String realName;

    /** 新密码 */
    private String password;

    /** 状态：0-禁用 1-启用 */
    private Integer status;
}
