package com.tripdog.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 简单高效的日期时间格式化
 *
 * @author tripdog
 */
public class DateTimeUtils {

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认格式化器
     */
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    /**
     * 格式化LocalDateTime为字符串
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : null;
    }

    /**
     * 获取当前时间格式化字符串
     */
    public static String now() {
        return format(LocalDateTime.now());
    }
}
