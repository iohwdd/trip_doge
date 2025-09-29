package com.tripdog.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class UserDO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 邮箱，用于登录
     */
    private String email;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 状态：1=正常，0=禁用
     */
    private Integer status;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
