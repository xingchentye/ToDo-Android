package com.example.todo.network;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.todo.storage.SharedPreferencesManager;
import android.content.Context;
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
    private static SharedPreferencesManager spManager;

    /**
     * 初始化SharedPreferences管理器
     * @param context 上下文对象
     */
    public static void init(Context context) {
        if (spManager == null) {
            spManager = new SharedPreferencesManager(context);
            Log.d(TAG, "✅ SharedPreferences管理器初始化完成");
        }
    }

    /**
     * 获取API服务实例
     * @return ApiService实例
     */
    public static ApiService getApiService() {
        Log.d(TAG, "🔄 开始初始化API服务...");

        if (retrofit == null) {
            Log.d(TAG, "📡 创建新的Retrofit实例，基础URL: " + BASE_URL);

            // 创建日志拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.d(TAG, "📤 网络请求: " + message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 创建认证拦截器 - 自动添加Token到请求头
            okhttp3.Interceptor authInterceptor = chain -> {
                Request originalRequest = chain.request();

                // 检查是否需要添加认证头（排除登录、注册等公开接口）
                if (requiresAuthentication(originalRequest)) {
                    String accessToken = spManager != null ? spManager.getAccessToken() : "";

                    if (!accessToken.isEmpty()) {
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + accessToken)
                                .build();
                        Log.d(TAG, "🔐 添加认证头到请求: " + originalRequest.url());
                        return chain.proceed(newRequest);
                    } else {
                        Log.w(TAG, "⚠️ 访问令牌为空，跳过认证头添加");
                    }
                }

                return chain.proceed(originalRequest);
            };

            // 创建OkHttpClient配置
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(authInterceptor) // 添加认证拦截器
                    .build();

            Log.d(TAG, "✅ OkHttpClient配置完成");

            // 创建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Log.d(TAG, "🎉 Retrofit实例创建成功");
        } else {
            Log.d(TAG, "♻️ 使用现有的Retrofit实例");
        }

        return retrofit.create(ApiService.class);
    }

    /**
     * 检查请求是否需要认证
     * @param request 请求对象
     * @return true-需要认证, false-不需要认证
     */
    private static boolean requiresAuthentication(Request request) {
        String url = request.url().toString();
        String method = request.method();

        // 不需要认证的接口
        if (url.contains("/auth/login") ||
                url.contains("/auth/register") ||
                url.contains("/auth/refresh") ||
                url.contains("/checkUsername") ||
                url.contains("/checkEmail")) {
            return false;
        }

        // 其他所有接口都需要认证
        return true;
    }
}