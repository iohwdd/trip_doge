package com.tripdog.ai.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author: iohw
 * @date: 2025/9/26 13:24
 * @description:
 */
@Configuration
@ConfigurationProperties(prefix = "pgvector")
@Data
public class PgVectorProperties {
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
    private String table;
}