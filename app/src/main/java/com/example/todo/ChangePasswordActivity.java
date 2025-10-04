package com.example.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todo.databinding.ActivityChangePasswordBinding;
import com.example.todo.model.dto.UserChangePasswordDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.NetworkUtils;
import com.example.todo.utils.Validator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 修改密码Activity
 */
public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "🔐 修改密码Activity";

    private ActivityChangePasswordBinding binding;
    private ApiService apiService;
    private SharedPreferencesManager spManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "✅ 修改密码布局加载完成");

        setTitle("修改密码");

        initComponents();

        Log.d(TAG, "🎉 修改密码Activity初始化完成");
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        // 初始化管理器
        spManager = new SharedPreferencesManager(this);
        apiService = RetrofitClient.getApiService();

        // 设置按钮点击事件
        binding.buttonCancel.setOnClickListener(v -> {
            Log.d(TAG, "❌ 取消按钮被点击");
            finish();
        });

        binding.buttonSave.setOnClickListener(v -> {
            Log.d(TAG, "💾 保存按钮被点击");
            changePassword();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void changePassword() {
        Log.d(TAG, "🔐 开始修改密码");

        // 验证输入
        String oldPassword = binding.editTextOldPassword.getText().toString().trim();
        if (oldPassword.isEmpty()) {
            binding.editTextOldPassword.setError("请输入旧密码");
            Log.w(TAG, "❌ 旧密码为空");
            return;
        }

        String newPassword = binding.editTextNewPassword.getText().toString().trim();
        if (newPassword.isEmpty()) {
            binding.editTextNewPassword.setError("请输入新密码");
            Log.w(TAG, "❌ 新密码为空");
            return;
        }

        if (!Validator.isValidPassword(newPassword)) {
            binding.editTextNewPassword.setError("密码需6-20位字符，包含字母和数字");
            Log.w(TAG, "❌ 新密码格式无效");
            return;
        }

        String confirmPassword = binding.editTextConfirmPassword.getText().toString().trim();
        if (confirmPassword.isEmpty()) {
            binding.editTextConfirmPassword.setError("请确认新密码");
            Log.w(TAG, "❌ 确认密码为空");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.editTextConfirmPassword.setError("两次输入的密码不一致");
            Log.w(TAG, "❌ 密码不一致");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "❌ 网络不可用，无法修改密码");
            return;
        }

        // 创建修改密码DTO
        UserChangePasswordDTO changePasswordDTO = new UserChangePasswordDTO(oldPassword, newPassword);

        Log.d(TAG, "📦 创建修改密码数据");

        performChangePassword(changePasswordDTO);
    }

    private void performChangePassword(UserChangePasswordDTO changePasswordDTO) {
        Log.d(TAG, "🔐 执行修改密码");

        showLoading(true);

        apiService.changePassword(changePasswordDTO).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);

                ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess()) {
                    Log.d(TAG, "✅ 密码修改成功");
                    Toast.makeText(ChangePasswordActivity.this, "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();

                    // 清除所有用户数据
                    spManager.clearAll();

                    // 跳转到登录页面
                    Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Log.w(TAG, "⚠️ 密码修改失败: " + processedResponse.getMessage());
                    Toast.makeText(ChangePasswordActivity.this, "修改失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "💥 密码修改网络请求失败: " + t.getMessage());
                Toast.makeText(ChangePasswordActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        binding.buttonSave.setEnabled(!show);
        binding.buttonSave.setText(show ? "保存中..." : "保存");

        if (show) {
            binding.buttonSave.setIcon(null);
        } else {
            binding.buttonSave.setIconResource(R.drawable.ic_save);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.d(TAG, "💀 Activity被销毁");
    }
}