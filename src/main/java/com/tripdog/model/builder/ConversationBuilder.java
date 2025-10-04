package com.tripdog.model.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripdog.common.Constants;
import com.tripdog.common.utils.JsonUtil;
import com.tripdog.model.entity.ConversationDO;
import com.tripdog.model.entity.ChatHistoryDO;
import java.time.LocalDateTime;
import java.util.List;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

/**
 * 会话和聊天历史对象构建器
 *
 * @author: iohw
 * @date: 2025/9/26
 */
public class ConversationBuilder {

    /**
     * 创建新会话
     */
    public static ConversationDO buildNewConversation(Long userId, Long roleId, String roleName) {
        ConversationDO conversation = new ConversationDO();
        conversation.setUserId(userId);
        conversation.setRoleId(roleId);
        conversation.setTitle("与" + roleName + "的对话");
        conversation.setConversationType("COMPANION");
        conversation.setStatus(1); // 活跃状态
        conversation.setIntimacyLevel(0); // 初始亲密度
        conversation.setMessageCount(0);
        conversation.setTotalInputTokens(0);
        conversation.setTotalOutputTokens(0);
        conversation.setContextWindowSize(20); // 默认记忆20条消息
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversation;
    }

    /**
     * 创建系统消息
     */
    public static ChatHistoryDO buildSystemMessage(String conversationId, String systemPrompt) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.SYSTEM);
        chatHistory.setContent(systemPrompt);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建用户消息
     */
    public static ChatHistoryDO buildUserMessage(String conversationId, String content) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.USER);
        chatHistory.setContent(content);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建助手消息
     */
    public static ChatHistoryDO buildAssistantMessage(String conversationId, String content) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.ASSISTANT);
        chatHistory.setContent(content);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建重置消息
     */
    public static ChatHistoryDO buildResetMessage(String conversationId) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.SYSTEM);
        chatHistory.setContent("[重置了聊天记录]");
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建带有检索增强内容的用户消息
     */
    public static ChatHistoryDO buildEnhancedUserMessage(String conversationId, String originalContent, String enhancedContent) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.USER);
        chatHistory.setContent(originalContent);
        chatHistory.setEnhancedContent(enhancedContent);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建带有检索增强内容的助手消息
     */
    public static ChatHistoryDO buildEnhancedAssistantMessage(String conversationId, String originalContent, String enhancedContent) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.ASSISTANT);
        chatHistory.setContent(originalContent);
        chatHistory.setEnhancedContent(enhancedContent);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建带有工具调用的助手消息
     */
    public static ChatHistoryDO buildAssistantMessageWithToolCall(String conversationId, String content, String toolCall) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.ASSISTANT);
        chatHistory.setContent(content);
        chatHistory.setToolCall(toolCall);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建带有工具执行结果的助手消息
     */
    public static ChatHistoryDO buildAssistantMessageWithToolResult(String conversationId, String content, String toolExecResult) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.ASSISTANT);
        chatHistory.setContent(content);
        chatHistory.setToolExecResult(toolExecResult);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建完整的助手消息（支持检索增强、工具调用和工具执行结果）
     */
    public static ChatHistoryDO buildFullAssistantMessage(String conversationId, String content, String enhancedContent, String toolCall, String toolExecResult) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.ASSISTANT);
        chatHistory.setContent(content);
        chatHistory.setEnhancedContent(enhancedContent);
        chatHistory.setToolCall(toolCall);
        chatHistory.setToolExecResult(toolExecResult);
        chatHistory.setCreatedAt(LocalDateTime.now());
        return chatHistory;
    }

    /**
     * 创建完整的助手消息（支持检索增强和工具执行结果）- 兼容性方法
     */
    public static ChatHistoryDO buildFullAssistantMessage(String conversationId, String content, String enhancedContent, String toolExecResult) {
        return buildFullAssistantMessage(conversationId, content, enhancedContent, null, toolExecResult);
    }

    public static ChatHistoryDO buildToolExecResultMessage(String conversationId, ToolExecutionResultMessage toolExecResult) {
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.ASSISTANT);
        chatHistory.setToolExecResult(toolExecResult.text());
        chatHistory.setCreatedAt(LocalDateTime.now());

        return chatHistory;
    }

    public static ChatHistoryDO buildToolCallMessage(String conversationId, AiMessage message) {
        List<ToolExecutionRequest> toolExecutionRequests = message.toolExecutionRequests();
        ChatHistoryDO chatHistory = new ChatHistoryDO();
        chatHistory.setConversationId(conversationId);
        chatHistory.setRole(Constants.ASSISTANT);
        chatHistory.setToolCall(JsonUtil.toJson(toolExecutionRequests));
        chatHistory.setCreatedAt(LocalDateTime.now());

        return chatHistory;
    }
}
