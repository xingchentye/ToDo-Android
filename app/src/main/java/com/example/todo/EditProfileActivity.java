package com.example.todo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todo.databinding.ActivityEditProfileBinding;
import com.example.todo.model.User;
import com.example.todo.model.dto.UserProfileUpdateDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 编辑资料Activity
 */
public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "✏️ 编辑资料Activity";

    private ActivityEditProfileBinding binding;
    private ApiService apiService;
    private SharedPreferencesManager spManager;
    private User currentUser;

    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "✅ 编辑资料布局加载完成");

        setTitle("编辑资料");

        // 获取传递的用户信息
        if (getIntent().hasExtra("user")) {
            currentUser = (User) getIntent().getSerializableExtra("user");
            Log.d(TAG, "👤 接收到用户数据: " + currentUser.getUsername());
        }

        initComponents();
        setupFormData();

        Log.d(TAG, "🎉 编辑资料Activity初始化完成");
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
            saveProfile();
        });

        binding.buttonChangeAvatar.setOnClickListener(v -> {
            Log.d(TAG, "🖼️ 更换头像按钮被点击");
            selectImage();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void setupFormData() {
        Log.d(TAG, "🔄 设置表单数据");

        if (currentUser != null) {
            binding.editTextUsername.setText(currentUser.getUsername());
            binding.editTextNickname.setText(currentUser.getNickName());
            binding.editTextEmail.setText(currentUser.getEmail());
            Log.d(TAG, "✅ 表单数据填充完成");
        }
    }

    private void selectImage() {
        Log.d(TAG, "🖼️ 选择图片");

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
    }

    private void saveProfile() {
        Log.d(TAG, "💾 开始保存资料");

        // 验证输入
        String username = binding.editTextUsername.getText().toString().trim();
        if (username.isEmpty()) {
            binding.editTextUsername.setError("请输入用户名");
            Log.w(TAG, "❌ 用户名为空");
            return;
        }

        String nickname = binding.editTextNickname.getText().toString().trim();
        if (nickname.isEmpty()) {
            binding.editTextNickname.setError("请输入昵称");
            Log.w(TAG, "❌ 昵称为空");
            return;
        }

        String email = binding.editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.editTextEmail.setError("请输入邮箱");
            Log.w(TAG, "❌ 邮箱为空");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "❌ 网络不可用，无法保存资料");
            return;
        }

        // 创建更新DTO
        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO(username, nickname, email);
        // 如果有头像文件ID，设置avatarFileId
        // updateDTO.setAvatarFileId(avatarFileId);

        Log.d(TAG, "📦 创建资料更新数据: " + updateDTO.toString());

        updateProfile(updateDTO);
    }

    private void updateProfile(UserProfileUpdateDTO updateDTO) {
        Log.d(TAG, "✏️ 更新用户资料");

        showLoading(true);

        apiService.updateUser(updateDTO).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);

                ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess()) {
                    Log.d(TAG, "✅ 资料更新成功");
                    Toast.makeText(EditProfileActivity.this, "资料更新成功", Toast.LENGTH_SHORT).show();

                    // 更新本地存储的用户名
                    spManager.saveUserCredentials(
                            updateDTO.getUsername(),
                            spManager.getSavedPassword(),
                            spManager.shouldRememberMe()
                    );

                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.w(TAG, "⚠️ 资料更新失败: " + processedResponse.getMessage());
                    Toast.makeText(EditProfileActivity.this, "更新失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "💥 资料更新网络请求失败: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.imageAvatar.setImageURI(selectedImageUri);
            Log.d(TAG, "🖼️ 图片选择成功: " + selectedImageUri.toString());

            // 这里可以上传图片到服务器并获取文件ID
            // uploadImage(selectedImageUri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.d(TAG, "💀 Activity被销毁");
    }
}