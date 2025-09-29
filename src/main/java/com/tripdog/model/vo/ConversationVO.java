package com.tripdog.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话VO
 *
 * @author: iohw
 * @date: 2025/9/26
 */
@Data
public class ConversationVO {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称（关联字段）
     */
    private String roleName;

    /**
     * 角色头像（关联字段）
     */
    private String roleAvatarUrl;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 总输入令牌数
     */
    private Long totalInputTokens;

    /**
     * 总输出令牌数
     */
    private Long totalOutputTokens;

    /**
     * 状态（1：活跃，0：已删除）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
