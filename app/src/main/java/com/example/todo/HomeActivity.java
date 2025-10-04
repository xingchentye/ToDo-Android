package com.example.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.todo.databinding.PageHomeBinding;
import com.example.todo.fragments.CategoryFragment;
import com.example.todo.fragments.ProfileFragment;
import com.example.todo.fragments.TaskFragment;
import com.example.todo.fragments.TagFragment;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.storage.TokenManager;
import com.google.android.material.navigation.NavigationView;

/**
 * 主页Activity - 包含任务管理、分类管理、标签管理和个人中心
 */
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "🏠 主页Activity";

    private PageHomeBinding binding;
    private SharedPreferencesManager spManager;
    private TokenManager tokenManager;

    // 当前选中的Fragment标签
    private String currentFragmentTag = "tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        // 初始化RetrofitClient
        RetrofitClient.init(this);

        binding = PageHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "✅ 主页布局加载完成");

        // 初始化组件
        initComponents();
        setupNavigation();

        // 默认显示任务页面
        showFragment(new TaskFragment(), "tasks");

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
                showFragment(new TaskFragment(), "tasks");
                return true;
            } else if (itemId == R.id.nav_categories) {
                showFragment(new CategoryFragment(), "categories");
                return true;
            } else if (itemId == R.id.nav_tags) {
                showFragment(new TagFragment(), "tags");
                return true;
            }
            return false;
        });

        // 侧边导航点击
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "👆 侧边导航项被点击: " + item.getTitle());

            binding.drawerLayout.closeDrawer(binding.navigationView);

            if (itemId == R.id.nav_profile) {
                showFragment(new ProfileFragment(), "profile");
                binding.bottomNavigation.setSelectedItemId(R.id.nav_tasks); // 重置底部导航
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_help) {
                Toast.makeText(this, "帮助功能开发中", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_logout) {
                performLogout();
                return true;
            }
            return false;
        });

        Log.d(TAG, "✅ 导航设置完成");
    }

    /**
     * 显示Fragment
     * @param fragment Fragment实例
     * @param tag Fragment标签
     */
    private void showFragment(Fragment fragment, String tag) {
        Log.d(TAG, "🔄 显示Fragment: " + tag);

        if (currentFragmentTag.equals(tag)) {
            Log.d(TAG, "ℹ️ Fragment已显示，跳过");
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment, tag);
        transaction.commit();

        currentFragmentTag = tag;
        Log.d(TAG, "✅ Fragment显示完成: " + tag);
    }

    /**
     * 执行退出登录
     */
    private void performLogout() {
        Log.d(TAG, "🚪 开始执行退出登录");

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
            Toast.makeText(this, "搜索功能开发中", Toast.LENGTH_SHORT).show();
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
            default:
                Toast.makeText(this, "添加功能", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示创建分类对话框
     */
    private void showCreateCategoryDialog() {
        CreateCategoryDialog dialog = new CreateCategoryDialog();
        dialog.show(getSupportFragmentManager(), "create_category");
    }

    /**
     * 显示创建标签对话框
     */
    private void showCreateTagDialog() {
        CreateTagDialog dialog = new CreateTagDialog();
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
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💀 Activity被销毁");

        if (tokenManager != null) {
            tokenManager.setTokenRefreshListener(null);
        }
    }
}