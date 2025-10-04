package com.example.todo.utils;

import android.util.Log;
import com.example.todo.model.response.ApiResponse;
import retrofit2.Response;
import java.io.IOException;

/**
 * API响应处理工具类
 * 统一处理网络请求的响应和错误
 */
public class ApiResponseHandler {
    private static final String TAG = "🔄 响应处理器"; // 日志标签

    /**
     * 处理API响应
     * @param response Retrofit响应对象
     * @param <T> 数据类型
     * @return 处理后的ApiResponse对象
     */
    public static <T> ApiResponse<T> handleResponse(Response<ApiResponse<T>> response) {
        Log.d(TAG, "🔧 开始处理API响应 - 状态码: " + response.code());

        // 如果响应体为空
        if (response.body() == null) {
            Log.w(TAG, "⚠️ 响应体为空");
            return createErrorResponse("服务器响应为空", response.code());
        }

        ApiResponse<T> apiResponse = response.body();

        // 记录响应详情
        Log.d(TAG, "📋 响应详情 - " +
                "业务状态码: " + apiResponse.getCode() +
                ", 消息: " + apiResponse.getMessage() +
                ", 请求ID: " + apiResponse.getRequestId() +
                ", 数据: " + (apiResponse.getData() != null ? "存在" : "空"));

        return apiResponse;
    }

    /**
     * 处理HTTP错误响应
     * @param response Retrofit响应对象
     * @param <T> 数据类型
     * @return 处理后的ApiResponse对象
     */
    public static <T> ApiResponse<T> handleErrorResponse(Response<ApiResponse<T>> response) {
        Log.w(TAG, "🚨 处理HTTP错误响应 - 状态码: " + response.code());

        String errorMessage = "请求失败，请稍后重试";

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.d(TAG, "📄 错误响应体: " + errorBody);

                // 尝试从错误体中提取message字段
                if (errorBody.contains("\"message\"")) {
                    int messageStart = errorBody.indexOf("\"message\":\"") + 11;
                    int messageEnd = errorBody.indexOf("\"", messageStart);
                    if (messageStart > 10 && messageEnd > messageStart) {
                        String extractedMessage = errorBody.substring(messageStart, messageEnd);
                        errorMessage = Validator.extractErrorMessage(extractedMessage);
                        Log.d(TAG, "📝 从错误体提取的消息: " + extractedMessage + " -> " + errorMessage);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "💥 读取错误响应体异常: " + e.getMessage());
        }

        // 根据HTTP状态码提供更具体的错误信息
        errorMessage = enhanceErrorMessage(errorMessage, response.code());

        return createErrorResponse(errorMessage, response.code());
    }

    /**
     * 根据HTTP状态码增强错误信息
     * @param originalMessage 原始错误信息
     * @param httpStatusCode HTTP状态码
     * @return 增强后的错误信息
     */
    private static String enhanceErrorMessage(String originalMessage, int httpStatusCode) {
        String enhancedMessage = originalMessage;

        switch (httpStatusCode) {
            case 400:
                enhancedMessage = "请求参数错误：" + originalMessage;
                break;
            case 401:
                enhancedMessage = "身份验证失败：" + originalMessage;
                break;
            case 403:
                enhancedMessage = "访问被拒绝：" + originalMessage;
                break;
            case 404:
                enhancedMessage = "请求的资源不存在：" + originalMessage;
                break;
            case 422:
                enhancedMessage = "数据验证失败：" + originalMessage;
                break;
            case 429:
                enhancedMessage = "请求过于频繁，请稍后重试";
                break;
            case 500:
                enhancedMessage = "服务端出现未知异常! 请稍后再试或提交反馈至开发组!";
                break;
            case 502:
            case 503:
            case 504:
                enhancedMessage = "服务暂时不可用，请稍后重试";
                break;
        }

        Log.d(TAG, "✨ 错误信息增强 - " +
                "原始: '" + originalMessage + "'" +
                ", 增强后: '" + enhancedMessage + "'" +
                ", 状态码: " + httpStatusCode);

        return enhancedMessage;
    }

    /**
     * 创建错误响应对象
     * @param message 错误信息
     * @param code 状态码
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    private static <T> ApiResponse<T> createErrorResponse(String message, int code) {
        ApiResponse<T> errorResponse = new ApiResponse<>();
        errorResponse.setCode(code);
        errorResponse.setMessage(message);
        errorResponse.setRequestId(""); // 空请求ID

        Log.d(TAG, "📦 创建错误响应 - " +
                "消息: '" + message + "'" +
                ", 状态码: " + code);

        return errorResponse;
    }

    /**
     * 统一处理网络请求（推荐使用）
     * @param response Retrofit响应对象
     * @param <T> 数据类型
     * @return 处理后的ApiResponse对象
     */
    public static <T> ApiResponse<T> processResponse(Response<ApiResponse<T>> response) {
        if (response.isSuccessful() && response.body() != null) {
            return handleResponse(response);
        } else {
            return handleErrorResponse(response);
        }
    }
}