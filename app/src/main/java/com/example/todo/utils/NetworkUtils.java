package com.example.todo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 网络工具类
 * 提供网络状态检查等功能
 */
public class NetworkUtils {
    private static final String TAG = "📶 网络工具"; // 日志标签

    /**
     * 检查网络是否可用
     * @param context 上下文对象
     * @return true-网络可用, false-网络不可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            Log.w(TAG, "⚠️ ConnectivityManager为空");
            return false;
        }

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        Log.d(TAG, "🔍 网络状态检查: " + (isAvailable ? "✅ 网络可用" : "❌ 网络不可用"));

        return isAvailable;
    }
}