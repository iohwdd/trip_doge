package com.tripdog.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tripdog.common.utils.DateTimeUtils;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * LocalDateTime的自定义JSON序列化器
 * 统一将LocalDateTime格式化为指定格式的字符串
 *
 * @author tripdog
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            gen.writeString(DateTimeUtils.format(value));
        } else {
            gen.writeNull();
        }
    }
}
