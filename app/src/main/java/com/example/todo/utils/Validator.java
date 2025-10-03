package com.example.todo.utils;

import android.text.TextUtils;
import android.util.Log;
import java.util.regex.Pattern;

/**
 * 输入验证工具类
 * 提供各种输入验证功能
 */
public class Validator {
    private static final String TAG = "🔍 输入验证器"; // 日志标签

    // 正则表达式模式
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$");

    /**
     * 验证用户名格式
     * @param username 用户名
     * @return true-有效, false-无效
     */
    public static boolean isValidUsername(String username) {
        boolean isValid = !TextUtils.isEmpty(username) && USERNAME_PATTERN.matcher(username).matches();

        Log.d(TAG, "👤 用户名验证 - " +
                "输入: " + (username != null ? "'" + username + "'" : "null") +
                ", 结果: " + (isValid ? "✅ 有效" : "❌ 无效"));

        return isValid;
    }

    /**
     * 验证昵称格式
     * @param nickname 昵称
     * @return true-有效, false-无效
     */
    public static boolean isValidNickname(String nickname) {
        boolean isValid = !TextUtils.isEmpty(nickname) && nickname.length() >= 2 && nickname.length() <= 20;

        Log.d(TAG, "👤 昵称验证 - " +
                "输入: " + (nickname != null ? "'" + nickname + "'" : "null") +
                ", 长度: " + (nickname != null ? nickname.length() : 0) +
                ", 结果: " + (isValid ? "✅ 有效" : "❌ 无效"));

        return isValid;
    }

    /**
     * 验证邮箱格式
     * @param email 邮箱
     * @return true-有效, false-无效
     */
    public static boolean isValidEmail(String email) {
        boolean isValid = !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();

        Log.d(TAG, "📧 邮箱验证 - " +
                "输入: " + (email != null ? "'" + email + "'" : "null") +
                ", 结果: " + (isValid ? "✅ 有效" : "❌ 无效"));

        return isValid;
    }

    /**
     * 验证密码格式
     * @param password 密码
     * @return true-有效, false-无效
     */
    public static boolean isValidPassword(String password) {
        boolean isValid = !TextUtils.isEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();

        Log.d(TAG, "🔒 密码验证 - " +
                "长度: " + (password != null ? password.length() : 0) +
                ", 结果: " + (isValid ? "✅ 有效" : "❌ 无效"));

        return isValid;
    }

    /**
     * 获取用户名验证提示
     * @return 提示信息
     */
    public static String getUsernameHint() {
        return "用户名需3-20位字母、数字或下划线";
    }

    /**
     * 获取昵称验证提示
     * @return 提示信息
     */
    public static String getNicknameHint() {
        return "昵称用于显示，2-20个字符";
    }

    /**
     * 获取邮箱验证提示
     * @return 提示信息
     */
    public static String getEmailHint() {
        return "请输入有效的邮箱地址";
    }

    /**
     * 获取密码验证提示
     * @return 提示信息
     */
    public static String getPasswordHint() {
        return "密码需6-20位字符，包含字母和数字";
    }
}