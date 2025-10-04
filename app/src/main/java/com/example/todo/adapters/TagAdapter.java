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
import com.example.todo.model.Tag;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 标签列表适配器
 */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    private static final String TAG = "🏷️ 标签适配器";

    private List<Tag> tagList;
    private TagClickListener listener;

    public interface TagClickListener {
        void onTagClick(Tag tag);
        void onTagEdit(Tag tag);
        void onTagDelete(Tag tag);
    }

    public TagAdapter(List<Tag> tagList, TagClickListener listener) {
        this.tagList = tagList;
        this.listener = listener;
        Log.d(TAG, "🔄 标签适配器初始化，标签数量: " + tagList.size());
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "🆕 创建视图持有者");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tagList.get(position);
        Log.d(TAG, "📝 绑定视图持有者 - 位置: " + position + ", 标签: " + tag.getName());

        holder.bind(tag, listener);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public void updateData(List<Tag> newTags) {
        Log.d(TAG, "🔄 更新适配器数据，新标签数量: " + newTags.size());
        tagList.clear();
        tagList.addAll(newTags);
        notifyDataSetChanged();
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageTagIcon;
        private TextView textTagName;
        private TextView textTaskCount;
        private TextView textCreateTime;
        private ImageButton buttonMenu;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "🆕 创建标签视图持有者");

            imageTagIcon = itemView.findViewById(R.id.imageTagIcon);
            textTagName = itemView.findViewById(R.id.textTagName);
            textTaskCount = itemView.findViewById(R.id.textTaskCount);
            textCreateTime = itemView.findViewById(R.id.textCreateTime);
            buttonMenu = itemView.findViewById(R.id.buttonMenu);
        }

        public void bind(Tag tag, TagClickListener listener) {
            Log.d(TAG, "🔗 绑定标签数据: " + tag.getName());

            // 设置标签名称
            textTagName.setText(tag.getName());

            // 设置任务数量（暂时显示0，需要从API获取）
            textTaskCount.setText("0 个任务");

            // 设置创建时间
            if (tag.getCreateTime() != null) {
                String formattedTime = formatCreateTime(tag.getCreateTime());
                textCreateTime.setText("创建于 " + formattedTime);
                textCreateTime.setVisibility(View.VISIBLE);
            } else {
                textCreateTime.setVisibility(View.GONE);
            }

            // 设置标签图标颜色（可以根据标签ID生成不同颜色）
            int colorRes = getTagColor(tag.getId());
            imageTagIcon.setColorFilter(itemView.getContext().getColor(colorRes));

            // 设置点击监听器
            itemView.setOnClickListener(v -> {
                Log.d(TAG, "👆 标签被点击: " + tag.getName());
                listener.onTagClick(tag);
            });

            // 设置菜单按钮 - 修复：调用setupMenuButton方法
            setupMenuButton(tag, listener);

            Log.d(TAG, "✅ 标签数据绑定完成");
        }

        // 在TagViewHolder类中添加菜单功能
        private void setupMenuButton(Tag tag, TagClickListener listener) {
            buttonMenu.setOnClickListener(v -> {
                Log.d(TAG, "📋 打开标签菜单: " + tag.getName());

                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), buttonMenu);
                popupMenu.inflate(R.menu.menu_item_actions);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        Log.d(TAG, "✏️ 编辑标签: " + tag.getName());
                        listener.onTagEdit(tag);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        Log.d(TAG, "🗑️ 删除标签: " + tag.getName());
                        listener.onTagDelete(tag);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
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

        private int getTagColor(int tagId) {
            int[] colors = {
                    R.color.primary,
                    R.color.secondary,
                    R.color.error,
                    R.color.success,
                    R.color.warning,
                    R.color.info
            };
            return colors[Math.abs(tagId) % colors.length];
        }
    }
}