// EditProfileActivity.java - 修复头像显示
package com.example.todo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.todo.databinding.ActivityEditProfileBinding;
import com.example.todo.model.User;
import com.example.todo.model.dto.UserProfileUpdateDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.model.vo.FileUploadVO;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.FileUploadUtils;
import com.example.todo.utils.NetworkUtils;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 编辑资料Activity - 修复头像显示
 */
public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "✏️ 编辑资料Activity";

    private ActivityEditProfileBinding binding;
    private ApiService apiService;
    private SharedPreferencesManager spManager;
    private User currentUser;

    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;
    private Integer uploadedAvatarFileId = null;

    // 标记是否正在上传头像
    private boolean isUploadingAvatar = false;

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

            // 邮箱处理：如果邮箱包含星号（掩码），则清空输入框，让用户重新输入
            String email = currentUser.getEmail();
            if (email != null && email.contains("*")) {
                Log.d(TAG, "📧 检测到掩码邮箱，清空输入框");
                binding.editTextEmail.setText("");
                binding.editTextEmail.setHint("请输入完整邮箱地址");
            } else {
                binding.editTextEmail.setText(email);
            }

            // 加载当前头像 - 修复版本
            loadCurrentAvatar();

            Log.d(TAG, "✅ 表单数据填充完成");
        }
    }

    /**
     * 加载当前头像
     */
    private void loadCurrentAvatar() {
        Log.d(TAG, "🖼️ 加载当前头像");

        String avatarUrl = currentUser.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Log.d(TAG, "🖼️ 加载头像URL: " + avatarUrl);
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.imageAvatar);
        } else {
            Log.d(TAG, "🖼️ 使用默认头像");
            binding.imageAvatar.setImageResource(R.drawable.ic_profile);
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

        // 邮箱格式验证
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("请输入有效的邮箱地址");
            Log.w(TAG, "❌ 邮箱格式无效");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "❌ 网络不可用，无法保存资料");
            return;
        }

        // 如果有新选择的头像且尚未上传，先上传头像
        if (selectedImageUri != null && uploadedAvatarFileId == null && !isUploadingAvatar) {
            Log.d(TAG, "📤 检测到新头像，先上传头像");
            uploadImage(selectedImageUri);
        } else {
            // 直接更新资料
            updateProfileData(username, nickname, email);
        }
    }

    private void uploadImage(Uri imageUri) {
        Log.d(TAG, "🔄 开始上传头像");

        isUploadingAvatar = true;
        showLoading(true, "上传头像中...");

        // 转换Uri为MultipartPart
        MultipartBody.Part filePart = FileUploadUtils.uriToMultipartPart(this, imageUri, "file");
        if (filePart == null) {
            Log.e(TAG, "❌ 文件转换失败");
            isUploadingAvatar = false;
            showLoading(false, "保存");
            Toast.makeText(this, "文件处理失败，请选择其他图片", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.uploadFile(filePart).enqueue(new Callback<ApiResponse<FileUploadVO>>() {
            @Override
            public void onResponse(Call<ApiResponse<FileUploadVO>> call, Response<ApiResponse<FileUploadVO>> response) {
                isUploadingAvatar = false;
                ApiResponse<FileUploadVO> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    FileUploadVO uploadVO = processedResponse.getData();
                    uploadedAvatarFileId = uploadVO.getFileId();

                    Log.d(TAG, "✅ 头像上传成功，文件ID: " + uploadedAvatarFileId);

                    // 头像上传成功后更新资料
                    String username = binding.editTextUsername.getText().toString().trim();
                    String nickname = binding.editTextNickname.getText().toString().trim();
                    String email = binding.editTextEmail.getText().toString().trim();
                    updateProfileData(username, nickname, email);

                } else {
                    showLoading(false, "保存");
                    Log.w(TAG, "⚠️ 头像上传失败: " + processedResponse.getMessage());
                    Toast.makeText(EditProfileActivity.this, "头像上传失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FileUploadVO>> call, Throwable t) {
                isUploadingAvatar = false;
                showLoading(false, "保存");
                Log.e(TAG, "💥 头像上传网络请求失败: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "头像上传失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileData(String username, String nickname, String email) {
        Log.d(TAG, "✏️ 更新用户资料");

        showLoading(true, "保存中...");

        // 创建更新DTO
        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO(username, nickname, email);

        // 如果有上传的头像文件ID，设置avatarFileId
        if (uploadedAvatarFileId != null) {
            updateDTO.setAvatarFileId(uploadedAvatarFileId);
            Log.d(TAG, "📦 设置头像文件ID: " + uploadedAvatarFileId);
        }

        Log.d(TAG, "📦 创建资料更新数据: " + updateDTO.toString());

        apiService.updateUser(updateDTO).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false, "保存");

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
                showLoading(false, "保存");
                Log.e(TAG, "💥 资料更新网络请求失败: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show, String text) {
        binding.buttonSave.setEnabled(!show);
        binding.buttonSave.setText(show ? text : "保存");

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

            // 立即显示选中的图片
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.imageAvatar);

            // 重置上传的文件ID，表示有新图片需要上传
            uploadedAvatarFileId = null;

            Log.d(TAG, "🖼️ 图片选择成功: " + selectedImageUri.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.d(TAG, "💀 Activity被销毁");
    }
}