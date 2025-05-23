package com.wan.util;

import com.google.gson.Gson;
import com.wan.r.R;

public class Help {

    // 返回json格式的数据
    public static <T> String getJson(T data) {

        R<T> r = new R();
        r.setCode("200");
        r.setState("ok");
        r.setData(data);

        Gson gson = new Gson();
        String json = gson.toJson(r);

        return json;
    }

    public static String getJsonError(String message) {
        R<String> r = new R<>(); // 这里因为 data 是 String 类型，所以 R<String>
        r.setCode("500");
        r.setState("error");
        r.setData(message);      // 将错误消息设置到 data 字段

        Gson gson = new Gson();
        return gson.toJson(r);
    }
}


/*
*       R r = new R();
        r.setCode("200");
        r.setState("ok");
        r.setData(allGroups);

        Gson gson = new Gson();
        String json = gson.toJson(r);
* */