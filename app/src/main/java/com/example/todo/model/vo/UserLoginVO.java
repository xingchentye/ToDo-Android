package com.example.todo.model.vo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用户登录响应值对象
 * 包含登录成功后返回的令牌信息
 */
public class UserLoginVO {
    private String accessToken;        // 访问令牌
    private long accessTokenExpiredAt; // 访问令牌过期时间戳
    private String refreshToken;       // 刷新令牌

    // ==================== Getter和Setter方法 ====================

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getAccessTokenExpiredAt() {
        return accessTokenExpiredAt;
    }

    public void setAccessTokenExpiredAt(long accessTokenExpiredAt) {
        this.accessTokenExpiredAt = accessTokenExpiredAt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String expireTime = sdf.format(new Date(accessTokenExpiredAt));

        return "UserLoginVO{" +
                "访问令牌长度=" + (accessToken != null ? accessToken.length() : 0) +
                ", 令牌过期时间=" + expireTime +
                ", 刷新令牌长度=" + (refreshToken != null ? refreshToken.length() : 0) +
                '}';
    }
}