package com.example.todo.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.todo.CreateTagDialog;
import com.example.todo.R;
import com.example.todo.adapters.TagAdapter;
import com.example.todo.databinding.FragmentTagBinding;
import com.example.todo.model.Tag;
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
 * 标签管理Fragment
 */
public class TagFragment extends Fragment {
    private static final String TAG = "🏷️ 标签Fragment";

    private FragmentTagBinding binding;
    private TagAdapter tagAdapter;
    private java.util.List<Tag> tagList = new ArrayList<>();
    private ApiService apiService;
    private SharedPreferencesManager spManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🎬 Fragment创建视图开始");

        binding = FragmentTagBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initComponents();
        loadTags();

        Log.d(TAG, "✅ 标签Fragment初始化完成");
        return view;
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        // 初始化管理器
        spManager = new SharedPreferencesManager(requireContext());
        apiService = RetrofitClient.getApiService();

        // 设置RecyclerView - 改为线性布局（列表）
        tagAdapter = new TagAdapter(tagList, new TagAdapter.TagClickListener() {
            @Override
            public void onTagClick(Tag tag) {
                Log.d(TAG, "👆 标签被点击: " + tag.getName());
                openTagDetail(tag);
            }

            @Override
            public void onTagEdit(Tag tag) {
                Log.d(TAG, "✏️ 编辑标签: " + tag.getName());
                editTag(tag);
            }

            @Override
            public void onTagDelete(Tag tag) {
                Log.d(TAG, "🗑️ 删除标签: " + tag.getName());
                deleteTag(tag);
            }
        });

        // 使用线性布局（列表布局）
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewTags.setLayoutManager(layoutManager);
        binding.recyclerViewTags.setAdapter(tagAdapter);

        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "🔄 下拉刷新触发");
            refreshTags();
        });

        // 设置FAB点击事件
        binding.fabAddTag.setOnClickListener(v -> {
            Log.d(TAG, "👆 添加标签FAB被点击");
            createNewTag();
        });

        // 设置排序按钮
        binding.buttonSort.setOnClickListener(v -> {
            Log.d(TAG, "🔃 排序按钮被点击");
            showSortOptions();
        });

        // 设置筛选按钮
        binding.buttonFilter.setOnClickListener(v -> {
            Log.d(TAG, "🔍 筛选按钮被点击");
            showFilterOptions();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void loadTags() {
        Log.d(TAG, "📥 加载标签数据");

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Log.w(TAG, "❌ 网络不可用，显示本地数据");
            showLocalTags();
            return;
        }

        showLoading(true);

        // 使用无参数的getTags方法
        apiService.getTags().enqueue(new Callback<ApiResponse<java.util.List<Tag>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<Tag>>> call, Response<ApiResponse<java.util.List<Tag>>> response) {
                showLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);

                ApiResponse<java.util.List<Tag>> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    tagList.clear();
                    tagList.addAll(processedResponse.getData());
                    tagAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    Log.d(TAG, "✅ 标签数据加载成功，共 " + tagList.size() + " 个标签");
                } else {
                    Log.w(TAG, "⚠️ 标签数据加载失败: " + processedResponse.getMessage());
                    Toast.makeText(requireContext(), "加载失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<Tag>>> call, Throwable t) {
                showLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "💥 标签数据网络请求失败: " + t.getMessage());
                Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                showLocalTags();
            }
        });
    }

    private void showLocalTags() {
        Log.d(TAG, "📋 显示本地标签数据");
        // 这里可以显示本地存储的标签数据
        tagList.clear();
        tagAdapter.notifyDataSetChanged();
        updateEmptyState();
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void updateEmptyState() {
        if (tagList.isEmpty()) {
            binding.textEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewTags.setVisibility(View.GONE);
        } else {
            binding.textEmpty.setVisibility(View.GONE);
            binding.recyclerViewTags.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void refreshTags() {
        Log.d(TAG, "🔄 刷新标签数据");
        loadTags();
    }

    private void createNewTag() {
        Log.d(TAG, "➕ 创建新标签");
        CreateTagDialog dialog = new CreateTagDialog();
        dialog.setTagCreatedListener(() -> {
            Log.d(TAG, "🎉 标签创建成功回调");
            refreshTags();
        });
        dialog.show(getParentFragmentManager(), "create_tag");
    }

    private void openTagDetail(Tag tag) {
        Log.d(TAG, "📖 打开标签详情: " + tag.getName());
        // TODO: 实现标签详情页面或显示标签下的任务
        Toast.makeText(requireContext(), "打开标签: " + tag.getName(), Toast.LENGTH_SHORT).show();
    }

    private void editTag(Tag tag) {
        Log.d(TAG, "✏️ 编辑标签: " + tag.getName());
        CreateTagDialog dialog = CreateTagDialog.newInstance(tag);
        dialog.setTagCreatedListener(() -> {
            Log.d(TAG, "🎉 标签更新成功回调");
            refreshTags();
        });
        dialog.show(getParentFragmentManager(), "edit_tag");
    }

    private void deleteTag(Tag tag) {
        Log.d(TAG, "🗑️ 删除标签: " + tag.getName());

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示确认对话框
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("删除标签")
                .setMessage("确定要删除标签 \"" + tag.getName() + "\" 吗？此操作不可恢复。")
                .setPositiveButton("删除", (d, which) -> {
                    performDeleteTag(tag);
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void performDeleteTag(Tag tag) {
        Log.d(TAG, "🗑️ 执行删除标签: " + tag.getName());

        apiService.deleteTag(tag.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 标签删除成功");
                            refreshTags();
                            Toast.makeText(requireContext(), "标签已删除", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "⚠️ 标签删除失败: " + processedResponse.getMessage());
                            Toast.makeText(requireContext(), "删除失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "💥 标签删除网络请求失败: " + t.getMessage());
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSortOptions() {
        Log.d(TAG, "📊 显示排序选项");
        // 实现排序功能
        Toast.makeText(requireContext(), "排序功能开发中", Toast.LENGTH_SHORT).show();
    }

    private void showFilterOptions() {
        Log.d(TAG, "🔍 显示筛选选项");
        // 实现筛选功能
        Toast.makeText(requireContext(), "筛选功能开发中", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment恢复，刷新数据");
        refreshTags();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "💀 Fragment视图被销毁");
    }
}