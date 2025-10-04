package com.example.todo;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.todo.databinding.DialogCreateCategoryBinding;
import com.example.todo.model.List;
import com.example.todo.model.dto.CreateListDTO;
import com.example.todo.model.dto.UpdateListDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 创建/编辑分类对话框
 */
public class CreateCategoryDialog extends DialogFragment {
    private static final String TAG = "📁 创建分类对话框";

    private DialogCreateCategoryBinding binding;
    private ApiService apiService;

    private List editingCategory = null;
    private CategoryCreatedListener categoryCreatedListener;

    public interface CategoryCreatedListener {
        void onCategoryCreated();
    }

    public static CreateCategoryDialog newInstance(List category) {
        CreateCategoryDialog dialog = new CreateCategoryDialog();
        if (category != null) {
            Bundle args = new Bundle();
            args.putSerializable("category", category);
            dialog.setArguments(args);
        }
        return dialog;
    }

    public void setCategoryCreatedListener(CategoryCreatedListener listener) {
        this.categoryCreatedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "🎬 对话框创建开始");

        // 检查是否是编辑模式
        if (getArguments() != null && getArguments().containsKey("category")) {
            editingCategory = (List) getArguments().getSerializable("category");
            Log.d(TAG, "✏️ 编辑模式，分类: " + editingCategory.getName());
        }

        binding = DialogCreateCategoryBinding.inflate(LayoutInflater.from(getContext()));
        View view = binding.getRoot();

        initComponents();
        setupFormData();

        // 创建对话框并设置标题
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(view);

        // 动态设置标题
        if (editingCategory != null) {
            builder.setTitle("编辑分类");
        } else {
            builder.setTitle("创建分类");
        }

        AlertDialog dialog = builder.create();
        Log.d(TAG, "✅ 对话框创建完成");
        return dialog;
    }

    private void initComponents() {
        Log.d(TAG, "🔄 初始化组件");

        apiService = RetrofitClient.getApiService();

        // 设置按钮点击事件
        binding.buttonCancel.setOnClickListener(v -> {
            Log.d(TAG, "❌ 取消按钮被点击");
            dismiss();
        });

        binding.buttonSave.setOnClickListener(v -> {
            Log.d(TAG, "💾 保存按钮被点击");
            saveCategory();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void setupFormData() {
        Log.d(TAG, "🔄 设置表单数据");

        if (editingCategory != null) {
            // 填充编辑数据
            binding.editTextCategoryName.setText(editingCategory.getName());
            if (editingCategory.getDescription() != null) {
                binding.editTextCategoryDescription.setText(editingCategory.getDescription());
            }
            binding.buttonSave.setText("更新");
            Log.d(TAG, "✅ 编辑数据填充完成");
        }
    }

    private void saveCategory() {
        Log.d(TAG, "💾 开始保存分类");

        // 验证输入
        String name = binding.editTextCategoryName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.editTextCategoryName.setError("请输入分类名称");
            Log.w(TAG, "❌ 分类名称为空");
            return;
        }

        String description = binding.editTextCategoryDescription.getText().toString().trim();

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "❌ 网络不可用，无法保存分类");
            return;
        }

        if (editingCategory != null) {
            // 更新分类
            updateCategory(name, description);
        } else {
            // 创建分类
            createCategory(name, description);
        }
    }

    private void createCategory(String name, String description) {
        Log.d(TAG, "🆕 创建新分类");

        showLoading(true);

        CreateListDTO createListDTO = new CreateListDTO(name, description);

        apiService.createList(createListDTO).enqueue(new Callback<ApiResponse<List>>() {
            @Override
            public void onResponse(Call<ApiResponse<List>> call, Response<ApiResponse<List>> response) {
                showLoading(false);

                ApiResponse<List> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess()) {
                    Log.d(TAG, "✅ 分类创建成功");
                    Toast.makeText(requireContext(), "分类创建成功", Toast.LENGTH_SHORT).show();
                    if (categoryCreatedListener != null) {
                        categoryCreatedListener.onCategoryCreated();
                    }
                    dismiss();
                } else {
                    Log.w(TAG, "⚠️ 分类创建失败: " + processedResponse.getMessage());
                    Toast.makeText(requireContext(), "创建失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "💥 分类创建网络请求失败: " + t.getMessage());
                Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategory(String name, String description) {
        Log.d(TAG, "✏️ 更新分类");

        showLoading(true);

        UpdateListDTO updateListDTO = new UpdateListDTO(name, description);

        apiService.updateList(editingCategory.getId(), updateListDTO)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        showLoading(false);

                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 分类更新成功");
                            Toast.makeText(requireContext(), "分类更新成功", Toast.LENGTH_SHORT).show();
                            if (categoryCreatedListener != null) {
                                categoryCreatedListener.onCategoryCreated();
                            }
                            dismiss();
                        } else {
                            Log.w(TAG, "⚠️ 分类更新失败: " + processedResponse.getMessage());
                            Toast.makeText(requireContext(), "更新失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "💥 分类更新网络请求失败: " + t.getMessage());
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.buttonSave.setEnabled(!show);
        binding.buttonSave.setText(show ? "保存中..." : (editingCategory != null ? "更新" : "保存"));

        if (show) {
            binding.buttonSave.setIcon(null);
        } else {
            binding.buttonSave.setIconResource(R.drawable.ic_save);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "💀 对话框视图被销毁");
    }
}