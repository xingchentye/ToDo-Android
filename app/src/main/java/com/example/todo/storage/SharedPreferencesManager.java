package com.example.todo.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SharedPreferences数据存储管理器
 * 统一管理应用本地数据存储
 */
public class SharedPreferencesManager {
    private static final String TAG = "💾 SP管理器"; // 日志标签

    // 存储文件名
    private static final String PREFS_NAME = "TodoPrefs";

    // 键名常量
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRED_AT = "token_expired_at";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    /**
     * 构造函数
     * @param context 上下文对象
     */
    public SharedPreferencesManager(Context context) {
        Log.d(TAG, "🔄 初始化SharedPreferences管理器");
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Log.d(TAG, "✅ SharedPreferences管理器初始化完成");
    }

    // ==================== 用户凭证相关方法 ====================

    /**
     * 保存用户凭证信息
     * @param username 用户名
     * @param password 密码
     * @param rememberMe 是否记住登录状态
     */
    public void saveUserCredentials(String username, String password, boolean rememberMe) {
        Log.d(TAG, "💾 保存用户凭证 - 用户名: " + username + ", 记住我: " + rememberMe);

        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        boolean success = editor.commit();

        if (success) {
            Log.d(TAG, "✅ 用户凭证保存成功");
        } else {
            Log.e(TAG, "❌ 用户凭证保存失败");
        }
    }

    public String getSavedUsername() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        Log.d(TAG, "📖 读取保存的用户名: " + (username.isEmpty() ? "空" : username));
        return username;
    }

    public String getSavedPassword() {
        String password = sharedPreferences.getString(KEY_PASSWORD, "");
        Log.d(TAG, "📖 读取保存的密码: " + (password.isEmpty() ? "空" : "长度=" + password.length()));
        return password;
    }

    public boolean shouldRememberMe() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        Log.d(TAG, "📖 读取记住我状态: " + rememberMe);
        return rememberMe;
    }

    // ==================== Token相关方法 ====================

    /**
     * 保存令牌信息
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiredAtSeconds 过期时间戳（秒级）
     */
    public void saveTokens(String accessToken, String refreshToken, long expiredAtSeconds) {
        // 将秒级时间戳转换为毫秒级
        long expiredAtMillis = expiredAtSeconds * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String expireTime = sdf.format(new Date(expiredAtMillis));

        Log.d(TAG, "🔐 保存令牌信息 - " +
                "访问令牌长度: " + (accessToken != null ? accessToken.length() : 0) +
                ", 刷新令牌长度: " + (refreshToken != null ? refreshToken.length() : 0) +
                ", 过期时间(秒): " + expiredAtSeconds +
                ", 过期时间: " + expireTime);

        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putLong(KEY_TOKEN_EXPIRED_AT, expiredAtMillis); // 存储为毫秒级
        boolean success = editor.commit();

        if (success) {
            Log.d(TAG, "✅ 令牌信息保存成功");
        } else {
            Log.e(TAG, "❌ 令牌信息保存失败");
        }
    }

    public String getAccessToken() {
        String token = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        Log.d(TAG, "🔑 读取访问令牌: " + (token.isEmpty() ? "空" : "长度=" + token.length()));
        return token;
    }

    public String getRefreshToken() {
        String token = sharedPreferences.getString(KEY_REFRESH_TOKEN, "");
        Log.d(TAG, "🔄 读取刷新令牌: " + (token.isEmpty() ? "空" : "长度=" + token.length()));
        return token;
    }

    public long getTokenExpiredAt() {
        long expiredAt = sharedPreferences.getLong(KEY_TOKEN_EXPIRED_AT, 0);
        if (expiredAt > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Log.d(TAG, "⏰ 令牌过期时间: " + sdf.format(new Date(expiredAt)));
        } else {
            Log.d(TAG, "⏰ 令牌过期时间: 未设置");
        }
        return expiredAt;
    }

    /**
     * 检查令牌是否有效
     * @return true-有效, false-无效
     */
    public boolean isTokenValid() {
        String token = getAccessToken();
        long expiredAtMillis = getTokenExpiredAt();
        long currentTimeMillis = System.currentTimeMillis();

        // 添加安全边际，提前5分钟认为令牌过期
        long safetyMargin = 5 * 60 * 1000; // 5分钟
        boolean isValid = !token.isEmpty() && (currentTimeMillis + safetyMargin) < expiredAtMillis;

        Log.d(TAG, "🔍 令牌有效性检查 - " +
                "令牌存在: " + !token.isEmpty() +
                ", 当前时间: " + currentTimeMillis +
                ", 过期时间: " + expiredAtMillis +
                ", 剩余时间(秒): " + ((expiredAtMillis - currentTimeMillis) / 1000) +
                ", 结果: " + (isValid ? "有效" : "无效"));

        return isValid;
    }

    // ==================== 清理方法 ====================

    /**
     * 清除所有存储数据
     */
    public void clearAll() {
        Log.d(TAG, "🗑️ 清除所有存储数据");
        editor.clear();
        boolean success = editor.commit();

        if (success) {
            Log.d(TAG, "✅ 所有数据清除成功");
        } else {
            Log.e(TAG, "❌ 数据清除失败");
        }
    }

    /**
     * 清除令牌信息
     */
    public void clearTokens() {
        Log.d(TAG, "🗑️ 清除令牌信息");
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_TOKEN_EXPIRED_AT);
        boolean success = editor.commit();

        if (success) {
            Log.d(TAG, "✅ 令牌信息清除成功");
        } else {
            Log.e(TAG, "❌ 令牌信息清除失败");
        }
    }

    /**
     * 清除用户凭证（保留令牌）
     */
    public void clearUserCredentials() {
        Log.d(TAG, "🗑️ 清除用户凭证");
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_REMEMBER_ME);
        boolean success = editor.commit();

        if (success) {
            Log.d(TAG, "✅ 用户凭证清除成功");
        } else {
            Log.e(TAG, "❌ 用户凭证清除失败");
        }
    }
}