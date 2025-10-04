package com.tripdog.common.utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * @author: iohw
 * @date: 2025/9/29 12:39
 * @description:
 */
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Gson gson = new Gson();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> List<T> fromJsonList(String json, TypeToken<List<T>> typeToken) {
        return gson.fromJson(json, typeToken.getType());
    }
}
