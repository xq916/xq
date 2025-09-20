package com.example.puzzle_server.common;

public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;
    
    // 构造函数
    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // 判断请求是否成功
    public boolean isSuccess() {
        return code == 200;
    }
    
    // Getter和Setter方法
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
}
