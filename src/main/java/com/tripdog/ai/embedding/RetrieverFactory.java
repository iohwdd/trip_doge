package com.tripdog.ai.embedding;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * @author: iohw
 * @date: 2025/9/26 14:21
 * @description:
 */
@Configuration
@RequiredArgsConstructor
public class RetrieverFactory {
    final String ROLE_ID = "roleId";
    final String USER_ID = "userId";
    final EmbeddingStore<TextSegment> embeddingStore;
    final EmbeddingModel embeddingModel;
    final Map<String, EmbeddingStoreContentRetriever> cache = new HashMap<>();

    public EmbeddingStoreContentRetriever getRetriever(Long roleId, Long userId) {
        String k = roleId + ":" + userId;
        if(cache.containsKey(k)) {
            return cache.get(k);
        }
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(10) //todo 配置抽离
            .minScore(0.8)
            .filter(metadataKey(ROLE_ID).isEqualTo(roleId)
                .and(metadataKey(USER_ID).isEqualTo(userId)))
            .build();
        cache.put(k, retriever);
        return retriever;
    }
}
