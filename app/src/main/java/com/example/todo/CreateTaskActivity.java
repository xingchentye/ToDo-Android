package com.example.todo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todo.databinding.ActivityCreateTaskBinding;
import com.example.todo.model.List;
import com.example.todo.model.Tag;
import com.example.todo.model.Task;
import com.example.todo.model.dto.CreateTaskDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.storage.SharedPreferencesManager;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.NetworkUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 创建/编辑任务Activity
 */
public class CreateTaskActivity extends AppCompatActivity {
    private static final String TAG = "➕ 创建任务Activity";

    private ActivityCreateTaskBinding binding;
    private ApiService apiService;
    private SharedPreferencesManager spManager;

    private java.util.List<List> categoryList = new ArrayList<>();
    private java.util.List<Tag> tagList = new ArrayList<>();
    private java.util.List<Integer> selectedTagIds = new ArrayList<>();
    private ArrayAdapter<List> categoryAdapter;

    private String selectedPriority = "MEDIUM";
    private String selectedDueDate = null;
    private Integer selectedCategoryId = null;

    private Task editingTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🎬 Activity创建开始");

        binding = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "✅ 创建任务布局加载完成");

        // 检查是否是编辑模式
        if (getIntent().hasExtra("task")) {
            editingTask = (Task) getIntent().getSerializableExtra("task");
            Log.d(TAG, "✏️ 编辑模式，任务: " + editingTask.getTitle());
            setTitle("编辑任务");
        } else {
            Log.d(TAG, "➕ 创建模式");
            setTitle("创建任务");
        }

        initComponents();
        loadCategories();
        loadTags();
        setupFormData();

        Log.d(TAG, "🎉 创建任务Activity初始化完成");
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        // 初始化管理器
        spManager = new SharedPreferencesManager(this);
        apiService = RetrofitClient.getApiService();

        // 设置优先级选择
        setupPrioritySelection();

        // 设置日期选择
        setupDatePicker();

        // 设置分类适配器
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        // 设置按钮点击事件
        binding.buttonCancel.setOnClickListener(v -> {
            Log.d(TAG, "❌ 取消按钮被点击");
            finish();
        });

        binding.buttonSave.setOnClickListener(v -> {
            Log.d(TAG, "💾 保存按钮被点击");
            saveTask();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void setupPrioritySelection() {
        Log.d(TAG, "🔄 设置优先级选择");

        // 设置默认选中中优先级
        binding.chipPriorityMedium.setChecked(true);

        binding.chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipPriorityLow) {
                selectedPriority = "LOW";
                Log.d(TAG, "📊 选择低优先级");
            } else if (checkedId == R.id.chipPriorityMedium) {
                selectedPriority = "MEDIUM";
                Log.d(TAG, "📊 选择中优先级");
            } else if (checkedId == R.id.chipPriorityHigh) {
                selectedPriority = "HIGH";
                Log.d(TAG, "📊 选择高优先级");
            }
        });

        Log.d(TAG, "✅ 优先级选择设置完成");
    }

    private void setupDatePicker() {
        Log.d(TAG, "🔄 设置日期选择器");

        binding.buttonDueDate.setOnClickListener(v -> {
            Log.d(TAG, "📅 打开日期选择器");

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        selectedDueDate = formattedDate;
                        binding.textSelectedDate.setText(formattedDate);
                        Log.d(TAG, "📅 选择日期: " + formattedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        Log.d(TAG, "✅ 日期选择器设置完成");
    }

    private void loadCategories() {
        Log.d(TAG, "📥 加载分类数据");

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.w(TAG, "❌ 网络不可用，跳过加载分类");
            return;
        }

        apiService.getLists().enqueue(new Callback<ApiResponse<java.util.List<List>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<List>>> call,
                                   Response<ApiResponse<java.util.List<List>>> response) {
                ApiResponse<java.util.List<List>> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    categoryList.clear();
                    categoryList.addAll(processedResponse.getData());
                    categoryAdapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ 分类数据加载成功，共 " + categoryList.size() + " 个分类");
                } else {
                    Log.w(TAG, "⚠️ 分类数据加载失败: " + processedResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<List>>> call, Throwable t) {
                Log.e(TAG, "💥 分类数据网络请求失败: " + t.getMessage());
            }
        });
    }

    private void loadTags() {
        Log.d(TAG, "📥 加载标签数据");

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.w(TAG, "❌ 网络不可用，跳过加载标签");
            return;
        }

        apiService.getTags().enqueue(new Callback<ApiResponse<java.util.List<Tag>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<Tag>>> call,
                                   Response<ApiResponse<java.util.List<Tag>>> response) {
                ApiResponse<java.util.List<Tag>> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess() && processedResponse.getData() != null) {
                    tagList.clear();
                    tagList.addAll(processedResponse.getData());
                    setupTagChips();
                    Log.d(TAG, "✅ 标签数据加载成功，共 " + tagList.size() + " 个标签");
                } else {
                    Log.w(TAG, "⚠️ 标签数据加载失败: " + processedResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<Tag>>> call, Throwable t) {
                Log.e(TAG, "💥 标签数据网络请求失败: " + t.getMessage());
            }
        });
    }

    private void setupTagChips() {
        Log.d(TAG, "🔄 设置标签芯片");

        binding.chipGroupTags.removeAllViews();

        for (Tag tag : tagList) {
            Chip chip = new Chip(this);
            chip.setText(tag.getName());
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.surfaceVariant);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTagIds.add(tag.getId());
                    Log.d(TAG, "🏷️ 选择标签: " + tag.getName());
                } else {
                    selectedTagIds.remove(Integer.valueOf(tag.getId()));
                    Log.d(TAG, "🏷️ 取消选择标签: " + tag.getName());
                }
            });

            binding.chipGroupTags.addView(chip);
        }

        Log.d(TAG, "✅ 标签芯片设置完成");
    }

    private void setupFormData() {
        Log.d(TAG, "🔄 设置表单数据");

        if (editingTask != null) {
            // 填充编辑数据
            binding.editTextTitle.setText(editingTask.getTitle());
            binding.editTextDescription.setText(editingTask.getDescription());

            // 设置优先级
            if (editingTask.getPriority() != null) {
                selectedPriority = editingTask.getPriority();
                switch (editingTask.getPriority()) {
                    case "LOW":
                        binding.chipPriorityLow.setChecked(true);
                        break;
                    case "MEDIUM":
                        binding.chipPriorityMedium.setChecked(true);
                        break;
                    case "HIGH":
                        binding.chipPriorityHigh.setChecked(true);
                        break;
                }
            }

            // 设置截止日期
            if (editingTask.getDueDate() != null) {
                selectedDueDate = editingTask.getDueDate();
                binding.textSelectedDate.setText(editingTask.getDueDate());
            }

            // 设置分类（需要等待分类加载完成后设置）
            if (editingTask.getList() != null) {
                selectedCategoryId = editingTask.getList().getId();
            }

            // 设置标签（需要等待标签加载完成后设置）
            if (editingTask.getTags() != null) {
                for (Task.TagInfo tag : editingTask.getTags()) {
                    selectedTagIds.add(tag.getId());
                }
            }

            Log.d(TAG, "✅ 编辑数据填充完成");
        }
    }

    private void saveTask() {
        Log.d(TAG, "💾 开始保存任务");

        // 验证输入
        String title = binding.editTextTitle.getText().toString().trim();
        if (title.isEmpty()) {
            binding.editTextTitle.setError("请输入任务标题");
            Log.w(TAG, "❌ 任务标题为空");
            return;
        }

        String description = binding.editTextDescription.getText().toString().trim();

        // 获取选择的分类
        List selectedCategory = (List) binding.spinnerCategory.getSelectedItem();
        if (selectedCategory != null) {
            selectedCategoryId = selectedCategory.getId();
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "❌ 网络不可用，无法保存任务");
            return;
        }

        // 创建任务DTO
        CreateTaskDTO taskDTO = new CreateTaskDTO(title, description, selectedDueDate, selectedPriority, selectedCategoryId);
        taskDTO.setTagIds(selectedTagIds);

        Log.d(TAG, "📦 创建任务数据: " + taskDTO.toString());

        if (editingTask != null) {
            // 更新任务
            updateTask(taskDTO);
        } else {
            // 创建任务
            createTask(taskDTO);
        }
    }

    private void createTask(CreateTaskDTO taskDTO) {
        Log.d(TAG, "🆕 创建新任务");

        showLoading(true);

        apiService.createTask(taskDTO).enqueue(new Callback<ApiResponse<Task>>() {
            @Override
            public void onResponse(Call<ApiResponse<Task>> call, Response<ApiResponse<Task>> response) {
                showLoading(false);

                ApiResponse<Task> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess()) {
                    Log.d(TAG, "✅ 任务创建成功");
                    Toast.makeText(CreateTaskActivity.this, "任务创建成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.w(TAG, "⚠️ 任务创建失败: " + processedResponse.getMessage());
                    Toast.makeText(CreateTaskActivity.this, "创建失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Task>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "💥 任务创建网络请求失败: " + t.getMessage());
                Toast.makeText(CreateTaskActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTask(CreateTaskDTO taskDTO) {
        Log.d(TAG, "✏️ 更新任务");

        showLoading(true);

        // 注意：这里需要将CreateTaskDTO转换为UpdateTaskDTO
        // 由于API设计，我们需要使用不同的DTO
        com.example.todo.model.dto.UpdateTaskDTO updateTaskDTO = new com.example.todo.model.dto.UpdateTaskDTO();
        updateTaskDTO.setTitle(taskDTO.getTitle());
        updateTaskDTO.setDescription(taskDTO.getDescription());
        updateTaskDTO.setDueDate(taskDTO.getDueDate());
        updateTaskDTO.setPriority(taskDTO.getPriority());
        updateTaskDTO.setListId(taskDTO.getListId());
        updateTaskDTO.setTagIds(taskDTO.getTagIds());

        apiService.updateTask(editingTask.getId(), updateTaskDTO)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        showLoading(false);

                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 任务更新成功");
                            Toast.makeText(CreateTaskActivity.this, "任务更新成功", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Log.w(TAG, "⚠️ 任务更新失败: " + processedResponse.getMessage());
                            Toast.makeText(CreateTaskActivity.this, "更新失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "💥 任务更新网络请求失败: " + t.getMessage());
                        Toast.makeText(CreateTaskActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.buttonSave.setEnabled(!show);
        binding.buttonSave.setText(show ? "保存中..." : "保存");

        if (show) {
            binding.buttonSave.setIcon(null);
        } else {
            binding.buttonSave.setIconResource(R.drawable.ic_save);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.d(TAG, "💀 Activity被销毁");
    }
}