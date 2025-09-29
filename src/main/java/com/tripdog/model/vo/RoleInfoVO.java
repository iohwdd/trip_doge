package com.tripdog.model.vo;

import lombok.Data;

/**
 * 角色信息VO
 */
@Data
public class RoleInfoVO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色代码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色设定
     */
    private String roleSetting;

    /**
     * 会话ID
     */
    private String conversationId;
}
