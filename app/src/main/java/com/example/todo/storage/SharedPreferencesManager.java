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
     * @param rememberMe 是否记住登录状态
     */
    public void saveUserCredentials(String username, boolean rememberMe) {
        Log.d(TAG, "💾 保存用户凭证 - 用户名: " + username + ", 记住我: " + rememberMe);

        editor.putString(KEY_USERNAME, username);
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
     * @param expiredAt 过期时间戳
     */
    public void saveTokens(String accessToken, String refreshToken, long expiredAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String expireTime = sdf.format(new Date(expiredAt));

        Log.d(TAG, "🔐 保存令牌信息 - " +
                "访问令牌长度: " + (accessToken != null ? accessToken.length() : 0) +
                ", 刷新令牌长度: " + (refreshToken != null ? refreshToken.length() : 0) +
                ", 过期时间: " + expireTime);

        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putLong(KEY_TOKEN_EXPIRED_AT, expiredAt);
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
        long expiredAt = getTokenExpiredAt();
        long currentTime = System.currentTimeMillis();
        boolean isValid = !token.isEmpty() && currentTime < expiredAt;

        Log.d(TAG, "🔍 令牌有效性检查 - " +
                "令牌存在: " + !token.isEmpty() +
                ", 未过期: " + (currentTime < expiredAt) +
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
}