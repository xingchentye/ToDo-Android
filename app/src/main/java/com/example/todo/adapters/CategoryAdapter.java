package com.example.todo.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todo.R;
import com.example.todo.model.List;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 分类列表适配器
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private static final String TAG = "📁 分类适配器";

    private java.util.List<List> categoryList;
    private CategoryClickListener listener;

    public interface CategoryClickListener {
        void onCategoryClick(List category);
        void onCategoryEdit(List category);
        void onCategoryDelete(List category);
    }

    public CategoryAdapter(java.util.List<List> categoryList, CategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
        Log.d(TAG, "🔄 分类适配器初始化，分类数量: " + categoryList.size());
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "🆕 创建视图持有者");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        List category = categoryList.get(position);
        Log.d(TAG, "📝 绑定视图持有者 - 位置: " + position + ", 分类: " + category.getName());
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateData(java.util.List<List> newCategories) {
        Log.d(TAG, "🔄 更新适配器数据，新分类数量: " + newCategories.size());
        categoryList.clear();
        categoryList.addAll(newCategories);
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageCategoryIcon;
        private TextView textCategoryName;
        private TextView textCategoryDescription;
        private TextView textTaskCount;
        private TextView textCreateTime;
        private ImageButton buttonMenu;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "🆕 创建分类视图持有者");

            imageCategoryIcon = itemView.findViewById(R.id.imageCategoryIcon);
            textCategoryName = itemView.findViewById(R.id.textCategoryName);
            textCategoryDescription = itemView.findViewById(R.id.textCategoryDescription);
            textTaskCount = itemView.findViewById(R.id.textTaskCount);
            textCreateTime = itemView.findViewById(R.id.textCreateTime);
            buttonMenu = itemView.findViewById(R.id.buttonMenu);
        }

        public void bind(List category, CategoryClickListener listener) {
            Log.d(TAG, "🔗 绑定分类数据: " + category.getName());

            // 设置分类名称
            textCategoryName.setText(category.getName());

            // 设置分类描述
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                textCategoryDescription.setText(category.getDescription());
                textCategoryDescription.setVisibility(View.VISIBLE);
            } else {
                textCategoryDescription.setVisibility(View.GONE);
            }

            // 设置任务数量（暂时显示0，需要从API获取）
            textTaskCount.setText("0 个任务");

            // 设置创建时间
            if (category.getCreateTime() != null) {
                String formattedTime = formatCreateTime(category.getCreateTime());
                textCreateTime.setText("创建于 " + formattedTime);
                textCreateTime.setVisibility(View.VISIBLE);
            } else {
                textCreateTime.setVisibility(View.GONE);
            }

            // 设置分类图标颜色（可以根据分类ID生成不同颜色）
            int colorRes = getCategoryColor(category.getId());
            imageCategoryIcon.setColorFilter(itemView.getContext().getColor(colorRes));

            // 设置点击监听器
            itemView.setOnClickListener(v -> {
                Log.d(TAG, "👆 分类被点击: " + category.getName());
                listener.onCategoryClick(category);
            });

            // 设置菜单按钮 - 修复：调用setupMenuButton方法
            setupMenuButton(category, listener);

            Log.d(TAG, "✅ 分类数据绑定完成");
        }

        private String formatCreateTime(String createTime) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(createTime);
                return outputFormat.format(date);
            } catch (ParseException e) {
                Log.w(TAG, "📅 创建时间格式解析失败: " + createTime);
                return createTime;
            }
        }

        // 在CategoryViewHolder类中添加菜单功能
        private void setupMenuButton(List category, CategoryClickListener listener) {
            buttonMenu.setOnClickListener(v -> {
                Log.d(TAG, "📋 打开分类菜单: " + category.getName());

                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), buttonMenu);
                popupMenu.inflate(R.menu.menu_item_actions);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        Log.d(TAG, "✏️ 编辑分类: " + category.getName());
                        listener.onCategoryEdit(category);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        Log.d(TAG, "🗑️ 删除分类: " + category.getName());
                        listener.onCategoryDelete(category);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }

        private int getCategoryColor(int categoryId) {
            int[] colors = {
                    R.color.primary,
                    R.color.secondary,
                    R.color.error,
                    R.color.success,
                    R.color.warning,
                    R.color.info
            };
            return colors[Math.abs(categoryId) % colors.length];
        }
    }
}