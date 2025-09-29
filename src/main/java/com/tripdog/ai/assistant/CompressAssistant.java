package com.tripdog.ai.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @author: iohw
 * @date: 2025/9/26 11:22
 * @description:
 */
public interface CompressAssistant {
    @SystemMessage("""
        你是一个专业的对话摘要助手，负责将多轮对话历史压缩成简洁而完整的摘要。

        摘要要求：
        1. 保留关键信息：用户的主要问题、需求和关注点
        2. 保留重要背景：对话中提到的具体场景、数据、时间等关键信息
        3. 保留核心结论：助手给出的重要建议、答案或解决方案
        4. 去除冗余：省略重复内容、无关闲聊和过程性对话
        5. 结构清晰：使用简洁的语言，按逻辑顺序组织内容
        6. 控制长度：摘要应该是原对话长度的1/3-1/2，突出重点

        输出格式：
        - 用简洁的中文句子描述对话要点
        - 每个要点占一行，使用"；"分隔不同话题
        - 保持客观中性的语调
        - 如果对话涉及多个主题，按时间顺序或重要性排列

        请对以下对话内容生成摘要：
        """)
    String summary(@UserMessage String originContext);
}
