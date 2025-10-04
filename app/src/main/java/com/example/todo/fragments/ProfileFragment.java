// ProfileFragment.java - 修复导航切换问题
package com.example.todo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.todo.EditProfileActivity;
import com.example.todo.ChangePasswordActivity;
import com.example.todo.R;
import com.example.todo.databinding.FragmentProfileBinding;
import com.example.todo.model.User;
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
 * 个人中心Fragment - 修复版本
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "👤 个人中心Fragment";

    private FragmentProfileBinding binding;
    private SharedPreferencesManager spManager;
    private ApiService apiService;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🎬 Fragment创建视图开始");

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initComponents();
        loadUserInfo();
        setupClickListeners();

        Log.d(TAG, "✅ 个人中心Fragment初始化完成");
        return view;
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        spManager = new SharedPreferencesManager(requireContext());
        apiService = RetrofitClient.getApiService();

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void loadUserInfo() {
        Log.d(TAG, "📥 加载用户信息");

        // 显示加载状态
        showLoading(true);

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Log.w(TAG, "❌ 网络不可用，显示本地数据");
            showLocalUserInfo();
            return;
        }

        // 调用API获取用户信息
        apiService.getCurrentUser().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);

                ApiResponse<User> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    currentUser = processedResponse.getData();
                    displayUserInfo(currentUser);
                    Log.d(TAG, "✅ 用户信息加载成功");
                } else {
                    Log.w(TAG, "⚠️ 用户信息加载失败: " + processedResponse.getMessage());
                    showLocalUserInfo();
                    Toast.makeText(requireContext(), "加载失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "💥 用户信息网络请求失败: " + t.getMessage());
                showLocalUserInfo();
                Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLocalUserInfo() {
        Log.d(TAG, "📋 显示本地用户信息");

        // 从本地存储获取用户名显示
        String username = spManager.getSavedUsername();
        if (!username.isEmpty()) {
            User localUser = new User();
            localUser.setUsername(username);
            localUser.setNickName(username);
            localUser.setEmail("点击刷新获取完整信息");
            displayUserInfo(localUser);
        }

        showLoading(false);
    }

    private void displayUserInfo(User user) {
        Log.d(TAG, "🖼️ 显示用户信息: " + user.getUsername());

        binding.textUsername.setText(user.getUsername());
        binding.textNickname.setText(user.getNickName());

        // 邮箱显示处理：如果邮箱包含星号，添加提示
        String email = user.getEmail();
        if (email != null && email.contains("*")) {
            binding.textEmail.setText(email);
            binding.textEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_info, 0);
            binding.textEmail.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "掩码邮箱，编辑资料时可更新", Toast.LENGTH_SHORT).show();
            });
        } else {
            binding.textEmail.setText(email);
            binding.textEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            binding.textEmail.setOnClickListener(null);
        }

        if (user.getCreateTime() != null) {
            String joinDate = formatJoinDate(user.getCreateTime());
            binding.textJoinDate.setText("注册时间：" + joinDate);
        } else {
            binding.textJoinDate.setText("注册时间：未知");
        }

        // 加载头像
        String avatarUrl = user.getAvatar(); // 确保这是从API返回的完整URL
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            // 使用Glide加载头像
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile) // 默认头像
                    .error(R.drawable.ic_profile)       // 加载失败时显示的头像
                    .into(binding.imageAvatar);
        } else {
            // 如果头像URL为空，显示默认头像
            binding.imageAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private String formatJoinDate(String createTime) {
        try {
            // 假设createTime格式为 "2024-01-15T10:30:00"
            String[] parts = createTime.split("T");
            if (parts.length > 0) {
                return parts[0];
            }
            return createTime;
        } catch (Exception e) {
            Log.w(TAG, "📅 注册时间格式解析失败: " + createTime);
            return createTime;
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            binding.textUsername.setText("加载中...");
            binding.textNickname.setText("");
            binding.textEmail.setText("");
            binding.textJoinDate.setText("");
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "🔄 设置点击监听器");

        // 编辑资料
        binding.cardEditProfile.setOnClickListener(v -> {
            Log.d(TAG, "👆 编辑资料被点击");
            if (currentUser != null) {
                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                intent.putExtra("user", currentUser);
                startActivityForResult(intent, 1001);
            } else {
                Toast.makeText(requireContext(), "请先加载用户信息", Toast.LENGTH_SHORT).show();
                loadUserInfo();
            }
        });

        // 修改密码
        binding.cardChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "👆 修改密码被点击");
            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // 刷新数据
        binding.cardRefresh.setOnClickListener(v -> {
            Log.d(TAG, "👆 刷新数据被点击");
            loadUserInfo();
            Toast.makeText(requireContext(), "刷新中...", Toast.LENGTH_SHORT).show();
        });

        // 退出登录
        binding.cardLogout.setOnClickListener(v -> {
            Log.d(TAG, "👆 退出登录被点击");
            performLogout();
        });

        Log.d(TAG, "✅ 点击监听器设置完成");
    }

    private void performLogout() {
        Log.d(TAG, "🚪 执行退出登录");

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络不可用，本地退出", Toast.LENGTH_SHORT).show();
            localLogout();
            return;
        }

        // 显示加载状态
        binding.cardLogout.setEnabled(false);

        // 调用退出登录API
        apiService.logout().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Log.d(TAG, "✅ 退出登录API调用成功");
                localLogout();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "💥 退出登录API调用失败: " + t.getMessage());
                // 即使API调用失败，也执行本地退出
                localLogout();
            }
        });
    }

    private void localLogout() {
        // 清除本地数据
        spManager.clearAll();

        // 跳转到登录页面
        Intent intent = new Intent(requireContext(), com.example.todo.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(requireContext(), "👋 已退出登录", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ 本地退出登录完成");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == requireActivity().RESULT_OK) {
            // 编辑资料成功后刷新数据
            Log.d(TAG, "🔄 编辑资料成功，刷新用户信息");
            loadUserInfo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment恢复，重新加载数据");
        loadUserInfo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "💀 Fragment视图被销毁");
    }

    // 公开方法供Activity调用
    public void refreshProfile() {
        loadUserInfo();
    }
}