package com.exam.model.dto.bank;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建题库请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCreateRequest {

    /** 题库名称 */
    @NotBlank(message = "题库名称不能为空")
    private String bankName;

    /** 题库描述 */
    private String description;
}
