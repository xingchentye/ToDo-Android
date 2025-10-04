package com.example.todo.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.todo.CreateCategoryDialog;
import com.example.todo.R;
import com.example.todo.adapters.CategoryAdapter;
import com.example.todo.databinding.FragmentCategoryBinding;
import com.example.todo.model.List;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

/**
 * 分类管理Fragment
 */
public class CategoryFragment extends Fragment {
    private static final String TAG = "📁 分类Fragment";

    private FragmentCategoryBinding binding;
    private CategoryAdapter categoryAdapter;
    private java.util.List<List> categoryList = new ArrayList<>();
    private ApiService apiService;
    private SharedPreferencesManager spManager;

    // 当前搜索关键词
    private String currentKeyword = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🎬 Fragment创建视图开始");

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initComponents();
        setupSearch();
        loadCategories();

        Log.d(TAG, "✅ 分类Fragment初始化完成");
        return view;
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        // 初始化管理器
        spManager = new SharedPreferencesManager(requireContext());
        apiService = RetrofitClient.getApiService();

        // 设置RecyclerView - 改为线性布局（列表）
        categoryAdapter = new CategoryAdapter(categoryList, new CategoryAdapter.CategoryClickListener() {
            @Override
            public void onCategoryClick(List category) {
                Log.d(TAG, "👆 分类被点击: " + category.getName());
                openCategoryDetail(category);
            }

            @Override
            public void onCategoryEdit(List category) {
                Log.d(TAG, "✏️ 编辑分类: " + category.getName());
                editCategory(category);
            }

            @Override
            public void onCategoryDelete(List category) {
                Log.d(TAG, "🗑️ 删除分类: " + category.getName());
                deleteCategory(category);
            }
        });

        // 使用线性布局（列表布局）
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewCategories.setLayoutManager(layoutManager);
        binding.recyclerViewCategories.setAdapter(categoryAdapter);

        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "🔄 下拉刷新触发");
            refreshCategories();
        });

        // 设置FAB点击事件
        binding.fabAddCategory.setOnClickListener(v -> {
            Log.d(TAG, "👆 添加分类FAB被点击");
            createNewCategory();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void setupSearch() {
        Log.d(TAG, "🔄 设置搜索功能");

        binding.editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentKeyword = binding.editTextSearch.getText().toString().trim();
                Log.d(TAG, "🔍 搜索分类: " + currentKeyword);
                refreshCategories();
                return true;
            }
            return false;
        });

        // 实时搜索
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentKeyword = s.toString().trim();
                if (currentKeyword.isEmpty()) {
                    refreshCategories();
                } else {
                    // 延迟搜索，避免频繁请求
                    binding.getRoot().postDelayed(() -> refreshCategories(), 500);
                }
            }
        });
    }

    private void loadCategories() {
        Log.d(TAG, "📥 加载分类数据");

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Log.w(TAG, "❌ 网络不可用，显示本地数据");
            showLocalCategories();
            return;
        }

        showLoading(true);

        // 如果有搜索关键词，传递给API（需要API支持搜索）
        // 注意：这里假设API支持搜索参数，如果不支持，需要在客户端进行过滤
        apiService.getLists().enqueue(new Callback<ApiResponse<java.util.List<List>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<List>>> call, Response<ApiResponse<java.util.List<List>>> response) {
                showLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);

                ApiResponse<java.util.List<List>> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    // 在客户端进行搜索过滤
                    java.util.List<List> allCategories = processedResponse.getData();
                    categoryList.clear();

                    if (currentKeyword.isEmpty()) {
                        categoryList.addAll(allCategories);
                    } else {
                        // 客户端搜索过滤
                        for (List category : allCategories) {
                            if (category.getName().toLowerCase().contains(currentKeyword.toLowerCase()) ||
                                    (category.getDescription() != null &&
                                            category.getDescription().toLowerCase().contains(currentKeyword.toLowerCase()))) {
                                categoryList.add(category);
                            }
                        }
                    }

                    categoryAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    Log.d(TAG, "✅ 分类数据加载成功，共 " + categoryList.size() + " 个分类");
                } else {
                    Log.w(TAG, "⚠️ 分类数据加载失败: " + processedResponse.getMessage());
                    Toast.makeText(requireContext(), "加载失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<List>>> call, Throwable t) {
                showLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "💥 分类数据网络请求失败: " + t.getMessage());
                Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                showLocalCategories();
            }
        });
    }

    private void showLocalCategories() {
        Log.d(TAG, "📋 显示本地分类数据");
        // 这里可以显示本地存储的分类数据
        categoryList.clear();
        categoryAdapter.notifyDataSetChanged();
        updateEmptyState();
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void updateEmptyState() {
        if (categoryList.isEmpty()) {
            binding.textEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewCategories.setVisibility(View.GONE);
        } else {
            binding.textEmpty.setVisibility(View.GONE);
            binding.recyclerViewCategories.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        // Check if the binding object and the root view are not null
        if (binding != null && binding.getRoot() != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        // Optionally, you can also log a warning if binding is null
        else {
            Log.w(TAG, "⚠️ Cannot show loading: Binding is null. Fragment view likely destroyed.");
        }
    }

    public void refreshCategories() {
        Log.d(TAG, "🔄 刷新分类数据");
        loadCategories();
    }

    private void createNewCategory() {
        Log.d(TAG, "➕ 创建新分类");
        CreateCategoryDialog dialog = new CreateCategoryDialog();
        dialog.setCategoryCreatedListener(() -> {
            Log.d(TAG, "🎉 分类创建成功回调");
            refreshCategories();
        });
        dialog.show(getParentFragmentManager(), "create_category");
    }

    private void openCategoryDetail(List category) {
        Log.d(TAG, "📖 打开分类详情: " + category.getName());
        // TODO: 实现分类详情页面或显示分类下的任务
        Toast.makeText(requireContext(), "打开分类: " + category.getName(), Toast.LENGTH_SHORT).show();
    }

    private void editCategory(List category) {
        Log.d(TAG, "✏️ 编辑分类: " + category.getName());
        CreateCategoryDialog dialog = CreateCategoryDialog.newInstance(category);
        dialog.setCategoryCreatedListener(() -> {
            Log.d(TAG, "🎉 分类更新成功回调");
            refreshCategories();
        });
        dialog.show(getParentFragmentManager(), "edit_category");
    }

    private void deleteCategory(List category) {
        Log.d(TAG, "🗑️ 删除分类: " + category.getName());

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示确认对话框
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("删除分类")
                .setMessage("确定要删除分类 \"" + category.getName() + "\" 吗？此操作不可恢复。")
                .setPositiveButton("删除", (d, which) -> {
                    performDeleteCategory(category);
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void performDeleteCategory(List category) {
        Log.d(TAG, "🗑️ 执行删除分类: " + category.getName());

        apiService.deleteList(category.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 分类删除成功");
                            refreshCategories();
                            Toast.makeText(requireContext(), "分类已删除", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "⚠️ 分类删除失败: " + processedResponse.getMessage());
                            Toast.makeText(requireContext(), "删除失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "💥 分类删除网络请求失败: " + t.getMessage());
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment恢复，刷新数据");
        refreshCategories();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "💀 Fragment视图被销毁");
    }
}