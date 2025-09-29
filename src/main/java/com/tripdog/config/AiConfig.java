package com.tripdog.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;

/**
 * @author: iohw
 * @date: 2025/9/22 22:58
 * @description: AI相关配置类 - 配置MCP (Model Context Protocol) 传输
 */
@Configuration
public class AiConfig {

    @Value("${DASHSCOPE_API_KEY}")
    private String dashscopeApiKey;

    /**
     * todo mcp接入异常
     * @return
     */
    @Bean
    public McpTransport webSearchMcpTransport() {
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + dashscopeApiKey,
            "Content-Type", "application/json"
        );

        return new StreamableHttpMcpTransport.Builder()
            .url("https://dashscope.aliyuncs.com/api/v1/mcps/WebSearch/sse")
            .customHeaders(headers)
            .logRequests(true)
            .logResponses(true)
            .build();
    }
}
