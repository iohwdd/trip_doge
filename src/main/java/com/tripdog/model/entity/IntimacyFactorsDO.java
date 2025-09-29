package com.tripdog.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 亲密度影响因素记录表
 */
@Data
public class IntimacyFactorsDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 影响因素类型
     */
    private String factorType;

    /**
     * 影响因素数值，可正可负
     */
    private Integer factorValue;

    /**
     * 影响因素描述
     */
    private String description;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;
}
