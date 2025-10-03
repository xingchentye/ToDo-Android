package com.example.todo.network;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit网络客户端管理类
 * 单例模式，管理网络请求配置
 */
public class RetrofitClient {
    private static final String TAG = "🌐 RetrofitClient"; // 日志标签

    // 基础URL配置
    private static final String BASE_URL = "https://api.todo.nianhuaci.cn:10443";
    private static Retrofit retrofit = null;

    /**
     * 获取API服务实例
     * @return ApiService实例
     */
    public static ApiService getApiService() {
        Log.d(TAG, "🔄 开始初始化API服务...");

        if (retrofit == null) {
            Log.d(TAG, "📡 创建新的Retrofit实例，基础URL: " + BASE_URL);

            // 创建日志拦截器 - 详细记录网络请求
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.d(TAG, "📤 网络请求: " + message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 创建OkHttpClient配置
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)    // 连接超时30秒
                    .readTimeout(30, TimeUnit.SECONDS)       // 读取超时30秒
                    .writeTimeout(30, TimeUnit.SECONDS)      // 写入超时30秒
                    .addInterceptor(loggingInterceptor)      // 添加日志拦截器
                    .build();

            Log.d(TAG, "✅ OkHttpClient配置完成");

            // 创建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create()) // JSON转换器
                    .build();

            Log.d(TAG, "🎉 Retrofit实例创建成功");
        } else {
            Log.d(TAG, "♻️ 使用现有的Retrofit实例");
        }

        return retrofit.create(ApiService.class);
    }
}