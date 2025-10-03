package com.example.todo.model.response;

/**
 * API统一响应包装类
 * @param <T> 数据类型
 */
public class ApiResponse<T> {
    private int code;           // 状态码
    private String message;     // 响应消息
    private String requestId;   // 请求ID
    private T data;             // 响应数据

    // ==================== Getter和Setter方法 ====================

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 判断请求是否成功
     * @return true-成功, false-失败
     */
    public boolean isSuccess() {
        return code == 200;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "状态码=" + code +
                ", 消息='" + message + '\'' +
                ", 请求ID='" + requestId + '\'' +
                ", 数据=" + (data != null ? data.toString() : "null") +
                '}';
    }
}