// HomeActivity.java - 修复侧边栏头像加载
package com.example.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.example.todo.databinding.PageHomeBinding;
import com.example.todo.fragments.CategoryFragment;
import com.example.todo.fragments.ProfileFragment;
import com.example.todo.fragments.TaskFragment;
import com.example.todo.fragments.TagFragment;
import com.example.todo.model.User;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.storage.TokenManager;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.NetworkUtils;
import com.google.android.material.navigation.NavigationView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 主页Activity - 修复侧边栏头像加载
 */
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "🏠 主页Activity";

    private PageHomeBinding binding;
    private SharedPreferencesManager spManager;
    private TokenManager tokenManager;
    private ApiService apiService;

    // 当前选中的Fragment标签
    private String currentFragmentTag = "tasks";
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        binding = PageHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "✅ 主页布局加载完成");

        // 初始化组件
        initComponents();
        setupNavigation();
        setupDrawerHeader();
        loadUserInfo();

        // 默认显示任务页面
        showFragment(new TaskFragment(), "tasks", true);

        Log.d(TAG, "🎉 主页Activity初始化完成");
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件...");

        // 设置工具栏
        setSupportActionBar(binding.toolbar);

        // 初始化管理器
        spManager = new SharedPreferencesManager(this);
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService();

        Log.d(TAG, "✅ 组件初始化完成");
    }

    /**
     * 设置导航
     */
    private void setupNavigation() {
        Log.d(TAG, "🔄 设置导航...");

        // 工具栏导航图标点击
        binding.toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "👆 工具栏导航图标被点击");
            binding.drawerLayout.openDrawer(binding.navigationView);
        });

        // 底部导航点击
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "👆 底部导航项被点击: " + item.getTitle());

            if (itemId == R.id.nav_tasks) {
                showFragment(new TaskFragment(), "tasks", true);
                return true;
            } else if (itemId == R.id.nav_categories) {
                showFragment(new CategoryFragment(), "categories", true);
                return true;
            } else if (itemId == R.id.nav_tags) {
                showFragment(new TagFragment(), "tags", true);
                return true;
            }
            return false;
        });

        // 侧边导航点击
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "👆 侧边导航项被点击: " + item.getTitle());

            // 关闭侧边栏
            binding.drawerLayout.closeDrawer(binding.navigationView);

            if (itemId == R.id.nav_profile) {
                showFragment(new ProfileFragment(), "profile", false);
                return true;
            } else if (itemId == R.id.nav_settings) {
                showSettings();
                return true;
            } else if (itemId == R.id.nav_help) {
                showHelp();
                return true;
            } else if (itemId == R.id.nav_logout) {
                performLogout();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // 点击用户信息区域，显示个人中心
                showFragment(new ProfileFragment(), "profile", false);
                return true;
            }
            return false;
        });

        Log.d(TAG, "✅ 导航设置完成");
    }

    /**
     * 设置侧边栏头部信息
     */
    private void setupDrawerHeader() {
        Log.d(TAG, "🔄 设置侧边栏头部信息");

        // 获取侧边栏头部视图
        View headerView = binding.navigationView.getHeaderView(0);
        TextView textUsername = headerView.findViewById(R.id.textUsername);
        TextView textEmail = headerView.findViewById(R.id.textEmail);

        // 设置默认信息
        String savedUsername = spManager.getSavedUsername();
        if (!savedUsername.isEmpty()) {
            textUsername.setText(savedUsername);
            textEmail.setText("点击加载完整信息");
        } else {
            textUsername.setText("用户");
            textEmail.setText("请登录");
        }

        // 设置头部点击事件
        headerView.setOnClickListener(v -> {
            Log.d(TAG, "👆 侧边栏头部被点击");
            showFragment(new ProfileFragment(), "profile", false);
            binding.drawerLayout.closeDrawer(binding.navigationView);
        });

        Log.d(TAG, "✅ 侧边栏头部设置完成");
    }

    /**
     * 加载用户信息
     */
    private void loadUserInfo() {
        Log.d(TAG, "📥 加载用户信息");

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.w(TAG, "❌ 网络不可用，跳过加载用户信息");
            return;
        }

        apiService.getCurrentUser().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                ApiResponse<User> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    currentUser = processedResponse.getData();
                    updateDrawerHeader(currentUser);
                    Log.d(TAG, "✅ 用户信息加载成功: " + currentUser.getUsername());
                } else {
                    Log.w(TAG, "⚠️ 用户信息加载失败: " + processedResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "💥 用户信息网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 更新侧边栏头部信息 - 修复头像加载
     */
    private void updateDrawerHeader(User user) {
        Log.d(TAG, "🔄 更新侧边栏头部信息");

        runOnUiThread(() -> {
            View headerView = binding.navigationView.getHeaderView(0);
            TextView textUsername = headerView.findViewById(R.id.textUsername);
            TextView textEmail = headerView.findViewById(R.id.textEmail);
            android.widget.ImageView imageAvatar = headerView.findViewById(R.id.imageAvatar);

            textUsername.setText(user.getUsername());
            textEmail.setText(user.getEmail());

            // 使用Glide加载头像
            String avatarUrl = user.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Log.d(TAG, "🖼️ 加载侧边栏头像: " + avatarUrl);
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(imageAvatar);
            } else {
                // 如果头像URL为空，显示默认头像
                Log.d(TAG, "🖼️ 使用默认头像");
                imageAvatar.setImageResource(R.drawable.ic_profile);
            }

            Log.d(TAG, "✅ 侧边栏头部信息更新完成");
        });
    }

    /**
     * 显示Fragment
     * @param fragment Fragment实例
     * @param tag Fragment标签
     * @param updateBottomNav 是否更新底部导航
     */
    private void showFragment(Fragment fragment, String tag, boolean updateBottomNav) {
        Log.d(TAG, "🔄 显示Fragment: " + tag + ", 更新底部导航: " + updateBottomNav);

        if (currentFragmentTag.equals(tag)) {
            Log.d(TAG, "ℹ️ Fragment已显示，跳过");
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment, tag);
        transaction.commit();

        currentFragmentTag = tag;

        // 更新底部导航状态
        if (updateBottomNav) {
            updateBottomNavigation(tag);
        } else {
            // 对于侧边栏的Fragment，清除底部导航选中状态
            clearBottomNavigationSelection();
        }

        Log.d(TAG, "✅ Fragment显示完成: " + tag);
    }

    /**
     * 更新底部导航状态
     */
    private void updateBottomNavigation(String tag) {
        Log.d(TAG, "🔄 更新底部导航状态: " + tag);

        switch (tag) {
            case "tasks":
                binding.bottomNavigation.setSelectedItemId(R.id.nav_tasks);
                break;
            case "categories":
                binding.bottomNavigation.setSelectedItemId(R.id.nav_categories);
                break;
            case "tags":
                binding.bottomNavigation.setSelectedItemId(R.id.nav_tags);
                break;
            default:
                clearBottomNavigationSelection();
        }
    }

    /**
     * 清除底部导航选中状态 - 修复版本
     */
    private void clearBottomNavigationSelection() {
        Log.d(TAG, "🔄 清除底部导航选中状态");

        // 清除底部导航的选中状态
        binding.bottomNavigation.setSelectedItemId(-1);

        // 手动清除每个项目的选中状态
        Menu menu = binding.bottomNavigation.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }

        Log.d(TAG, "✅ 底部导航选中状态已清除");
    }

    /**
     * 显示设置页面
     */
    private void showSettings() {
        Log.d(TAG, "⚙️ 显示设置页面");
        Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示帮助页面
     */
    private void showHelp() {
        Log.d(TAG, "❓ 显示帮助页面");
        Toast.makeText(this, "帮助功能开发中", Toast.LENGTH_SHORT).show();
    }

    /**
     * 执行退出登录
     */
    private void performLogout() {
        Log.d(TAG, "🚪 开始执行退出登录");

        // 显示确认对话框
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (d, which) -> {
                    doLogout();
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void doLogout() {
        Log.d(TAG, "🔐 执行退出登录操作");

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.w(TAG, "❌ 网络不可用，执行本地退出");
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
        // 清除用户数据
        spManager.clearAll();
        tokenManager.clearAllTokens();

        // 跳转到登录页面
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "👋 已退出登录", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ 退出登录完成");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "🔄 创建选项菜单");
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Log.d(TAG, "👆 选项菜单项被点击: " + item.getTitle());

        if (itemId == R.id.action_search) {
            showSearch();
            return true;
        } else if (itemId == R.id.action_add) {
            // 根据当前页面显示不同的添加功能
            showAddDialog();
            return true;
        } else if (itemId == R.id.action_refresh) {
            refreshCurrentPage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示搜索功能
     */
    private void showSearch() {
        Log.d(TAG, "🔍 显示搜索功能");
        Toast.makeText(this, "搜索功能开发中", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示添加对话框
     */
    private void showAddDialog() {
        Log.d(TAG, "➕ 显示添加对话框，当前页面: " + currentFragmentTag);

        switch (currentFragmentTag) {
            case "tasks":
                // 跳转到创建任务页面
                Intent taskIntent = new Intent(this, CreateTaskActivity.class);
                startActivity(taskIntent);
                break;
            case "categories":
                // 显示创建分类对话框
                showCreateCategoryDialog();
                break;
            case "tags":
                // 显示创建标签对话框
                showCreateTagDialog();
                break;
            case "profile":
                // 个人中心页面，编辑资料
                if (currentUser != null) {
                    Intent editIntent = new Intent(this, EditProfileActivity.class);
                    editIntent.putExtra("user", currentUser);
                    startActivity(editIntent);
                } else {
                    Toast.makeText(this, "请先加载用户信息", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Toast.makeText(this, "添加功能", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示创建分类对话框
     */
    private void showCreateCategoryDialog() {
        CreateCategoryDialog dialog = new CreateCategoryDialog();
        dialog.setCategoryCreatedListener(() -> {
            Log.d(TAG, "🎉 分类创建成功，刷新页面");
            refreshCurrentPage();
        });
        dialog.show(getSupportFragmentManager(), "create_category");
    }

    /**
     * 显示创建标签对话框
     */
    private void showCreateTagDialog() {
        CreateTagDialog dialog = new CreateTagDialog();
        dialog.setTagCreatedListener(() -> {
            Log.d(TAG, "🎉 标签创建成功，刷新页面");
            refreshCurrentPage();
        });
        dialog.show(getSupportFragmentManager(), "create_tag");
    }

    /**
     * 刷新当前页面
     */
    private void refreshCurrentPage() {
        Log.d(TAG, "🔄 刷新当前页面: " + currentFragmentTag);
        Toast.makeText(this, "刷新中...", Toast.LENGTH_SHORT).show();

        // 这里可以调用各Fragment的刷新方法
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
        if (currentFragment instanceof TaskFragment) {
            ((TaskFragment) currentFragment).refreshTasks();
        } else if (currentFragment instanceof CategoryFragment) {
            ((CategoryFragment) currentFragment).refreshCategories();
        } else if (currentFragment instanceof TagFragment) {
            ((TagFragment) currentFragment).refreshTags();
        } else if (currentFragment instanceof ProfileFragment) {
            ((ProfileFragment) currentFragment).refreshProfile();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Activity恢复");

        // 重新加载用户信息
        loadUserInfo();

        // 刷新当前页面
        refreshCurrentPage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💀 Activity被销毁");

        if (tokenManager != null) {
            tokenManager.setTokenRefreshListener(null);
        }
    }
}