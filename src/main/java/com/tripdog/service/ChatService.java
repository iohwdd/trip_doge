package com.tripdog.service;

import com.tripdog.model.dto.ChatReqDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 与指定角色聊天
     * @param roleId 角色ID
     * @param userId 用户ID
     * @param ChatReqDTO 聊天请求
     * @return SseEmitter 流式响应
     */
    SseEmitter chat(Long roleId, Long userId, ChatReqDTO ChatReqDTO);

}
