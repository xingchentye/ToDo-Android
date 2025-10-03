package com.example.todo.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 防抖工具类
 * 用于防止频繁的网络请求
 */
public class Debouncer {
    private static final String TAG = "⏰ 防抖器"; // 日志标签

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ConcurrentMap<String, Runnable> runnableMap = new ConcurrentHashMap<>();
    private final long delayMillis;

    /**
     * 构造函数
     * @param delayMillis 延迟毫秒数
     */
    public Debouncer(long delayMillis) {
        this.delayMillis = delayMillis;
        Log.d(TAG, "🔄 防抖器初始化，延迟时间: " + delayMillis + "ms");
    }

    /**
     * 提交防抖任务
     * @param key 任务键
     * @param runnable 要执行的任务
     */
    public void debounce(String key, Runnable runnable) {
        Log.d(TAG, "📤 提交防抖任务 - 键: " + key);

        // 移除之前的任务
        Runnable existing = runnableMap.get(key);
        if (existing != null) {
            handler.removeCallbacks(existing);
            Log.d(TAG, "🗑️ 移除之前的任务: " + key);
        }

        // 创建新任务
        Runnable newRunnable = () -> {
            Log.d(TAG, "🎯 执行防抖任务: " + key);
            runnable.run();
            runnableMap.remove(key);
        };

        runnableMap.put(key, newRunnable);
        handler.postDelayed(newRunnable, delayMillis);
        Log.d(TAG, "✅ 新任务已调度，将在 " + delayMillis + "ms 后执行");
    }

    /**
     * 取消指定任务
     * @param key 任务键
     */
    public void cancel(String key) {
        Runnable runnable = runnableMap.get(key);
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnableMap.remove(key);
            Log.d(TAG, "❌ 取消任务: " + key);
        }
    }

    /**
     * 清理所有任务
     */
    public void shutdown() {
        handler.removeCallbacksAndMessages(null);
        runnableMap.clear();
        Log.d(TAG, "🧹 清理所有防抖任务");
    }
}