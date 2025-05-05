package com.wan.r;

public class R {
    private String state; //状态 成功 失败
    private String code;  //状态码---http 200 404 500
    private Object data;

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
}
