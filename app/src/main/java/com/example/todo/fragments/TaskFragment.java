package com.example.todo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.todo.CreateTaskActivity;
import com.example.todo.R;
import com.example.todo.adapters.TaskAdapter;
import com.example.todo.databinding.FragmentTaskBinding;
import com.example.todo.model.Task;
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
import java.util.List;

/**
 * 任务管理Fragment
 */
public class TaskFragment extends Fragment {
    private static final String TAG = "📝 任务Fragment";

    private FragmentTaskBinding binding;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private ApiService apiService;
    private SharedPreferencesManager spManager;

    // 当前筛选条件
    private String currentStatus = null;

    // 统计视图
    private TextView textTotalTasks, textTodoTasks, textDoneTasks;
    private int totalTasks = 0, todoTasks = 0, doneTasks = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🎬 Fragment创建视图开始");

        binding = FragmentTaskBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initComponents();
        setupChipGroup();
        loadTasks();

        Log.d(TAG, "✅ 任务Fragment初始化完成");
        return view;
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        // 初始化管理器
        spManager = new SharedPreferencesManager(requireContext());
        apiService = RetrofitClient.getApiService();

        // 初始化统计视图
        textTotalTasks = binding.textTotalTasks;
        textTodoTasks = binding.textTodoTasks;
        textDoneTasks = binding.textDoneTasks;

        // 设置RecyclerView
        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.TaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Log.d(TAG, "👆 任务被点击: " + task.getTitle());
                openTaskDetail(task);
            }

            @Override
            public void onTaskStatusChange(Task task, boolean completed) {
                Log.d(TAG, "🔄 任务状态变更: " + task.getTitle() + " -> " + (completed ? "完成" : "待办"));
                updateTaskStatus(task, completed ? "DONE" : "TODO");
            }

            @Override
            public void onTaskEdit(Task task) {
                Log.d(TAG, "✏️ 编辑任务: " + task.getTitle());
                editTask(task);
            }

            @Override
            public void onTaskDelete(Task task) {
                Log.d(TAG, "🗑️ 删除任务: " + task.getTitle());
                deleteTask(task);
            }
        });

        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTasks.setAdapter(taskAdapter);

        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "🔄 下拉刷新触发");
            refreshTasks();
        });

        // 设置FAB点击事件
        binding.fabAddTask.setOnClickListener(v -> {
            Log.d(TAG, "👆 添加任务FAB被点击");
            createNewTask();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void setupChipGroup() {
        Log.d(TAG, "🔄 设置状态筛选ChipGroup");

        // 初始选中全部
        binding.chipAll.setChecked(true);

        // 设置Chip选中监听
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentStatus = null;
            } else if (checkedId == R.id.chipTodo) {
                currentStatus = "TODO";
            } else if (checkedId == R.id.chipDone) {
                currentStatus = "DONE";
            }
            Log.d(TAG, "📑 状态筛选变更: " + currentStatus);
            refreshTasks();
        });

        Log.d(TAG, "✅ ChipGroup设置完成");
    }

    private void loadTasks() {
        Log.d(TAG, "📥 加载任务数据");

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Log.w(TAG, "❌ 网络不可用，显示本地数据");
            showLocalTasks();
            return;
        }

        showLoading(true);

        // 移除搜索关键词参数，因为TaskFragment没有搜索功能
        apiService.getTasks(currentStatus, null, null, null, null)
                .enqueue(new Callback<ApiResponse<List<Task>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Task>>> call, Response<ApiResponse<List<Task>>> response) {
                        showLoading(false);
                        binding.swipeRefreshLayout.setRefreshing(false);

                        ApiResponse<List<Task>> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                            taskList.clear();
                            taskList.addAll(processedResponse.getData());
                            taskAdapter.notifyDataSetChanged();
                            updateEmptyState();
                            updateTaskStatistics();
                            Log.d(TAG, "✅ 任务数据加载成功，共 " + taskList.size() + " 个任务");
                        } else {
                            Log.w(TAG, "⚠️ 任务数据加载失败: " + processedResponse.getMessage());
                            Toast.makeText(requireContext(), "加载失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Task>>> call, Throwable t) {
                        showLoading(false);
                        binding.swipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, "💥 任务数据网络请求失败: " + t.getMessage());
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                        showLocalTasks();
                    }
                });
    }

    private void showLocalTasks() {
        Log.d(TAG, "📋 显示本地任务数据");
        taskList.clear();
        taskAdapter.notifyDataSetChanged();
        updateEmptyState();
        updateTaskStatistics();
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void updateEmptyState() {
        if (taskList.isEmpty()) {
            binding.textEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewTasks.setVisibility(View.GONE);
        } else {
            binding.textEmpty.setVisibility(View.GONE);
            binding.recyclerViewTasks.setVisibility(View.VISIBLE);
        }
    }

    private void updateTaskStatistics() {
        totalTasks = taskList.size();
        todoTasks = 0;
        doneTasks = 0;

        for (Task task : taskList) {
            if ("DONE".equals(task.getStatus())) {
                doneTasks++;
            } else {
                todoTasks++;
            }
        }

        textTotalTasks.setText(String.valueOf(totalTasks));
        textTodoTasks.setText(String.valueOf(todoTasks));
        textDoneTasks.setText(String.valueOf(doneTasks));

        Log.d(TAG, "📊 任务统计更新 - 总计: " + totalTasks + ", 待办: " + todoTasks + ", 已完成: " + doneTasks);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void refreshTasks() {
        Log.d(TAG, "🔄 刷新任务数据");
        loadTasks();
    }

    private void createNewTask() {
        Log.d(TAG, "➕ 创建新任务");
        Intent intent = new Intent(requireContext(), CreateTaskActivity.class);
        startActivityForResult(intent, 1001);
    }

    private void openTaskDetail(Task task) {
        Log.d(TAG, "📖 打开任务详情: " + task.getTitle());
        Toast.makeText(requireContext(), "打开任务详情: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void editTask(Task task) {
        Log.d(TAG, "✏️ 编辑任务: " + task.getTitle());
        Intent intent = new Intent(requireContext(), CreateTaskActivity.class);
        intent.putExtra("task", task);
        startActivityForResult(intent, 1002);
    }

    private void updateTaskStatus(Task task, String status) {
        Log.d(TAG, "🔄 更新任务状态: " + task.getTitle() + " -> " + status);

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.todo.model.dto.UpdateTaskStatusDTO statusDTO =
                new com.example.todo.model.dto.UpdateTaskStatusDTO(status);

        apiService.updateTaskStatus(task.getId(), statusDTO)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 任务状态更新成功");
                            refreshTasks();
                        } else {
                            Log.w(TAG, "⚠️ 任务状态更新失败: " + processedResponse.getMessage());
                            Toast.makeText(requireContext(), "更新失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            // 刷新以恢复状态
                            refreshTasks();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "💥 任务状态更新网络请求失败: " + t.getMessage());
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                        // 刷新以恢复状态
                        refreshTasks();
                    }
                });
    }

    private void deleteTask(Task task) {
        Log.d(TAG, "🗑️ 删除任务: " + task.getTitle());

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示确认对话框
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("删除任务")
                .setMessage("确定要删除任务 \"" + task.getTitle() + "\" 吗？此操作不可恢复。")
                .setPositiveButton("删除", (d, which) -> {
                    performDeleteTask(task);
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void performDeleteTask(Task task) {
        Log.d(TAG, "🗑️ 执行删除任务: " + task.getTitle());

        apiService.deleteTask(task.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 任务删除成功");
                            refreshTasks();
                            Toast.makeText(requireContext(), "任务已删除", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "⚠️ 任务删除失败: " + processedResponse.getMessage());
                            Toast.makeText(requireContext(), "删除失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "💥 任务删除网络请求失败: " + t.getMessage());
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 || requestCode == 1002) {
            refreshTasks();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment恢复，刷新数据");
        refreshTasks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "💀 Fragment视图被销毁");
    }
}