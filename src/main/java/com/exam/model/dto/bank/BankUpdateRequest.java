package com.exam.model.dto.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新题库请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankUpdateRequest {

    /** 题库名称 */
    private String bankName;

    /** 题库描述 */
    private String description;
}
