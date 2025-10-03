package com.example.todo.model.dto;

/**
 * 用户刷新令牌数据传输对象
 * 用于刷新访问令牌
 */
public class UserRefreshTokenDTO {
    private String refreshToken; // 刷新令牌

    /**
     * 构造函数
     * @param refreshToken 刷新令牌
     */
    public UserRefreshTokenDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // ==================== Getter和Setter方法 ====================

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "UserRefreshTokenDTO{" +
                "刷新令牌长度=" + (refreshToken != null ? refreshToken.length() : 0) +
                '}';
    }
}