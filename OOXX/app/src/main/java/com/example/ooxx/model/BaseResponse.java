package com.example.ooxx.model;

public class BaseResponse<T> {
    private int code;        // 状态码（200=成功）
    private String message;  // 提示信息
    private T data;          // 响应数据

    // Getter和Setter
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
