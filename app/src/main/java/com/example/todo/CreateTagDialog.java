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
import com.example.todo.databinding.DialogCreateTagBinding;
import com.example.todo.model.Tag;
import com.example.todo.model.dto.CreateTagDTO;
import com.example.todo.model.dto.UpdateTagDTO;
import com.example.todo.model.response.ApiResponse;
import com.example.todo.network.ApiService;
import com.example.todo.network.RetrofitClient;
import com.example.todo.utils.ApiResponseHandler;
import com.example.todo.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 创建/编辑标签对话框
 */
public class CreateTagDialog extends DialogFragment {
    private static final String TAG = "🏷️ 创建标签对话框";

    private DialogCreateTagBinding binding;
    private ApiService apiService;

    private Tag editingTag = null;
    private TagCreatedListener tagCreatedListener;

    public interface TagCreatedListener {
        void onTagCreated();
    }

    public static CreateTagDialog newInstance(Tag tag) {
        CreateTagDialog dialog = new CreateTagDialog();
        if (tag != null) {
            Bundle args = new Bundle();
            args.putSerializable("tag", tag);
            dialog.setArguments(args);
        }
        return dialog;
    }

    public void setTagCreatedListener(TagCreatedListener listener) {
        this.tagCreatedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "🎬 对话框创建开始");

        // 检查是否是编辑模式
        if (getArguments() != null && getArguments().containsKey("tag")) {
            editingTag = (Tag) getArguments().getSerializable("tag");
            Log.d(TAG, "✏️ 编辑模式，标签: " + editingTag.getName());
        }

        binding = DialogCreateTagBinding.inflate(LayoutInflater.from(getContext()));
        View view = binding.getRoot();

        initComponents();
        setupFormData();

        // 创建对话框并设置标题
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(view);

        // 动态设置标题
        if (editingTag != null) {
            builder.setTitle("编辑标签");
        } else {
            builder.setTitle("创建标签");
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
            saveTag();
        });

        Log.d(TAG, "✅ 组件初始化完成");
    }

    private void setupFormData() {
        Log.d(TAG, "🔄 设置表单数据");

        if (editingTag != null) {
            // 填充编辑数据
            binding.editTextTagName.setText(editingTag.getName());
            binding.buttonSave.setText("更新");
            Log.d(TAG, "✅ 编辑数据填充完成");
        }
    }

    private void saveTag() {
        Log.d(TAG, "💾 开始保存标签");

        // 验证输入
        String name = binding.editTextTagName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.editTextTagName.setError("请输入标签名称");
            Log.w(TAG, "❌ 标签名称为空");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "❌ 网络不可用，无法保存标签");
            return;
        }

        if (editingTag != null) {
            // 更新标签
            updateTag(name);
        } else {
            // 创建标签
            createTag(name);
        }
    }

    private void createTag(String name) {
        Log.d(TAG, "🆕 创建新标签");

        showLoading(true);

        CreateTagDTO createTagDTO = new CreateTagDTO(name);

        apiService.createTag(createTagDTO).enqueue(new Callback<ApiResponse<Tag>>() {
            @Override
            public void onResponse(Call<ApiResponse<Tag>> call, Response<ApiResponse<Tag>> response) {
                showLoading(false);

                ApiResponse<Tag> processedResponse = ApiResponseHandler.processResponse(response);

                if (processedResponse.isSuccess()) {
                    Log.d(TAG, "✅ 标签创建成功");
                    Toast.makeText(requireContext(), "标签创建成功", Toast.LENGTH_SHORT).show();
                    if (tagCreatedListener != null) {
                        tagCreatedListener.onTagCreated();
                    }
                    dismiss();
                } else {
                    Log.w(TAG, "⚠️ 标签创建失败: " + processedResponse.getMessage());
                    Toast.makeText(requireContext(), "创建失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Tag>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "💥 标签创建网络请求失败: " + t.getMessage());
                Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTag(String name) {
        Log.d(TAG, "✏️ 更新标签");

        showLoading(true);

        UpdateTagDTO updateTagDTO = new UpdateTagDTO(name);

        apiService.updateTag(editingTag.getId(), updateTagDTO)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        showLoading(false);

                        ApiResponse<Void> processedResponse = ApiResponseHandler.processResponse(response);

                        if (processedResponse.isSuccess()) {
                            Log.d(TAG, "✅ 标签更新成功");
                            Toast.makeText(requireContext(), "标签更新成功", Toast.LENGTH_SHORT).show();
                            if (tagCreatedListener != null) {
                                tagCreatedListener.onTagCreated();
                            }
                            dismiss();
                        } else {
                            Log.w(TAG, "⚠️ 标签更新失败: " + processedResponse.getMessage());
                            Toast.makeText(requireContext(), "更新失败: " + processedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "💥 标签更新网络请求失败: " + t.getMessage());
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.buttonSave.setEnabled(!show);
        binding.buttonSave.setText(show ? "保存中..." : (editingTag != null ? "更新" : "保存"));

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