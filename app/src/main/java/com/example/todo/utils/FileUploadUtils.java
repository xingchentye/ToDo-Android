// FileUploadUtils.java - 修复版本
package com.example.todo.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件上传工具类
 */
public class FileUploadUtils {
    private static final String TAG = "📁 文件上传工具";

    /**
     * 将Uri转换为MultipartBody.Part
     */
    public static MultipartBody.Part uriToMultipartPart(Context context, Uri uri, String partName) {
        Log.d(TAG, "🔄 转换Uri为MultipartPart: " + uri.toString());

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "❌ 无法从Uri获取输入流");
                return null;
            }

            // 创建临时文件
            File tempFile = createTempFileFromInputStream(context, inputStream, "upload_" + System.currentTimeMillis() + ".jpg");
            if (tempFile == null) {
                Log.e(TAG, "❌ 创建临时文件失败");
                return null;
            }

            // 检查文件大小（限制5MB）
            if (!isFileSizeValid(tempFile, 5)) {
                Log.e(TAG, "❌ 文件大小超过限制");
                tempFile.delete();
                return null;
            }

            // 创建请求体
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);

            // 创建MultipartBody.Part
            MultipartBody.Part part = MultipartBody.Part.createFormData(partName, tempFile.getName(), requestBody);

            Log.d(TAG, "✅ 文件转换成功: " + tempFile.getName() + ", 大小: " + tempFile.length() + " bytes");
            return part;

        } catch (Exception e) {
            Log.e(TAG, "💥 文件转换异常: " + e.getMessage());
            return null;
        }
    }

    private static File createTempFileFromInputStream(Context context, InputStream inputStream, String fileName) {
        File tempFile = null;
        FileOutputStream outputStream = null;

        try {
            tempFile = new File(context.getCacheDir(), fileName);
            outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            Log.d(TAG, "✅ 临时文件创建成功: " + tempFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "💥 创建临时文件失败: " + e.getMessage());
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            tempFile = null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "💥 关闭流失败: " + e.getMessage());
            }
        }

        return tempFile;
    }

    private static boolean isFileSizeValid(File file, int maxSizeMB) {
        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        boolean isValid = file.length() <= maxSizeBytes;

        Log.d(TAG, "📏 文件大小检查 - 文件: " + file.length() + " bytes, 限制: " + maxSizeBytes + " bytes, 结果: " + (isValid ? "✅ 有效" : "❌ 过大"));
        return isValid;
    }
}