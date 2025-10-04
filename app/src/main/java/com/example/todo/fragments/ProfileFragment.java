package com.example.todo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
 * 个人中心Fragment
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "👤 个人中心Fragment";

    private FragmentProfileBinding binding;
    private SharedPreferencesManager spManager;
    private ApiService apiService;
    private User currentUser;
    private boolean isViewDestroyed = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Fragment创建开始");

        // 初始化Retrofit
        RetrofitClient.init(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🎬 Fragment创建视图开始");

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        isViewDestroyed = false;

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

        // 检查视图状态
        if (isViewDestroyed || binding == null) {
            Log.w(TAG, "⚠️ 视图已销毁，跳过加载用户信息");
            return;
        }

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
                // 检查Fragment状态
                if (isViewDestroyed || binding == null || getActivity() == null) {
                    Log.w(TAG, "⚠️ Fragment状态无效，跳过处理响应");
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);

                    ApiResponse<User> processedResponse = ApiResponseHandler.processResponse(response);

                    if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                        currentUser = processedResponse.getData();
                        displayUserInfo(currentUser);
                        Log.d(TAG, "✅ 用户信息加载成功");
                    } else {
                        Log.w(TAG, "⚠️ 用户信息加载失败: " + processedResponse.getMessage());
                        showLocalUserInfo();
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // 检查Fragment状态
                if (isViewDestroyed || binding == null || getActivity() == null) {
                    Log.w(TAG, "⚠️ Fragment状态无效，跳过处理错误");
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "💥 用户信息网络请求失败: " + t.getMessage());
                    showLocalUserInfo();
                });
            }
        });
    }

    private void showLocalUserInfo() {
        Log.d(TAG, "📋 显示本地用户信息");

        // 检查视图状态
        if (isViewDestroyed || binding == null) {
            Log.w(TAG, "⚠️ 视图已销毁，跳过显示本地信息");
            return;
        }

        // 从本地存储获取用户名显示
        String username = spManager.getSavedUsername();
        if (!username.isEmpty()) {
            User localUser = new User();
            localUser.setUsername(username);
            localUser.setNickName(username); // 使用用户名作为昵称
            localUser.setEmail("点击刷新获取完整信息");
            displayUserInfo(localUser);
        }

        showLoading(false);
    }

    private void displayUserInfo(User user) {
        // 重要：添加空检查
        if (binding == null || isViewDestroyed) {
            Log.w(TAG, "⚠️ Binding为空或视图已销毁，跳过显示用户信息");
            return;
        }

        try {
            Log.d(TAG, "🖼️ 显示用户信息: " + user.getUsername());

            binding.textUsername.setText(user.getUsername());
            binding.textNickname.setText(user.getNickName());
            binding.textEmail.setText(user.getEmail());

            if (user.getCreateTime() != null) {
                String joinDate = formatJoinDate(user.getCreateTime());
                binding.textJoinDate.setText("注册时间：" + joinDate);
            } else {
                binding.textJoinDate.setText("注册时间：未知");
            }

            // 设置头像（如果有）
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                // 使用Glide或Picasso加载头像
                // Glide.with(this).load(user.getAvatar()).into(binding.imageAvatar);
            }
        } catch (Exception e) {
            Log.e(TAG, "💥 显示用户信息时发生异常: " + e.getMessage());
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
        // 检查视图状态
        if (binding == null || isViewDestroyed) {
            return;
        }

        // 这里可以显示或隐藏加载指示器
        if (show) {
            binding.textUsername.setText("加载中...");
            binding.textNickname.setText("");
            binding.textEmail.setText("");
            binding.textJoinDate.setText("");
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "🔄 设置点击监听器");

        if (binding == null) {
            Log.w(TAG, "⚠️ Binding为空，跳过设置点击监听器");
            return;
        }

        // 编辑资料
        binding.cardEditProfile.setOnClickListener(v -> {
            Log.d(TAG, "👆 编辑资料被点击");
            if (getActivity() == null) return;

            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            if (currentUser != null) {
                intent.putExtra("user", currentUser);
            }
            startActivity(intent);
        });

        // 修改密码
        binding.cardChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "👆 修改密码被点击");
            if (getActivity() == null) return;

            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // 刷新数据
        binding.cardRefresh.setOnClickListener(v -> {
            Log.d(TAG, "👆 刷新数据被点击");
            loadUserInfo();
            Toast.makeText(requireContext(), "数据刷新中...", Toast.LENGTH_SHORT).show();
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
        if (getActivity() == null) return;

        Intent intent = new Intent(requireContext(), com.example.todo.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(requireContext(), "👋 已退出登录", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ 本地退出登录完成");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment恢复，重新加载数据");
        loadUserInfo(); // 重新加载用户信息
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "💀 Fragment视图被销毁");
        isViewDestroyed = true;
        binding = null;
    }

    // 公开方法供Activity调用
    public void refreshProfile() {
        loadUserInfo();
    }
}