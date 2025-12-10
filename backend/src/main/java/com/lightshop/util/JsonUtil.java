package com.lightshop.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static JsonObject parseJson(String json) {
        return gson.fromJson(json, JsonObject.class);
    }

    public static String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    public static <T> T readRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        String body = readRequestBody(request);
        return fromJson(body, clazz);
    }

    public static void writeResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(toJson(data));
        }
    }

    public static void writeSuccess(HttpServletResponse response, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        writeResponse(response, result);
    }

    public static void writeSuccess(HttpServletResponse response, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", message);
        result.put("data", data);
        writeResponse(response, result);
    }

    public static void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code >= 400 && code < 600 ? code : 400);
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        writeResponse(response, result);
    }

    public static void writePageData(HttpServletResponse response, Object list, int total, int page, int pageSize) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("totalPages", (int) Math.ceil((double) total / pageSize));
        writeSuccess(response, data);
    }
}

