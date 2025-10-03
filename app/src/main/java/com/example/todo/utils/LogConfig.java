package com.example.todo.utils;

import android.util.Log;

/**
 * 日志配置类
 * 统一管理日志输出格式和级别
 */
public class LogConfig {
    // 日志级别
    public static final int LEVEL_VERBOSE = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_WARN = 3;
    public static final int LEVEL_ERROR = 4;

    // 当前日志级别
    private static int currentLevel = LEVEL_DEBUG;

    /**
     * 设置日志级别
     * @param level 日志级别
     */
    public static void setLogLevel(int level) {
        currentLevel = level;
        Log.d("🔧 日志配置", "📊 日志级别设置为: " + getLevelName(level));
    }

    /**
     * 获取级别名称
     * @param level 级别值
     * @return 级别名称
     */
    private static String getLevelName(int level) {
        switch (level) {
            case LEVEL_VERBOSE: return "VERBOSE";
            case LEVEL_DEBUG: return "DEBUG";
            case LEVEL_INFO: return "INFO";
            case LEVEL_WARN: return "WARN";
            case LEVEL_ERROR: return "ERROR";
            default: return "UNKNOWN";
        }
    }

    // 可以根据需要添加条件日志方法
}