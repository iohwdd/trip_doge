package com.tripdog.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 文档列表查询请求DTO
 */
@Data
public class DocListDTO {

    /**
     * 角色ID
     */
    private Long roleId;
}
