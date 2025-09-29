package com.tripdog.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.dashscope.tokenizers.Tokenizer;
import com.alibaba.dashscope.tokenizers.TokenizerFactory;
import com.tripdog.ai.compress.CompressionService;
import com.tripdog.common.Constants;
import com.tripdog.mapper.ChatHistoryMapper;
import com.tripdog.model.entity.ChatHistoryDO;
import com.tripdog.model.builder.ConversationBuilder;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.CustomMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.tripdog.common.Constants.INJECT_TEMPLATE;

/**
 * @author: iohw
 * @date: 2025/4/13 10:35
 * @description:
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {
    final ChatHistoryMapper chatHistoryMapper;
    private final Tokenizer tokenizer = TokenizerFactory.qwen();
    private final String USER = "user";
    private final String ASSISTANT = "assistant";
    private final String SYSTEM = "system";
    private final String TOOL = "tool";
    private final CompressionService compressionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ChatMessage> getMessages(Object o) {
        String conversationId = (String) o;
        List<ChatHistoryDO> chatHistoryDOS = chatHistoryMapper.selectAllById(conversationId);
        List<ChatMessage> chatMessages = new ArrayList<>();

        log.debug("获取对话历史，conversation_id: {}, 原始消息数量: {}", conversationId, chatHistoryDOS.size());

        // 如果没有历史消息，返回空列表（这是正常情况，新对话）
        if (chatHistoryDOS.isEmpty()) {
            log.debug("没有历史消息，返回空列表");
            return chatMessages;
        }

        for (ChatHistoryDO d : chatHistoryDOS) {
            // 上下文使用检索增强后的内容
            String content = StringUtils.hasText(d.getEnhancedContent()) ? d.getEnhancedContent() : d.getContent();

            switch (d.getRole()) {
                case USER:
                    chatMessages.add(UserMessage.from(content));
                    break;
                case ASSISTANT:
                    // 检查是否是包含工具调用的消息，如果是则跳过加载
                    if (isToolCallMessage(content)) {
                        log.debug("跳过加载包含工具调用的AI消息");
                        continue;
                    }
                    chatMessages.add(AiMessage.from(content));
                    break;
                case SYSTEM:
                    chatMessages.add(SystemMessage.from(content));
                    break;
            }
        }

        // 正常返回所有历史消息，让LangChain4j决定如何处理
        // DashScope需要完整的对话上下文来理解消息序列

        log.debug("过滤后的消息数量: {}, 原始消息数量: {}", chatMessages.size(), chatHistoryDOS.size());

        // 压缩处理
        return compressionService.compress(chatMessages);
    }

    @Override
    public void updateMessages(Object o, List<ChatMessage> list) {
        String conversationId = o.toString();
        ChatMessage latestMessage = list.getLast();
        String role = getRoleFromMessage(latestMessage);

        // 持久化时过滤：忽略 function call 相关的消息，不保存到数据库
        if (isFunctionCallRelatedMessage(latestMessage)) {
            log.debug("持久化过滤：忽略 function call 相关消息，消息类型: {}", latestMessage.getClass().getSimpleName());
            return;
        }

        String message = getContentMessage(latestMessage);

        // 对于AI消息，如果包含工具调用也不持久化
        if (Constants.ASSISTANT.equals(role) && isToolCallMessage(message)) {
            log.debug("持久化过滤：忽略包含工具调用的AI消息");
            return;
        }

        // 触发多轮改写/长期记忆生成
        // TODO: 可在这里调用multiTurnRewriteService分析historyMessages
        // List<ChatMessage> historyMessages = list.subList(1, list.size() - 1);
        // multiTurnRewriteService.rewrite(message, historyMessages);

        ChatHistoryDO chatHistoryDO;
        if (Constants.USER.equals(role)) {
            chatHistoryDO = ConversationBuilder.buildUserMessage(conversationId, message);
        } else if (Constants.ASSISTANT.equals(role)) {
            chatHistoryDO = ConversationBuilder.buildAssistantMessage(conversationId, message);
        } else {
            chatHistoryDO = ConversationBuilder.buildSystemMessage(conversationId, message);
        }

        String content = chatHistoryDO.getContent();
        if(isEnhanced(content)) {
            // 保存增强后的完整内容到 enhanced_content 字段
            chatHistoryDO.setEnhancedContent(content);
            // 原始内容提取并保存到 content 字段
            chatHistoryDO.setContent(extractOrigin(content));
        }
        chatHistoryMapper.insert(chatHistoryDO);
    }

    @Override
    public void deleteMessages(Object o) {
        String conversationId = o.toString();
        chatHistoryMapper.deleteByConversationId(conversationId);
    }


    private String getRoleFromMessage(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return SYSTEM;
        } else if (message instanceof UserMessage) {
            return USER;
        } else if (message instanceof AiMessage) {
            return ASSISTANT;
        } else if (message instanceof ToolExecutionResultMessage) {
            return TOOL;
        } else if (message instanceof CustomMessage) {
            return "custom";
        }
        throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
    }

    private String getContentMessage(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return ((SystemMessage) message).text();
        } else if (message instanceof UserMessage) {

            return ((UserMessage) message).singleText();
        } else if (message instanceof AiMessage) {
            AiMessage aiMessage = (AiMessage) message;
            // 如果AI消息包含工具调用请求，需要序列化保存
            if (aiMessage.hasToolExecutionRequests()) {
                return serializeAiMessageWithToolCalls(aiMessage);
            } else {
                return aiMessage.text();
            }
        } else if (message instanceof ToolExecutionResultMessage) {
            ToolExecutionResultMessage toolMsg = (ToolExecutionResultMessage) message;
            return serializeToolExecutionResult(toolMsg);
        } else if (message instanceof CustomMessage) {
            // 自定义消息可能需要JSON序列化
            return ((CustomMessage) message).toString();
        }
        throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
    }

    private String extractOrigin(String content) {
        int i = content.indexOf(INJECT_TEMPLATE);
        return i == -1 ? content : content.substring(i + INJECT_TEMPLATE.length());
    }

    private boolean isEnhanced(String content) {
        return content.contains(INJECT_TEMPLATE);
    }

    /**
     * 判断是否是 function call 相关的消息
     * 这类消息不需要维护到上下文记忆中
     */
    private boolean isFunctionCallRelatedMessage(ChatMessage message) {
        // 1. AI消息包含工具执行请求
        if (message instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
            return true;
        }

        // 2. 工具执行结果消息
        if (message instanceof ToolExecutionResultMessage) {
            return true;
        }

        // 3. 检查AI消息内容是否是工具调用相关的序列化内容
        if (message instanceof AiMessage aiMessage) {
            String content = aiMessage.text();
            if (content != null && isToolCallMessage(content)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查消息内容是否包含工具调用
     */
    private boolean isToolCallMessage(String content) {
        return content.startsWith("{\"text\":") && content.contains("\"toolExecutionRequests\":");
    }

    /**
     * 序列化包含工具调用的AI消息
     */
    private String serializeAiMessageWithToolCalls(AiMessage aiMessage) {
        try {
            // 创建一个包含文本和工具调用的结构
            var messageData = new java.util.HashMap<String, Object>();
            messageData.put("text", aiMessage.text());
            messageData.put("toolExecutionRequests", aiMessage.toolExecutionRequests());
            return objectMapper.writeValueAsString(messageData);
        } catch (JsonProcessingException e) {
            log.error("序列化AI消息失败", e);
            return aiMessage.text(); // 降级处理
        }
    }

    /**
     * 重建包含工具调用的AI消息
     */
    private AiMessage reconstructAiMessageWithToolCalls(String content) {
        try {
            @SuppressWarnings("unchecked")
            var messageData = objectMapper.readValue(content, java.util.HashMap.class);
            String text = (String) messageData.get("text");

            @SuppressWarnings("unchecked")
            var toolRequestsData = (java.util.List<java.util.Map<String, Object>>) messageData.get("toolExecutionRequests");

            if (toolRequestsData != null && !toolRequestsData.isEmpty()) {
                var toolRequests = new ArrayList<ToolExecutionRequest>();
                for (var requestData : toolRequestsData) {
                    ToolExecutionRequest request = ToolExecutionRequest.builder()
                        .id((String) requestData.get("id"))
                        .name((String) requestData.get("name"))
                        .arguments((String) requestData.get("arguments"))
                        .build();
                    toolRequests.add(request);
                }
                return AiMessage.from(text, toolRequests);
            } else {
                return AiMessage.from(text);
            }
        } catch (Exception e) {
            log.error("重建AI消息失败", e);
            return AiMessage.from(content); // 降级处理
        }
    }

    /**
     * 序列化工具执行结果
     */
    private String serializeToolExecutionResult(ToolExecutionResultMessage toolMsg) {
        try {
            var resultData = new HashMap<String, Object>();
            resultData.put("id", toolMsg.id());
            resultData.put("toolName", toolMsg.toolName());
            resultData.put("text", toolMsg.text());
            return objectMapper.writeValueAsString(resultData);
        } catch (JsonProcessingException e) {
            log.error("序列化工具执行结果失败", e);
            return String.format("{\"id\":\"%s\",\"toolName\":\"%s\",\"text\":\"%s\"}",
                toolMsg.id(), toolMsg.toolName(), toolMsg.text());
        }
    }

    /**
     * 重建工具执行结果消息
     */
    private ToolExecutionResultMessage reconstructToolExecutionResultMessage(String content) {
        try {
            @SuppressWarnings("unchecked")
            var resultData = objectMapper.readValue(content, java.util.HashMap.class);
            return ToolExecutionResultMessage.from(
                (String) resultData.get("id"),
                (String) resultData.get("toolName"),
                (String) resultData.get("text")
            );
        } catch (Exception e) {
            log.error("重建工具执行结果消息失败", e);
            // 降级处理，尝试解析旧格式
            return ToolExecutionResultMessage.from("unknown", "unknown", content);
        }
    }
}
