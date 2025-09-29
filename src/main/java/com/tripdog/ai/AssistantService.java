package com.tripdog.ai;

import org.springframework.context.annotation.Configuration;

import com.tripdog.ai.assistant.ChatAssistant;
import com.tripdog.ai.embedding.RetrieverFactory;
import com.tripdog.ai.mcp.McpClientFactory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import static com.tripdog.common.Constants.INJECT_TEMPLATE;

/**
 * @author: iohw
 * @date: 2025/9/24 22:21
 * @description:
 */
@Configuration
@RequiredArgsConstructor
public class AssistantService {
    final StreamingChatModel chatLanguageModel;
    final RetrieverFactory retrieverFactory;
    final CustomerChatMemoryProvider chatMemoryProvider;
    final McpClientFactory mcpClientFactory;

    public ChatAssistant getAssistant(Long roleId, Long userId) {
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
            .contentRetriever(retrieverFactory.getRetriever(roleId, userId))
            .contentInjector(DefaultContentInjector.builder()
                .promptTemplate(PromptTemplate.from("{{userMessage}}" + INJECT_TEMPLATE + "{{contents}}"))
                .build())
            .build();

        // todo 接入mcp
        // McpClient mcpClient = mcpClientFactory.getMcpClient(WEB_SEARCH);
        // McpToolProvider toolProvider = McpToolProvider.builder()
        //     .mcpClients(mcpClient)
        //     .build();

        return AiServices.builder(ChatAssistant.class)
            .streamingChatModel(chatLanguageModel)
            .retrievalAugmentor(retrievalAugmentor)
            .chatMemoryProvider(chatMemoryProvider)
            // .tools(new MyTools())
            // .toolProvider(toolProvider)
            .build();

    }

}
