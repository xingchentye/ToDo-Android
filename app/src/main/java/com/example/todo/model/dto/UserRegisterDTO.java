package com.example.todo.model.dto;

/**
 * 用户注册数据传输对象
 * 用于封装注册请求的用户信息
 */
public class UserRegisterDTO {
    private String username;  // 用户名
    private String nickName;  // 昵称
    private String email;     // 邮箱
    private String password;  // 密码

    /**
     * 构造函数
     * @param username 用户名
     * @param nickName 昵称
     * @param email 邮箱
     * @param password 密码
     */
    public UserRegisterDTO(String username, String nickName, String email, String password) {
        this.username = username;
        this.nickName = nickName;
        this.email = email;
        this.password = password;
    }

    // ==================== Getter和Setter方法 ====================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserRegisterDTO{" +
                "用户名='" + username + '\'' +
                ", 昵称='" + nickName + '\'' +
                ", 邮箱='" + email + '\'' +
                ", 密码长度=" + (password != null ? password.length() : 0) +
                '}';
    }
}