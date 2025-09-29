package com.tripdog;

import org.junit.jupiter.api.Test;

import com.tripdog.common.utils.GeneratorIdUtils;

/**
 * @author: iohw
 * @date: 2025/9/28 15:15
 * @description:
 */
public class CommonTests {
    @Test
    public void test() {
        System.out.println(GeneratorIdUtils.nextId());
    }
}
