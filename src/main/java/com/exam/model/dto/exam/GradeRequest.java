package com.exam.model.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批改请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {

    /** 批改数据列表 */
    @NotNull(message = "批改数据不能为空")
    private List<GradeItem> answers;
}
