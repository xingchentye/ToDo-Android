package com.example.todo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.todo.model.dto.UserLoginDTO;
import com.example.todo.model.dto.UserRegisterDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.model.vo.UserLoginVO;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.storage.TokenManager;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.Debouncer;
import com.example.todo.utils.NetworkUtils;
import com.example.todo.utils.Validator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 注册Activity - 用户注册界面
 * 处理用户注册逻辑和实时校验
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "🚀 注册Activity"; // 日志标签

    // 界面组件
    private ImageButton backButton;
    private TextInputEditText usernameEditText;
    private TextInputEditText nicknameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputLayout usernameLayout;
    private TextInputLayout nicknameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextView usernameStatusText;
    private TextView emailStatusText;
    private Button registerButton;
    private TextView loginHint;
    private TextView versionInfo;

    // 业务组件
    private SharedPreferencesManager spManager;
    private TokenManager tokenManager;
    private ApiService apiService;
    private Debouncer usernameDebouncer;
    private Debouncer emailDebouncer;

    // 校验状态
    private boolean isUsernameValid = false;
    private boolean isUsernameAvailable = false;
    private boolean isEmailValid = false;
    private boolean isEmailAvailable = false;
    private boolean isNicknameValid = false;
    private boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        setContentView(R.layout.page_register);
        Log.d(TAG, "✅ 注册布局文件加载完成");

        // 初始化各个模块
        initViews();
        initDependencies();
        setupClickListeners();
        setupTextWatchers();

        Log.d(TAG, "🎉 注册Activity初始化完成");
    }

    /**
     * 初始化界面组件
     */
    private void initViews() {
        Log.d(TAG, "🔄 开始初始化界面组件...");

        backButton = findViewById(R.id.backButton);
        usernameEditText = findViewById(R.id.usernameEditText);
        nicknameEditText = findViewById(R.id.nicknameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameLayout = findViewById(R.id.usernameLayout);
        nicknameLayout = findViewById(R.id.nicknameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        usernameStatusText = findViewById(R.id.usernameStatusText);
        emailStatusText = findViewById(R.id.emailStatusText);
        registerButton = findViewById(R.id.registerButton);
        loginHint = findViewById(R.id.loginHint);
        versionInfo = findViewById(R.id.versionInfo);

        // 设置提示文本
        usernameStatusText.setText(Validator.getUsernameHint());
        emailStatusText.setText(Validator.getEmailHint());

        Log.d(TAG, "✅ 所有界面组件初始化完成");
    }

    /**
     * 初始化依赖组件
     */
    private void initDependencies() {
        Log.d(TAG, "🔄 开始初始化依赖组件...");

        spManager = new SharedPreferencesManager(this);
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService();
        usernameDebouncer = new Debouncer(500); // 500ms防抖
        emailDebouncer = new Debouncer(500);    // 500ms防抖

        Log.d(TAG, "✅ 依赖组件初始化完成");
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        Log.d(TAG, "🔄 设置点击监听器...");

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "👆 返回按钮被点击");
            navigateToLogin();
        });

        // 注册按钮点击事件
        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "👆 注册按钮被点击");
            attemptRegister();
        });

        // 登录提示点击事件
        loginHint.setOnClickListener(v -> {
            Log.d(TAG, "👆 登录提示被点击，返回登录页面");
            navigateToLogin();
        });

        Log.d(TAG, "✅ 所有点击监听器设置完成");
    }

    /**
     * 设置文本监听器
     */
    private void setupTextWatchers() {
        Log.d(TAG, "🔄 设置文本监听器...");

        // 用户名输入监听
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                Log.d(TAG, "👤 用户名输入变化: " + username);

                // 前端格式验证
                validateUsernameFormat(username);

                // 后端可用性检查（防抖）
                if (Validator.isValidUsername(username)) {
                    usernameDebouncer.debounce("check_username", () -> checkUsernameAvailability(username));
                } else {
                    usernameDebouncer.cancel("check_username");
                    setUsernameAvailable(false, "用户名格式不正确");
                }

                // 更新注册按钮状态
                updateRegisterButtonState();
            }
        });

        // 昵称输入监听
        nicknameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String nickname = s.toString().trim();
                Log.d(TAG, "👤 昵称输入变化: " + nickname);

                // 昵称验证
                validateNicknameFormat(nickname);

                // 更新注册按钮状态
                updateRegisterButtonState();
            }
        });

        // 邮箱输入监听
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                Log.d(TAG, "📧 邮箱输入变化: " + email);

                // 前端格式验证
                validateEmailFormat(email);

                // 后端可用性检查（防抖）
                if (Validator.isValidEmail(email)) {
                    emailDebouncer.debounce("check_email", () -> checkEmailAvailability(email));
                } else {
                    emailDebouncer.cancel("check_email");
                    setEmailAvailable(false, "邮箱格式不正确");
                }

                // 更新注册按钮状态
                updateRegisterButtonState();
            }
        });

        // 密码输入监听
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                Log.d(TAG, "🔒 密码输入变化，长度: " + password.length());

                // 密码验证
                validatePasswordFormat(password);

                // 更新注册按钮状态
                updateRegisterButtonState();
            }
        });

        Log.d(TAG, "✅ 所有文本监听器设置完成");
    }

    /**
     * 验证用户名格式
     * @param username 用户名
     */
    private void validateUsernameFormat(String username) {
        isUsernameValid = Validator.isValidUsername(username);

        if (username.isEmpty()) {
            usernameLayout.setError(null);
            usernameStatusText.setText(Validator.getUsernameHint()); // 这里会自动使用新的提示
            usernameStatusText.setTextColor(ContextCompat.getColor(this, R.color.onSurfaceVariant));
            usernameLayout.setEndIconTintList(null);
        } else if (isUsernameValid) {
            usernameLayout.setError(null);
            usernameStatusText.setText("✅ 用户名格式正确，正在检查可用性...");
            usernameStatusText.setTextColor(ContextCompat.getColor(this, R.color.success));
            usernameLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.success));
        } else {
            usernameLayout.setError("❌ 用户名格式不正确");
            usernameStatusText.setText("❌ " + Validator.getUsernameHint()); // 这里会自动使用新的提示
            usernameStatusText.setTextColor(ContextCompat.getColor(this, R.color.error));
            usernameLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.error));
        }

        Log.d(TAG, "👤 用户名格式验证结果: " + (isUsernameValid ? "✅ 有效" : "❌ 无效"));
    }

    /**
     * 验证昵称格式
     * @param nickname 昵称
     */
    private void validateNicknameFormat(String nickname) {
        isNicknameValid = Validator.isValidNickname(nickname);

        if (nickname.isEmpty()) {
            nicknameLayout.setError(null);
        } else if (isNicknameValid) {
            nicknameLayout.setError(null);
        } else {
            nicknameLayout.setError("❌ 昵称长度需2-20个字符");
        }

        Log.d(TAG, "👤 昵称格式验证结果: " + (isNicknameValid ? "✅ 有效" : "❌ 无效"));
    }

    /**
     * 验证邮箱格式
     * @param email 邮箱
     */
    private void validateEmailFormat(String email) {
        isEmailValid = Validator.isValidEmail(email);

        if (email.isEmpty()) {
            emailLayout.setError(null);
            emailStatusText.setText(Validator.getEmailHint());
            emailStatusText.setTextColor(ContextCompat.getColor(this, R.color.onSurfaceVariant));
            emailLayout.setEndIconTintList(null);
        } else if (isEmailValid) {
            emailLayout.setError(null);
            emailStatusText.setText("✅ 邮箱格式正确，正在检查可用性...");
            emailStatusText.setTextColor(ContextCompat.getColor(this, R.color.success));
            emailLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.success));
        } else {
            emailLayout.setError("❌ 邮箱格式不正确");
            emailStatusText.setText("❌ " + Validator.getEmailHint());
            emailStatusText.setTextColor(ContextCompat.getColor(this, R.color.error));
            emailLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.error));
        }

        Log.d(TAG, "📧 邮箱格式验证结果: " + (isEmailValid ? "✅ 有效" : "❌ 无效"));
    }

    /**
     * 验证密码格式
     * @param password 密码
     */
    private void validatePasswordFormat(String password) {
        isPasswordValid = Validator.isValidPassword(password);

        if (password.isEmpty()) {
            passwordLayout.setError(null);
        } else if (isPasswordValid) {
            passwordLayout.setError(null);
        } else {
            passwordLayout.setError("❌ 密码需包含字母和数字");
        }

        Log.d(TAG, "🔒 密码格式验证结果: " + (isPasswordValid ? "✅ 有效" : "❌ 无效"));
    }

    /**
     * 检查用户名可用性
     * @param username 用户名
     */
    private void checkUsernameAvailability(String username) {
        Log.d(TAG, "🌐 开始检查用户名可用性: " + username);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.w(TAG, "❌ 网络不可用，跳过用户名检查");
            return;
        }

        apiService.checkUsername(username).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                // 使用统一的响应处理器
                ApiResponse<Boolean> processedResponse = ApiResponseHandler.processResponse(response);

                boolean available = Boolean.TRUE.equals(processedResponse.getData());

                Log.d(TAG, "✅ 用户名可用性检查完成: " + (available ? "✅ 可用" : "❌ 不可用"));

                if (available) {
                    setUsernameAvailable(true, null);
                } else {
                    // 使用处理后的错误信息
                    String errorMessage = processedResponse.getMessage();
                    setUsernameAvailable(false, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "💥 用户名检查网络请求失败: " + t.getMessage());
                setUsernameAvailable(false, "网络错误，无法检查用户名");
            }
        });
    }

    /**
     * 检查邮箱可用性
     * @param email 邮箱
     */
    private void checkEmailAvailability(String email) {
        Log.d(TAG, "🌐 开始检查邮箱可用性: " + email);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.w(TAG, "❌ 网络不可用，跳过邮箱检查");
            return;
        }

        apiService.checkEmail(email).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                // 使用统一的响应处理器
                ApiResponse<Boolean> processedResponse = ApiResponseHandler.processResponse(response);

                boolean available = Boolean.TRUE.equals(processedResponse.getData());

                Log.d(TAG, "✅ 邮箱可用性检查完成: " + (available ? "✅ 可用" : "❌ 不可用"));

                if (available) {
                    setEmailAvailable(true, null);
                } else {
                    // 使用处理后的错误信息
                    String errorMessage = processedResponse.getMessage();
                    setEmailAvailable(false, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "💥 邮箱检查网络请求失败: " + t.getMessage());
                setEmailAvailable(false, "网络错误，无法检查邮箱");
            }
        });
    }

    /**
     * 设置用户名可用状态
     * @param available 是否可用
     * @param errorMessage 错误信息（如果不可用）
     */
    private void setUsernameAvailable(boolean available, String errorMessage) {
        isUsernameAvailable = available;

        runOnUiThread(() -> {
            if (available) {
                usernameStatusText.setText("✅ 用户名可用");
                usernameStatusText.setTextColor(ContextCompat.getColor(this, R.color.success));
                usernameLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.success));
                usernameLayout.setError(null);
            } else if (isUsernameValid) {
                // 只在用户名格式正确但不可用时显示错误
                String displayMessage = errorMessage != null ? errorMessage : "用户名已被占用";
                usernameStatusText.setText("❌ " + displayMessage);
                usernameStatusText.setTextColor(ContextCompat.getColor(this, R.color.error));
                usernameLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.error));
                usernameLayout.setError(displayMessage);
            }

            updateRegisterButtonState();
        });

        Log.d(TAG, "👤 用户名可用状态更新: " + (available ? "✅ 可用" : "❌ 不可用 - " + errorMessage));
    }

    /**
     * 设置邮箱可用状态
     * @param available 是否可用
     * @param errorMessage 错误信息（如果不可用）
     */
    private void setEmailAvailable(boolean available, String errorMessage) {
        isEmailAvailable = available;

        runOnUiThread(() -> {
            if (available) {
                emailStatusText.setText("✅ 邮箱可用");
                emailStatusText.setTextColor(ContextCompat.getColor(this, R.color.success));
                emailLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.success));
                emailLayout.setError(null);
            } else if (isEmailValid) {
                // 只在邮箱格式正确但不可用时显示错误
                String displayMessage = errorMessage != null ? errorMessage : "邮箱已被注册";
                emailStatusText.setText("❌ " + displayMessage);
                emailStatusText.setTextColor(ContextCompat.getColor(this, R.color.error));
                emailLayout.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.error));
                emailLayout.setError(displayMessage);
            }

            updateRegisterButtonState();
        });

        Log.d(TAG, "📧 邮箱可用状态更新: " + (available ? "✅ 可用" : "❌ 不可用 - " + errorMessage));
    }

    /**
     * 更新注册按钮状态
     */
    private void updateRegisterButtonState() {
        boolean isFormValid = isUsernameValid && isUsernameAvailable &&
                isNicknameValid && isEmailValid &&
                isEmailAvailable && isPasswordValid;

        registerButton.setEnabled(isFormValid);
        registerButton.setAlpha(isFormValid ? 1.0f : 0.5f);

        Log.d(TAG, "🔄 注册按钮状态更新 - " +
                "启用: " + isFormValid +
                ", 条件详情[用户名有效: " + isUsernameValid +
                ", 用户名可用: " + isUsernameAvailable +
                ", 昵称有效: " + isNicknameValid +
                ", 邮箱有效: " + isEmailValid +
                ", 邮箱可用: " + isEmailAvailable +
                ", 密码有效: " + isPasswordValid + "]");
    }

    /**
     * 尝试注册
     */
    private void attemptRegister() {
        Log.d(TAG, "🎯 开始注册流程...");

        // 获取输入数据
        String username = usernameEditText.getText().toString().trim();
        String nickname = nicknameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        Log.d(TAG, "📥 注册数据 - " +
                "用户名: '" + username + "'" +
                ", 昵称: '" + nickname + "'" +
                ", 邮箱: '" + email + "'" +
                ", 密码长度: " + password.length());

        // 最终验证
        if (!validateFinalInputs(username, nickname, email, password)) {
            Log.w(TAG, "❌ 最终验证失败，停止注册流程");
            return;
        }

        // 网络状态检查
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.e(TAG, "❌ 网络不可用，停止注册流程");
            Toast.makeText(this, "🌐 网络不可用，请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "✅ 所有前置检查通过，开始执行注册");
        performRegister(username, nickname, email, password);
    }

    /**
     * 最终输入验证
     * @param username 用户名
     * @param nickname 昵称
     * @param email 邮箱
     * @param password 密码
     * @return true-验证通过, false-验证失败
     */
    private boolean validateFinalInputs(String username, String nickname, String email, String password) {
        Log.d(TAG, "🔍 开始最终输入验证...");

        boolean isValid = true;

        if (!isUsernameValid || !isUsernameAvailable) {
            String errorMessage = "请检查用户名";
            if (usernameLayout.getError() != null) {
                errorMessage = usernameLayout.getError().toString();
            }
            usernameLayout.setError("❌ " + errorMessage);
            isValid = false;
        }

        if (!isNicknameValid) {
            nicknameLayout.setError("❌ 请检查昵称");
            isValid = false;
        }

        if (!isEmailValid || !isEmailAvailable) {
            String errorMessage = "请检查邮箱";
            if (emailLayout.getError() != null) {
                errorMessage = emailLayout.getError().toString();
            }
            emailLayout.setError("❌ " + errorMessage);
            isValid = false;
        }

        if (!isPasswordValid) {
            passwordLayout.setError("❌ 请检查密码");
            isValid = false;
        }

        Log.d(TAG, "📊 最终验证结果: " + (isValid ? "✅ 全部通过" : "❌ 存在错误"));
        return isValid;
    }

    /**
     * 执行注册网络请求
     * @param username 用户名
     * @param nickname 昵称
     * @param email 邮箱
     * @param password 密码
     */
    private void performRegister(String username, String nickname, String email, String password) {
        Log.d(TAG, "🌐 开始执行注册网络请求...");

        // 更新按钮状态
        setRegisterButtonState(false, "⏳ 注册中...");
        Log.d(TAG, "✅ 注册按钮状态已更新为禁用");

        // 创建注册请求数据
        UserRegisterDTO registerDTO = new UserRegisterDTO(username, nickname, email, password);
        Log.d(TAG, "📦 创建注册请求数据: " + registerDTO.toString());

        // 执行网络请求
        Log.d(TAG, "🚀 发送注册API请求...");
        apiService.register(registerDTO).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Log.d(TAG, "📥 收到注册API响应");

                // 使用统一的响应处理器
                ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess()) {
                    Log.d(TAG, "🎉 注册业务逻辑成功");
                    handleRegisterSuccess(username, password);
                } else {
                    Log.w(TAG, "⚠️ 注册业务逻辑失败 - " +
                            "状态码: " + processedResponse.getCode() +
                            ", 消息: " + processedResponse.getMessage());
                    handleRegisterError(processedResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "💥 API请求失败: " + t.getMessage(), t);
                handleRegisterError("🌐 网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 处理注册成功
     * @param username 用户名
     * @param password 密码
     */
    private void handleRegisterSuccess(String username, String password) {
        Log.d(TAG, "🎉 注册成功，开始自动登录");

        // 显示成功提示
        Toast.makeText(this, "🎉 注册成功！正在自动登录...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ 成功提示已显示");

        // 注册成功后自动登录
        performAutoLoginAfterRegister(username, password);
    }

    /**
     * 注册成功后自动登录
     * @param username 用户名
     * @param password 密码
     */
    private void performAutoLoginAfterRegister(String username, String password) {
        Log.d(TAG, "🤖 注册成功后开始自动登录...");

        // 更新按钮状态
        setRegisterButtonState(false, "⏳ 自动登录中...");

        UserLoginDTO loginDTO = new UserLoginDTO(username, password);
        Log.d(TAG, "📦 创建自动登录请求数据: " + loginDTO.toString());

        apiService.login(loginDTO).enqueue(new Callback<ApiResponse<UserLoginVO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserLoginVO>> call,
                                   Response<ApiResponse<UserLoginVO>> response) {
                // 使用统一的响应处理器
                ApiResponse<UserLoginVO> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    Log.d(TAG, "✅ 自动登录成功");
                    handleAutoLoginAfterRegister(processedResponse.getData(), username, password);
                } else {
                    Log.e(TAG, "❌ 自动登录失败: " + processedResponse.getMessage());
                    handleAutoLoginAfterRegisterError("自动登录失败，请手动登录");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserLoginVO>> call, Throwable t) {
                Log.e(TAG, "💥 自动登录网络请求失败: " + t.getMessage());
                handleAutoLoginAfterRegisterError("自动登录网络错误");
            }
        });
    }

    /**
     * 处理注册后自动登录响应
     * @param loginVO 登录响应数据
     * @param username 用户名
     * @param password 密码
     */
    private void handleAutoLoginAfterRegister(UserLoginVO loginVO, String username, String password) {
        Log.d(TAG, "🔧 开始处理注册后自动登录响应");

        // 保存令牌信息
        spManager.saveTokens(
                loginVO.getAccessToken(),
                loginVO.getRefreshToken(),
                loginVO.getAccessTokenExpiredAt()
        );

        // 保存用户凭证（默认记住登录状态）
        spManager.saveUserCredentials(username, password, true);

        Toast.makeText(this, "🎉 注册并登录成功！", Toast.LENGTH_SHORT).show();
        navigateToHomePage();
    }

    /**
     * 处理注册后自动登录错误
     * @param errorMessage 错误信息
     */
    private void handleAutoLoginAfterRegisterError(String errorMessage) {
        Log.e(TAG, "💥 处理注册后自动登录错误: " + errorMessage);

        // 恢复按钮状态
        setRegisterButtonState(true, "📝 注册账户");

        Toast.makeText(this, "❌ " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * 跳转到登录页面并填充凭证
     * @param username 用户名
     * @param password 密码
     */
    private void navigateToLoginWithCredentials(String username, String password) {
        Log.d(TAG, "🚀 跳转到登录页面并填充凭证");

        Intent intent = new Intent(this, MainActivity.class);

        // 使用 Bundle 传递凭证信息
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        bundle.putString("password", password);
        intent.putExtras(bundle);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 处理注册错误
     * @param errorMessage 错误信息
     */
    private void handleRegisterError(String errorMessage) {
        Log.e(TAG, "💥 处理注册错误: " + errorMessage);

        // 恢复按钮状态
        setRegisterButtonState(true, "📝 注册账户");
        Log.d(TAG, "✅ 注册按钮状态已恢复");

        // 显示错误信息
        Toast.makeText(this, "❌ " + errorMessage, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ 错误信息已显示");
    }

    /**
     * 设置注册按钮状态
     * @param enabled 是否启用
     * @param text 按钮文本
     */
    private void setRegisterButtonState(boolean enabled, String text) {
        registerButton.setEnabled(enabled);
        registerButton.setText(text);
        Log.d(TAG, "🔄 注册按钮状态更新 - " +
                "启用: " + enabled +
                ", 文本: '" + text + "'");
    }

    /**
     * 跳转到登录页面
     */
    private void navigateToLogin() {
        Log.d(TAG, "🚀 开始跳转到登录页面...");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        Log.d(TAG, "✅ 登录页面Activity已启动");

        finish();
        Log.d(TAG, "🎬 当前注册页面已结束");
    }

    /**
     * 跳转到主页面
     */
    private void navigateToHomePage() {
        Log.d(TAG, "🚀 开始跳转到主页面...");

        // TODO: 这里暂时注释掉，等你的HomeActivity开发完成后再取消注释
        // Intent intent = new Intent(this, HomeActivity.class);
        // startActivity(intent);
        Log.d(TAG, "✅ 主页面Activity已启动");

        finish();

//        // 临时解决方案：显示成功消息但不跳转
//        Toast.makeText(this, "🎉 注册并登录成功！主页开发中...", Toast.LENGTH_LONG).show();
//        Log.d(TAG, "🏠 主页开发中，暂不跳转");

        // 返回登录页面
        navigateToLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 清理防抖器
        if (usernameDebouncer != null) {
            usernameDebouncer.shutdown();
        }
        if (emailDebouncer != null) {
            emailDebouncer.shutdown();
        }

        // 清理令牌管理器
        if (tokenManager != null) {
            tokenManager.setTokenRefreshListener(null);
        }

        Log.d(TAG, "💀 Activity被销毁");
    }
}