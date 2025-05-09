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
}


/*
*       R r = new R();
        r.setCode("200");
        r.setState("ok");
        r.setData(allGroups);

        Gson gson = new Gson();
        String json = gson.toJson(r);
* */