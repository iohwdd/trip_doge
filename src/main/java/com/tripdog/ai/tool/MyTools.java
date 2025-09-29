package com.tripdog.ai.tool;

import dev.langchain4j.agent.tool.Tool;

/**
 * @author: iohw
 * @date: 2025/9/27 14:16
 * @description:
 */
public class MyTools {

    @Tool
    public int square(int x) {
        return x * x;
    }
}
