package com.example.todo.model.dto;

/**
 * 用户登录数据传输对象
 * 用于封装登录请求的用户名和密码
 */
public class UserLoginDTO {
    private String username; // 用户名
    private String password; // 密码

    /**
     * 构造函数
     * @param username 用户名
     * @param password 密码
     */
    public UserLoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ==================== Getter和Setter方法 ====================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserLoginDTO{" +
                "用户名='" + username + '\'' +
                ", 密码长度=" + (password != null ? password.length() : 0) +
                '}';
    }
}