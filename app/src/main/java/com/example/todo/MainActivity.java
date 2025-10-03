package com.example.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todo.model.dto.UserLoginDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.model.vo.UserLoginVO;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.utils.NetworkUtils;
import com.example.todo.utils.Validator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 主Activity - 用户登录界面
 * 处理用户登录逻辑和界面交互
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "🚀 主Activity"; // 日志标签

    // 界面组件
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private CheckBox rememberMeCheckbox;
    private Button loginButton;
    private TextView registerHint;
    private TextView versionInfo;

    // 业务组件
    private SharedPreferencesManager spManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        setContentView(R.layout.page_login);
        Log.d(TAG, "✅ 布局文件加载完成");

        // 初始化各个模块
        initViews();
        initDependencies();
        setupClickListeners();

        // 检查是否有传递的凭证
        checkPassedCredentials();

        // 加载保存的用户凭证
        loadSavedCredentials();

        // 检查自动登录
        checkAutoLogin();

        Log.d(TAG, "🎉 Activity初始化完成");
    }

    /**
     * 初始化界面组件
     */
    private void initViews() {
        Log.d(TAG, "🔄 开始初始化界面组件...");

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        loginButton = findViewById(R.id.loginButton);
        registerHint = findViewById(R.id.registerHint);
        versionInfo = findViewById(R.id.versionInfo);

        Log.d(TAG, "✅ 所有界面组件初始化完成");
    }

    /**
     * 初始化依赖组件
     */
    private void initDependencies() {
        Log.d(TAG, "🔄 开始初始化依赖组件...");

        spManager = new SharedPreferencesManager(this);
        apiService = RetrofitClient.getApiService();

        Log.d(TAG, "✅ 依赖组件初始化完成");
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        Log.d(TAG, "🔄 设置点击监听器...");

        // 登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            Log.d(TAG, "👆 登录按钮被点击");
            attemptLogin();
        });

        // 注册提示点击事件
        registerHint.setOnClickListener(v -> {
            Log.d(TAG, "👆 注册提示被点击");
            navigateToRegister();
        });

        // 清除输入框错误状态
        usernameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "👤 用户名输入框获得焦点");
                usernameLayout.setError(null);
            }
        });

        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "🔒 密码输入框获得焦点");
                passwordLayout.setError(null);
            }
        });

        Log.d(TAG, "✅ 所有点击监听器设置完成");
    }

    /**
     * 检查是否有传递的凭证
     */
    private void checkPassedCredentials() {
        Log.d(TAG, "🔍 检查传递的凭证信息...");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String username = extras.getString("username");
            String password = extras.getString("password");

            if (username != null && password != null) {
                Log.d(TAG, "📨 接收到传递的凭证 - 用户名: " + username);

                usernameEditText.setText(username);
                passwordEditText.setText(password);
                rememberMeCheckbox.setChecked(true);

                Log.d(TAG, "✅ 已填充传递的凭证信息");
            }
        } else {
            Log.d(TAG, "ℹ️ 未接收到传递的凭证");
        }
    }

    /**
     * 加载保存的用户凭证
     */
    private void loadSavedCredentials() {
        Log.d(TAG, "🔄 开始加载保存的用户凭证...");

        if (spManager.shouldRememberMe()) {
            String savedUsername = spManager.getSavedUsername();
            String savedPassword = spManager.getSavedPassword();

            usernameEditText.setText(savedUsername);
            passwordEditText.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);

            Log.d(TAG, "✅ 已加载保存的用户凭证 - 用户名: " + savedUsername);
        } else {
            Log.d(TAG, "ℹ️ 未启用记住我功能，跳过加载凭证");
        }
    }

    /**
     * 检查自动登录
     */
    private void checkAutoLogin() {
        Log.d(TAG, "🔄 检查自动登录条件...");

        if (spManager.shouldRememberMe() && spManager.isTokenValid()) {
            Log.d(TAG, "🎯 满足自动登录条件，执行自动登录");
            String savedUsername = spManager.getSavedUsername();
            String savedPassword = spManager.getSavedPassword();

            if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
                Log.d(TAG, "🔑 使用保存的凭证执行自动登录");
                performAutoLogin(savedUsername, savedPassword);
            } else {
                Log.w(TAG, "⚠️ 保存的凭证为空，跳过自动登录");
            }
        } else {
            Log.d(TAG, "ℹ️ 不满足自动登录条件，需要手动登录");
        }
    }

    /**
     * 执行自动登录
     * @param username 用户名
     * @param password 密码
     */
    private void performAutoLogin(String username, String password) {
        Log.d(TAG, "🤖 开始执行自动登录...");

        // 显示自动登录状态
        setLoginButtonState(false, "⏳ 自动登录中...");

        UserLoginDTO loginDTO = new UserLoginDTO(username, password);
        Log.d(TAG, "📦 创建自动登录请求数据: " + loginDTO.toString());

        apiService.login(loginDTO).enqueue(new Callback<ApiResponse<UserLoginVO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserLoginVO>> call,
                                   Response<ApiResponse<UserLoginVO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ 自动登录API请求成功");
                    handleAutoLoginResponse(response.body());
                } else {
                    Log.e(TAG, "❌ 自动登录API响应异常");
                    handleAutoLoginError("自动登录失败，请手动登录");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserLoginVO>> call, Throwable t) {
                Log.e(TAG, "💥 自动登录网络请求失败: " + t.getMessage());
                handleAutoLoginError("自动登录网络错误");
            }
        });
    }

    /**
     * 处理自动登录响应
     * @param response API响应数据
     */
    private void handleAutoLoginResponse(ApiResponse<UserLoginVO> response) {
        Log.d(TAG, "🔧 开始处理自动登录响应");

        if (response.isSuccess() && response.getData() != null) {
            Log.d(TAG, "🎉 自动登录成功");

            UserLoginVO loginVO = response.getData();

            // 保存新的令牌信息
            spManager.saveTokens(
                    loginVO.getAccessToken(),
                    loginVO.getRefreshToken(),
                    loginVO.getAccessTokenExpiredAt()
            );

            Toast.makeText(this, "🤖 自动登录成功！", Toast.LENGTH_SHORT).show();
            navigateToHomePage();
        } else {
            Log.e(TAG, "❌ 自动登录业务失败: " + response.getMessage());
            handleAutoLoginError("自动登录失败: " + response.getMessage());
        }
    }

    /**
     * 处理自动登录错误
     * @param errorMessage 错误信息
     */
    private void handleAutoLoginError(String errorMessage) {
        Log.e(TAG, "💥 处理自动登录错误: " + errorMessage);

        // 恢复按钮状态
        setLoginButtonState(true, "🔑 登录账户");

        // 清除无效的令牌
        spManager.clearTokens();

        // 显示错误提示但不干扰用户
        Log.w(TAG, "⚠️ 自动登录失败，需要用户手动登录");
    }

    /**
     * 尝试登录
     */
    private void attemptLogin() {
        Log.d(TAG, "🎯 开始登录流程...");

        // 清除之前的错误状态
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        Log.d(TAG, "✅ 已清除历史错误状态");

        // 获取输入数据
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Log.d(TAG, "📥 用户输入 - " +
                "用户名: " + (username.isEmpty() ? "空" : "'" + username + "'") +
                ", 密码长度: " + password.length());

        // 前端输入验证
        if (!validateInputs(username, password)) {
            Log.w(TAG, "❌ 输入验证失败，停止登录流程");
            return;
        }

        // 网络状态检查
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.e(TAG, "❌ 网络不可用，停止登录流程");
            Toast.makeText(this, "🌐 网络不可用，请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "✅ 所有前置检查通过，开始执行登录");
        performLogin(username, password);
    }

    /**
     * 验证输入数据
     * @param username 用户名
     * @param password 密码
     * @return true-验证通过, false-验证失败
     */
    private boolean validateInputs(String username, String password) {
        Log.d(TAG, "🔍 开始输入数据验证...");

        boolean isValid = true;

        // 用户名验证
        if (!Validator.isValidUsername(username)) {
            String errorMsg = "👤 用户名至少3个字符";
            usernameLayout.setError(errorMsg);
            Log.w(TAG, "❌ 用户名验证失败: " + errorMsg);
            isValid = false;
        } else {
            Log.d(TAG, "✅ 用户名验证通过");
        }

        // 密码验证
        if (!Validator.isValidPassword(password)) {
            String errorMsg = "🔒 密码至少6个字符";
            passwordLayout.setError(errorMsg);
            Log.w(TAG, "❌ 密码验证失败: " + errorMsg);
            isValid = false;
        } else {
            Log.d(TAG, "✅ 密码验证通过");
        }

        Log.d(TAG, "📊 输入验证结果: " + (isValid ? "✅ 全部通过" : "❌ 存在错误"));
        return isValid;
    }

    /**
     * 执行登录网络请求
     * @param username 用户名
     * @param password 密码
     */
    private void performLogin(String username, String password) {
        Log.d(TAG, "🌐 开始执行登录网络请求...");

        // 更新按钮状态
        setLoginButtonState(false, "⏳ 登录中...");
        Log.d(TAG, "✅ 登录按钮状态已更新为禁用");

        // 创建登录请求数据
        UserLoginDTO loginDTO = new UserLoginDTO(username, password);
        Log.d(TAG, "📦 创建登录请求数据: " + loginDTO.toString());

        // 执行网络请求
        Log.d(TAG, "🚀 发送登录API请求...");
        apiService.login(loginDTO).enqueue(new Callback<ApiResponse<UserLoginVO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserLoginVO>> call,
                                   Response<ApiResponse<UserLoginVO>> response) {
                Log.d(TAG, "📥 收到登录API响应");

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ API请求成功，开始处理响应数据");
                    handleLoginResponse(response.body(), username, password);
                } else {
                    String errorMsg = "❌ API响应异常 - " +
                            "响应码: " + response.code() +
                            ", 响应体: " + (response.body() == null ? "空" : "非空");
                    Log.e(TAG, errorMsg);
                    handleLoginError("🔧 服务器响应异常，请稍后重试");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserLoginVO>> call, Throwable t) {
                Log.e(TAG, "💥 API请求失败: " + t.getMessage(), t);
                handleLoginError("🌐 网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 处理登录响应
     * @param response API响应数据
     * @param username 用户名
     * @param password 密码
     */
    private void handleLoginResponse(ApiResponse<UserLoginVO> response, String username, String password) {
        Log.d(TAG, "🔧 开始处理登录响应: " + response.toString());

        if (response.isSuccess() && response.getData() != null) {
            Log.d(TAG, "🎉 登录业务逻辑成功");

            // 获取登录响应数据
            UserLoginVO loginVO = response.getData();
            Log.d(TAG, "📋 登录响应数据: " + loginVO.toString());

            // 保存令牌信息
            spManager.saveTokens(
                    loginVO.getAccessToken(),
                    loginVO.getRefreshToken(),
                    loginVO.getAccessTokenExpiredAt() // 这里传入的是秒级时间戳
            );
            Log.d(TAG, "✅ 令牌信息已保存");

            // 保存用户凭证
            spManager.saveUserCredentials(
                    username,
                    password,
                    rememberMeCheckbox.isChecked()
            );
            Log.d(TAG, "✅ 用户凭证已保存");

            // 显示成功提示
            Toast.makeText(this, "🎉 登录成功！", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "✅ 成功提示已显示");

            // 跳转到主页面
            navigateToHomePage();
        } else {
            String errorMsg = "❌ 登录业务失败 - " +
                    "状态码: " + response.getCode() +
                    ", 消息: " + response.getMessage();
            Log.e(TAG, errorMsg);
            handleLoginError(response.getMessage());
        }
    }

    /**
     * 处理登录错误
     * @param errorMessage 错误信息
     */
    private void handleLoginError(String errorMessage) {
        Log.e(TAG, "💥 处理登录错误: " + errorMessage);

        // 恢复按钮状态
        setLoginButtonState(true, "🔑 登录账户");
        Log.d(TAG, "✅ 登录按钮状态已恢复");

        // 显示错误信息
        passwordLayout.setError(errorMessage);
        Toast.makeText(this, "❌ " + errorMessage, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ 错误信息已显示");
    }

    /**
     * 设置登录按钮状态
     * @param enabled 是否启用
     * @param text 按钮文本
     */
    private void setLoginButtonState(boolean enabled, String text) {
        loginButton.setEnabled(enabled);
        loginButton.setText(text);
        Log.d(TAG, "🔄 登录按钮状态更新 - " +
                "启用: " + enabled +
                ", 文本: '" + text + "'");
    }

    /**
     * 跳转到主页面
     */
    private void navigateToHomePage() {
        Log.d(TAG, "🚀 开始跳转到主页面...");

//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
        Log.d(TAG, "✅ 主页面Activity已启动");

        finish();
        Log.d(TAG, "🎬 当前登录页面已结束");
    }

    /**
     * 跳转到注册页面
     */
    private void navigateToRegister() {
        Log.d(TAG, "🚀 开始跳转到注册页面...");

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        Log.d(TAG, "✅ 注册页面Activity已启动");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "⬅️ 返回按钮被点击");

        if (isTaskRoot()) {
            Log.d(TAG, "🏠 当前是根Activity，显示退出确认");
            showExitConfirmation();
        } else {
            Log.d(TAG, "🔙 执行默认返回操作");
            super.onBackPressed();
        }
    }

    /**
     * 显示退出确认提示
     */
    private void showExitConfirmation() {
        Log.d(TAG, "💬 显示退出确认提示");
        Toast.makeText(this, "👋 再按一次退出应用", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💀 Activity被销毁");
    }
}