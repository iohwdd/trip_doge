package com.tripdog.service.impl;

import java.io.IOException;
import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.tripdog.ai.AssistantService;
import com.tripdog.ai.assistant.ChatAssistant;
import com.tripdog.common.utils.FileUploadUtils;
import com.tripdog.common.utils.ThreadLocalUtils;
import com.tripdog.model.dto.FileUploadDTO;
import com.tripdog.model.entity.ConversationDO;
import com.tripdog.model.entity.RoleDO;
import com.tripdog.model.dto.ChatReqDTO;
import com.tripdog.service.ChatService;
import com.tripdog.mapper.ChatHistoryMapper;
import com.tripdog.mapper.RoleMapper;
import com.tripdog.common.utils.RoleConfigParser;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.tripdog.common.Constants.ROLE_ID;

/**
 * 聊天服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final StreamingChatModel chatLanguageModel;
    private final ConversationServiceImpl conversationServiceImpl;
    private final ChatHistoryMapper chatHistoryMapper;
    private final RoleMapper roleMapper;
    private final AssistantService assistantService;
    private final FileUploadUtils fileUploadUtils;

    @Override
    public SseEmitter chat(Long roleId, Long userId, ChatReqDTO chatReqDTO) {
        ThreadLocalUtils.set(ROLE_ID, roleId);
        SseEmitter emitter = new SseEmitter(-1L);

        try {
            // 1. 获取或创建会话
            ConversationDO conversation = conversationServiceImpl.getOrCreateConversation(userId, roleId);

            // 2. 获取角色信息
            RoleDO role = roleMapper.selectById(roleId);
            if (role == null) {
                emitter.completeWithError(new RuntimeException("角色不存在"));
                return emitter;
            }

            // 3. 从角色配置中提取系统提示词
            String systemPrompt = RoleConfigParser.extractSystemPrompt(role.getAiSetting());
            log.info("角色[{}]使用系统提示词: {}", role.getName(), systemPrompt);

            StringBuilder responseBuilder = new StringBuilder();
            // 使用角色专用的聊天助手，传入角色的系统提示词
            ChatAssistant assistant = assistantService.getAssistant();

            MultipartFile file = chatReqDTO.getFile();
            TokenStream stream;
            if(file != null) {
                // todo 多模态支持
                FileUploadDTO fileUploadDTO = fileUploadUtils.upload2Minio(chatReqDTO.getFile(), userId, "/tmp");
                String imageUrl = fileUploadUtils.getUrlFromMinio(fileUploadDTO.getFileUrl());
                UserMessage message = UserMessage.from(TextContent.from(chatReqDTO.getMessage()), ImageContent.from(URI.create(imageUrl)));
                stream = assistant.chat(conversation.getConversationId(), message);
            }else {
                stream = assistant.chat(conversation.getConversationId(), chatReqDTO.getMessage());
            }

            stream.onPartialResponse((data) -> {
                try {
                    responseBuilder.append(data);
                    emitter.send(SseEmitter.event()
                        .data(data)
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name("message")
                    );
                } catch (IOException e) {
                    log.error("发送SSE部分响应失败", e);
                    emitter.completeWithError(e);
                }
            }).onCompleteResponse((data) -> {
                try {

                    // 8. 更新会话统计
                    conversationServiceImpl.updateConversationStats(conversation.getConversationId(), null, null);

                    emitter.send(SseEmitter.event()
                        .data("[DONE]")
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name("done"));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("发送SSE完成响应失败", e);
                    emitter.completeWithError(e);
                }
            }).onError((ex) -> {
                log.error("AI聊天流处理异常", ex);
                emitter.completeWithError(ex);
            }).start();

        } catch (Exception e) {
            log.error("聊天服务处理异常", e);
            emitter.completeWithError(e);
        } finally {
            ThreadLocalUtils.remove(ROLE_ID);
        }

        return emitter;
    }
}
