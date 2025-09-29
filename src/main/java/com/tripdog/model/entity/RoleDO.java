package com.tripdog.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色信息表
 */
@Data
public class RoleDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色唯一标识码，如 SHIBA_INU/RAGDOLL_CAT/GREY_WOLF
     */
    private String code;

    /**
     * 角色展示名称
     */
    private String name;

    /**
     * 角色头像URL
     */
    private String avatarUrl;

    /**
     * 角色背景描述
     */
    private String description;

    /**
     * AI模型配置，包含system_prompt、temperature、max_tokens、top_p等参数
     */
    private String aiSetting;

    /**
     * 角色特性配置，包含性格特征、能力描述、行为规则等
     */
    private String roleSetting;

    /**
     * 状态：1=启用，0=禁用
     */
    private Integer status;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
