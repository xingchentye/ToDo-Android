package com.example.todo.storage;

import android.content.Context;
import android.util.Log;
import com.example.todo.model.dto.UserRefreshTokenDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.model.vo.UserRefreshTokenVO;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 令牌管理器
 * 负责令牌的自动刷新和管理
 */
public class TokenManager {
    private static final String TAG = "🔑 令牌管理器"; // 日志标签

    private SharedPreferencesManager spManager;
    private ApiService apiService;
    private TokenRefreshListener refreshListener;
    private boolean isRefreshing = false;

    /**
     * 令牌刷新监听器接口
     */
    public interface TokenRefreshListener {
        void onTokenRefreshed(String newAccessToken);
        void onTokenRefreshFailed(String errorMessage);
    }

    /**
     * 构造函数
     * @param context 上下文对象
     */
    public TokenManager(Context context) {
        Log.d(TAG, "🔄 初始化令牌管理器");
        this.spManager = new SharedPreferencesManager(context);
        this.apiService = RetrofitClient.getApiService();
        Log.d(TAG, "✅ 令牌管理器初始化完成");
    }

    /**
     * 设置令牌刷新监听器
     * @param listener 监听器
     */
    public void setTokenRefreshListener(TokenRefreshListener listener) {
        this.refreshListener = listener;
        Log.d(TAG, "🎯 设置令牌刷新监听器");
    }

    /**
     * 检查并刷新令牌（如果需要）
     * @return true-令牌有效或正在刷新, false-令牌无效且无法刷新
     */
    public boolean checkAndRefreshTokenIfNeeded() {
        Log.d(TAG, "🔍 检查令牌状态...");

        if (spManager.isTokenValid()) {
            Log.d(TAG, "✅ 访问令牌有效，无需刷新");
            return true;
        }

        String refreshToken = spManager.getRefreshToken();
        if (refreshToken.isEmpty()) {
            Log.w(TAG, "❌ 刷新令牌不存在，无法自动刷新");
            return false;
        }

        Log.d(TAG, "🔄 访问令牌已过期，尝试使用刷新令牌自动刷新");
        refreshAccessToken(refreshToken);
        return true; // 返回true表示正在尝试刷新
    }

    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     */
    private void refreshAccessToken(String refreshToken) {
        if (isRefreshing) {
            Log.d(TAG, "⏳ 令牌刷新正在进行中，跳过重复请求");
            return;
        }

        Log.d(TAG, "🔄 开始刷新访问令牌...");
        isRefreshing = true;

        UserRefreshTokenDTO refreshTokenDTO = new UserRefreshTokenDTO(refreshToken);
        Log.d(TAG, "📦 创建刷新令牌请求: " + refreshTokenDTO.toString());

        apiService.refreshToken(refreshTokenDTO).enqueue(new Callback<ApiResponse<UserRefreshTokenVO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserRefreshTokenVO>> call,
                                   Response<ApiResponse<UserRefreshTokenVO>> response) {
                isRefreshing = false;
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ 刷新令牌API请求成功");
                    handleRefreshTokenResponse(response.body());
                } else {
                    Log.e(TAG, "❌ 刷新令牌API响应异常");
                    handleRefreshTokenError("刷新令牌失败，请重新登录");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserRefreshTokenVO>> call, Throwable t) {
                isRefreshing = false;
                Log.e(TAG, "💥 刷新令牌网络请求失败: " + t.getMessage());
                handleRefreshTokenError("网络错误，无法刷新令牌");
            }
        });
    }

    /**
     * 处理刷新令牌响应
     * @param response API响应数据
     */
    private void handleRefreshTokenResponse(ApiResponse<UserRefreshTokenVO> response) {
        Log.d(TAG, "🔧 开始处理刷新令牌响应");

        if (response.isSuccess() && response.getData() != null) {
            Log.d(TAG, "🎉 刷新令牌成功");

            UserRefreshTokenVO refreshVO = response.getData();

            // 保存新的访问令牌
            spManager.saveTokens(
                    refreshVO.getAccessToken(),
                    spManager.getRefreshToken(), // 保持原刷新令牌不变
                    refreshVO.getAccessTokenExpiredAt()
            );

            Log.d(TAG, "✅ 新访问令牌已保存");

            // 通知监听器
            if (refreshListener != null) {
                refreshListener.onTokenRefreshed(refreshVO.getAccessToken());
            }
        } else {
            Log.e(TAG, "❌ 刷新令牌业务失败: " + response.getMessage());
            handleRefreshTokenError("刷新令牌失败: " + response.getMessage());
        }
    }

    /**
     * 处理刷新令牌错误
     * @param errorMessage 错误信息
     */
    private void handleRefreshTokenError(String errorMessage) {
        Log.e(TAG, "💥 处理刷新令牌错误: " + errorMessage);

        // 清除所有令牌（包括刷新令牌）
        spManager.clearTokens();

        // 通知监听器
        if (refreshListener != null) {
            refreshListener.onTokenRefreshFailed(errorMessage);
        }
    }

    /**
     * 获取当前访问令牌（如果过期会自动刷新）
     * @return 访问令牌，如果无法获取返回空字符串
     */
    public String getValidAccessToken() {
        if (spManager.isTokenValid()) {
            return spManager.getAccessToken();
        }

        // 如果令牌过期但正在刷新，返回空字符串，调用方应该等待刷新完成
        return "";
    }

    /**
     * 强制刷新令牌
     */
    public void forceRefreshToken() {
        String refreshToken = spManager.getRefreshToken();
        if (!refreshToken.isEmpty()) {
            refreshAccessToken(refreshToken);
        } else {
            Log.w(TAG, "⚠️ 无法强制刷新，刷新令牌不存在");
        }
    }

    /**
     * 清除所有令牌
     */
    public void clearAllTokens() {
        Log.d(TAG, "🗑️ 清除所有令牌");
        spManager.clearTokens();
    }

    /**
     * 检查是否正在进行令牌刷新
     * @return true-正在刷新, false-未在刷新
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }
}