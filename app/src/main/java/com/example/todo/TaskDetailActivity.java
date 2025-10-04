// TaskDetailActivity.java
package com.example.todo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todo.databinding.ActivityTaskDetailBinding;
import com.example.todo.model.Task;
import com.example.todo.model.dto.UpdateTaskStatusDTO;
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
 * 任务详情Activity
 */
public class TaskDetailActivity extends AppCompatActivity {
    private static final String TAG = "📖 任务详情Activity";

    private ActivityTaskDetailBinding binding;
    private ApiService apiService;
    private SharedPreferencesManager spManager;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "✅ 任务详情布局加载完成");

        // 获取传递的任务信息
        if (getIntent().hasExtra("task")) {
            currentTask = (Task) getIntent().getSerializableExtra("task");
            Log.d(TAG, "📋 接收到任务数据: " + currentTask.getTitle());
        } else {
            Log.e(TAG, "❌ 未接收到任务数据");
            Toast.makeText(this, "任务数据加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initComponents();
        setupTaskData();
        setupClickListeners();

        Log.d(TAG, "🎉 任务详情Activity初始化完成");
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        // 初始化管理器
        spManager = new SharedPreferencesManager(this);
        apiService = RetrofitClient.getApiService();

        // 设置工具栏
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("任务详情");
        }

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void setupTaskData() {
        Log.d(TAG, "🔄 设置任务数据");

        if (currentTask == null) {
            Log.w(TAG, "❌ 任务数据为空");
            return;
        }

        // 设置任务标题
        binding.textTaskTitle.setText(currentTask.getTitle());

        // 设置任务描述
        if (currentTask.getDescription() != null && !currentTask.getDescription().isEmpty()) {
            binding.textTaskDescription.setText(currentTask.getDescription());
            binding.textTaskDescription.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.textTaskDescription.setVisibility(android.view.View.GONE);
        }

        // 设置任务状态
        boolean isCompleted = "DONE".equals(currentTask.getStatus());
        binding.checkboxCompleted.setChecked(isCompleted);
        updateTaskStatusUI(isCompleted);

        // 设置优先级
        if (currentTask.getPriority() != null) {
            binding.textPriority.setText(getPriorityText(currentTask.getPriority()));
            binding.textPriority.setBackgroundResource(getPriorityBackground(currentTask.getPriority()));
            binding.textPriority.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.textPriority.setVisibility(android.view.View.GONE);
        }

        // 设置截止日期
        if (currentTask.getDueDate() != null && !currentTask.getDueDate().isEmpty()) {
            binding.textDueDate.setText(formatDate(currentTask.getDueDate()));
            binding.textDueDate.setVisibility(android.view.View.VISIBLE);

            // 检查是否过期
            if (!isCompleted && isOverdue(currentTask.getDueDate())) {
                binding.textDueDate.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        } else {
            binding.textDueDate.setVisibility(android.view.View.GONE);
        }

        // 设置分类
        if (currentTask.getList() != null) {
            binding.textCategory.setText(currentTask.getList().getName());
            binding.textCategory.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.textCategory.setVisibility(android.view.View.GONE);
        }

        // 设置标签
        if (currentTask.getTags() != null && !currentTask.getTags().isEmpty()) {
            StringBuilder tagsBuilder = new StringBuilder();
            for (Task.TagInfo tag : currentTask.getTags()) {
                if (tagsBuilder.length() > 0) {
                    tagsBuilder.append(", ");
                }
                tagsBuilder.append(tag.getName());
            }
            binding.textTags.setText(tagsBuilder.toString());
            binding.textTags.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.textTags.setVisibility(android.view.View.GONE);
        }

        // 设置创建时间
        if (currentTask.getCreateTime() != null) {
            binding.textCreateTime.setText("创建时间: " + formatCreateTime(currentTask.getCreateTime()));
            binding.textCreateTime.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.textCreateTime.setVisibility(android.view.View.GONE);
        }

        Log.d(TAG, "✅ 任务数据设置完成");
    }

    private void setupClickListeners() {
        Log.d(TAG, "🔄 设置点击监听器");

        // 返回按钮
        binding.toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "👆 返回按钮被点击");
            finish();
        });

        // 完成任务状态切换
        binding.checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "✅ 任务状态变更: " + currentTask.getTitle() + " -> " + (isChecked ? "完成" : "待办"));
            updateTaskStatus(isChecked ? "DONE" : "TODO");
        });

        // 编辑按钮
        binding.buttonEdit.setOnClickListener(v -> {
            Log.d(TAG, "✏️ 编辑任务: " + currentTask.getTitle());
            editTask();
        });

        // 删除按钮
        binding.buttonDelete.setOnClickListener(v -> {
            Log.d(TAG, "🗑️ 删除任务: " + currentTask.getTitle());
            deleteTask();
        });

        Log.d(TAG, "✅ 点击监听器设置完成");
    }

    private void updateTaskStatusUI(boolean isCompleted) {
        if (isCompleted) {
            binding.textTaskTitle.setPaintFlags(binding.textTaskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            binding.textTaskTitle.setAlpha(0.6f);
        } else {
            binding.textTaskTitle.setPaintFlags(binding.textTaskTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            binding.textTaskTitle.setAlpha(1.0f);
        }
    }

    private void updateTaskStatus(String status) {
        Log.d(TAG, "🔄 更新任务状态: " + currentTask.getTitle() + " -> " + status);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            // 恢复原来的状态
            binding.checkboxCompleted.setChecked(!"DONE".equals(status));
            return;
        }

        UpdateTaskStatusDTO statusDTO = new UpdateTaskStatusDTO(status);

        apiService.updateTaskStatus(currentTask.getId(), statusDTO)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 任务状态更新成功");
                            currentTask.setStatus(status);
                            updateTaskStatusUI("DONE".equals(status));
                            Toast.makeText(TaskDetailActivity.this,
                                    "DONE".equals(status) ? "任务已完成" : "任务已恢复为待办",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "⚠️ 任务状态更新失败: " + processedResponse.getMessage());
                            Toast.makeText(TaskDetailActivity.this, "更新失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            // 恢复原来的状态
                            binding.checkboxCompleted.setChecked(!"DONE".equals(status));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "💥 任务状态更新网络请求失败: " + t.getMessage());
                        Toast.makeText(TaskDetailActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                        // 恢复原来的状态
                        binding.checkboxCompleted.setChecked(!"DONE".equals(status));
                    }
                });
    }

    private void editTask() {
        Log.d(TAG, "🚀 跳转到编辑任务页面");
        android.content.Intent intent = new android.content.Intent(this, CreateTaskActivity.class);
        intent.putExtra("task", currentTask);
        startActivityForResult(intent, 1001);
    }

    private void deleteTask() {
        Log.d(TAG, "🗑️ 删除任务: " + currentTask.getTitle());

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示确认对话框
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("删除任务")
                .setMessage("确定要删除任务 \"" + currentTask.getTitle() + "\" 吗？此操作不可恢复。")
                .setPositiveButton("删除", (d, which) -> {
                    performDeleteTask();
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private void performDeleteTask() {
        Log.d(TAG, "🗑️ 执行删除任务: " + currentTask.getTitle());

        apiService.deleteTask(currentTask.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 任务删除成功");
                            Toast.makeText(TaskDetailActivity.this, "任务已删除", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Log.w(TAG, "⚠️ 任务删除失败: " + processedResponse.getMessage());
                            Toast.makeText(TaskDetailActivity.this, "删除失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e(TAG, "💥 任务删除网络请求失败: " + t.getMessage());
                        Toast.makeText(TaskDetailActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // 任务编辑成功，刷新数据
            Log.d(TAG, "🔄 任务编辑成功，刷新数据");
            // 这里可以重新加载任务数据，或者直接finish并让列表刷新
            setResult(RESULT_OK);
            finish();
        }
    }

    // 工具方法
    private String getPriorityText(String priority) {
        switch (priority) {
            case "HIGH": return "高优先级";
            case "MEDIUM": return "中优先级";
            case "LOW": return "低优先级";
            default: return "普通";
        }
    }

    private int getPriorityBackground(String priority) {
        switch (priority) {
            case "HIGH": return R.drawable.bg_priority_high;
            case "MEDIUM": return R.drawable.bg_priority_medium;
            case "LOW": return R.drawable.bg_priority_low;
            default: return R.drawable.bg_priority_low;
        }
    }

    private String formatDate(String dateString) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (java.text.ParseException e) {
            Log.w(TAG, "📅 日期格式解析失败: " + dateString);
            return dateString;
        }
    }

    private String formatCreateTime(String createTime) {
        try {
            // 假设createTime格式为 "2024-01-15T10:30:00"
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(createTime);
            return outputFormat.format(date);
        } catch (java.text.ParseException e) {
            Log.w(TAG, "📅 创建时间格式解析失败: " + createTime);
            return createTime;
        }
    }

    private boolean isOverdue(String dateString) {
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date dueDate = format.parse(dateString);
            java.util.Date today = new java.util.Date();
            return dueDate != null && dueDate.before(today);
        } catch (java.text.ParseException e) {
            Log.w(TAG, "📅 日期比较失败: " + dateString);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.d(TAG, "💀 Activity被销毁");
    }
}