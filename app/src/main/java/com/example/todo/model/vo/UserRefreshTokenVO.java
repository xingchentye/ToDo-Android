package com.example.todo.model.vo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用户刷新令牌响应值对象
 * 包含刷新后返回的新令牌信息
 */
public class UserRefreshTokenVO {
    private String accessToken;        // 新的访问令牌
    private long accessTokenExpiredAt; // 新的访问令牌过期时间戳

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

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String expireTime = sdf.format(new Date(accessTokenExpiredAt * 1000)); // 秒转毫秒

        return "UserRefreshTokenVO{" +
                "访问令牌长度=" + (accessToken != null ? accessToken.length() : 0) +
                ", 令牌过期时间=" + expireTime +
                '}';
    }
}