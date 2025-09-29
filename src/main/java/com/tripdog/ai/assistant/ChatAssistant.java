package com.tripdog.ai.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * @author: iohw
 * @date: 2025/9/22 23:13
 * @description:
 */
public interface ChatAssistant {
    TokenStream chat(@MemoryId String conversationId, @UserMessage String message);
}
