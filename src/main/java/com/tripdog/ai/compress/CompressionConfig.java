package com.tripdog.ai.compress;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 对话压缩配置（可后续改为读取配置文件）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "chat.compress")
public class CompressionConfig {
    /** 是否启用压缩 */
    private boolean enabled = true;
    /** 最大总token预算（粗略估算，可后续接入真实token统计） */
    private int maxTotalTokens = 6000;
    /** 最近保留的原始消息条数（User+Assistant交互消息） */
    private int recentRawCount = 10;
    /** 触发压缩的最小历史消息条数 */
    private int minMessagesToCompress = 18;
}
