package com.example.echo.codec;

import com.google.gson.Gson;

import lombok.Getter;

@Getter
public class JsonMessage {
    private String type;
    private String data;

    // 생성자, getter, setter
    public JsonMessage(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static JsonMessage fromJson(String json) {
        return new Gson().fromJson(json, JsonMessage.class);
    }

    @Override
    public String toString() {
        return "JsonMessage [type=" + type + ", data=" + data + "]";
    }
}
