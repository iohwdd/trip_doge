package com.tripdog.model.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户与角色对话会话表
 */
@Data
public class ConversationDO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 会话业务ID - 用于持久化和关联
     */
    private String conversationId;

    /**
     * 用户ID，关联用户表
     */
    private Long userId;

    /**
     * 角色ID，关联角色表
     */
    private Long roleId;

    /**
     * 会话标题，如"与小柴的冒险之旅"
     */
    private String title;

    /**
     * 会话类型：COMPANION=陪伴，ADVENTURE=冒险，GUIDANCE=指导
     */
    private String conversationType;

    /**
     * 会话状态：1=活跃，2=暂停，3=完结
     */
    private Integer status;

    /**
     * 亲密度等级：0-100，影响角色回应深度
     */
    private Integer intimacyLevel;

    /**
     * 最后互动时间
     */
    private LocalDateTime lastMessageAt;

    /**
     * 对话消息总数
     */
    private Integer messageCount;

    /**
     * 累计输入token数
     */
    private Integer totalInputTokens;

    /**
     * 累计输出token数
     */
    private Integer totalOutputTokens;

    /**
     * 记忆长度（保留最近N条消息）
     */
    private Integer contextWindowSize;

    /**
     * 个性化调整：{"energy_level": "high", "response_style": "playful"}
     */
    private String personalityAdjustment;

    /**
     * 标签：如"日常陪伴,心情低落,需要鼓励"等
     */
    private String tags;

    /**
     * 特殊备注：用户重要信息，角色需要记住的内容
     */
    private String specialNotes;

    /**
     * 建立连接时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 构造函数中生成UUID
     */
    public ConversationDO() {
        this.conversationId = UUID.randomUUID().toString();
    }
}
