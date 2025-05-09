package com.wan.r;

import lombok.Data;

@Data
public class R<T> {
    private String state; //状态 成功 失败
    private String code;  //状态码---http 200 404 500
    private T data;
}
