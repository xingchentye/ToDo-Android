package com.example.todo.adapters;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todo.R;
import com.example.todo.model.Task;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 任务列表适配器
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private static final String TAG = "📋 任务适配器";

    private List<Task> taskList;
    private TaskClickListener listener;

    public interface TaskClickListener {
        void onTaskClick(Task task);
        void onTaskStatusChange(Task task, boolean completed);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
    }

    public TaskAdapter(List<Task> taskList, TaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
        Log.d(TAG, "🔄 任务适配器初始化，任务数量: " + taskList.size());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "🆕 创建视图持有者");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        Log.d(TAG, "📝 绑定视图持有者 - 位置: " + position + ", 任务: " + task.getTitle());

        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateData(List<Task> newTasks) {
        Log.d(TAG, "🔄 更新适配器数据，新任务数量: " + newTasks.size());
        taskList.clear();
        taskList.addAll(newTasks);
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private View priorityIndicator;
        private CheckBox checkBoxCompleted;
        private TextView textTaskTitle;
        private TextView textTaskDescription;
        private Chip chipPriority;
        private Chip chipCategory;
        private Chip chipDueDate;
        private Chip chipTags;
        private ImageButton buttonMenu;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "🆕 创建任务视图持有者");

            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            textTaskTitle = itemView.findViewById(R.id.textTaskTitle);
            textTaskDescription = itemView.findViewById(R.id.textTaskDescription);
            chipPriority = itemView.findViewById(R.id.chipPriority);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            chipDueDate = itemView.findViewById(R.id.chipDueDate);
            chipTags = itemView.findViewById(R.id.chipTags);
            buttonMenu = itemView.findViewById(R.id.buttonMenu);
        }

        public void bind(Task task, TaskClickListener listener) {
            Log.d(TAG, "🔗 绑定任务数据: " + task.getTitle());

            // 设置任务状态
            boolean isCompleted = "DONE".equals(task.getStatus());
            checkBoxCompleted.setChecked(isCompleted);

            // 设置任务标题（添加删除线如果已完成）
            textTaskTitle.setText(task.getTitle());
            if (isCompleted) {
                textTaskTitle.setPaintFlags(textTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textTaskTitle.setAlpha(0.6f);
            } else {
                textTaskTitle.setPaintFlags(textTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textTaskTitle.setAlpha(1.0f);
            }

            // 设置任务描述
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                textTaskDescription.setText(task.getDescription());
                textTaskDescription.setVisibility(View.VISIBLE);
            } else {
                textTaskDescription.setVisibility(View.GONE);
            }

            // 设置优先级指示器和芯片
            if (task.getPriority() != null) {
                priorityIndicator.setVisibility(View.VISIBLE);
                chipPriority.setVisibility(View.VISIBLE);

                switch (task.getPriority()) {
                    case "HIGH":
                        priorityIndicator.setBackgroundColor(itemView.getContext().getColor(R.color.priority_high));
                        chipPriority.setText("高");
                        chipPriority.setChipBackgroundColorResource(R.color.priority_high);
                        break;
                    case "MEDIUM":
                        priorityIndicator.setBackgroundColor(itemView.getContext().getColor(R.color.priority_medium));
                        chipPriority.setText("中");
                        chipPriority.setChipBackgroundColorResource(R.color.priority_medium);
                        break;
                    case "LOW":
                        priorityIndicator.setBackgroundColor(itemView.getContext().getColor(R.color.priority_low));
                        chipPriority.setText("低");
                        chipPriority.setChipBackgroundColorResource(R.color.priority_low);
                        break;
                    default:
                        priorityIndicator.setVisibility(View.GONE);
                        chipPriority.setVisibility(View.GONE);
                }
            } else {
                priorityIndicator.setVisibility(View.GONE);
                chipPriority.setVisibility(View.GONE);
            }

            // 设置分类
            if (task.getList() != null && task.getList().getName() != null) {
                chipCategory.setText(task.getList().getName());
                chipCategory.setVisibility(View.VISIBLE);
            } else {
                chipCategory.setVisibility(View.GONE);
            }

            // 设置截止日期
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                String formattedDate = formatDate(task.getDueDate());
                chipDueDate.setText(formattedDate);
                chipDueDate.setVisibility(View.VISIBLE);

                // 检查是否过期
                if (!isCompleted && isOverdue(task.getDueDate())) {
                    chipDueDate.setChipBackgroundColorResource(R.color.error);
                    chipDueDate.setTextColor(itemView.getContext().getColor(R.color.onError));
                } else {
                    chipDueDate.setChipBackgroundColorResource(R.color.surfaceVariant);
                    chipDueDate.setTextColor(itemView.getContext().getColor(R.color.onSurfaceVariant));
                }
            } else {
                chipDueDate.setVisibility(View.GONE);
            }

            // 设置标签
            if (task.getTags() != null && !task.getTags().isEmpty()) {
                int tagCount = task.getTags().size();
                chipTags.setText(tagCount + "个标签");
                chipTags.setVisibility(View.VISIBLE);
            } else {
                chipTags.setVisibility(View.GONE);
            }

            // 设置菜单按钮
            setupMenuButton(task, listener);

            // 设置点击监听器
            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "✅ 任务状态变更: " + task.getTitle() + " -> " + (isChecked ? "完成" : "待办"));
                listener.onTaskStatusChange(task, isChecked);
            });

            itemView.setOnClickListener(v -> {
                Log.d(TAG, "👆 任务被点击: " + task.getTitle());
                listener.onTaskClick(task);
            });

            Log.d(TAG, "✅ 任务数据绑定完成");
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException e) {
                Log.w(TAG, "📅 日期格式解析失败: " + dateString);
                return dateString;
            }
        }

        // 在TaskViewHolder类中添加菜单功能
        private void setupMenuButton(Task task, TaskClickListener listener) {
            buttonMenu.setOnClickListener(v -> {
                Log.d(TAG, "📋 打开任务菜单: " + task.getTitle());

                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), buttonMenu);
                popupMenu.inflate(R.menu.menu_item_actions);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        Log.d(TAG, "✏️ 编辑任务: " + task.getTitle());
                        listener.onTaskEdit(task);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        Log.d(TAG, "🗑️ 删除任务: " + task.getTitle());
                        listener.onTaskDelete(task);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }

        private boolean isOverdue(String dateString) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dueDate = format.parse(dateString);
                Date today = new Date();
                // 清除时间部分，只比较日期
                format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dueDate = format.parse(format.format(dueDate));
                today = format.parse(format.format(today));
                return dueDate != null && dueDate.before(today);
            } catch (ParseException e) {
                Log.w(TAG, "📅 日期比较失败: " + dateString);
                return false;
            }
        }
    }
}