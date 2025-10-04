package com.example.todo.model;

import java.util.List;
import java.io.Serializable;

public class Task implements Serializable {
    private int id;
    private String title;
    private String description;
    private String status; // TODO, DONE
    private String priority; // LOW, MEDIUM, HIGH
    private String dueDate;
    private ListInfo list;
    private List<TagInfo> tags;
    private int ownerId;
    private String createTime;

    public static class ListInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private int id;
        private String name;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public String toString() {
            return "ListInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class TagInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private int id;
        private String name;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public String toString() {
            return "TagInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public ListInfo getList() { return list; }
    public void setList(ListInfo list) { this.list = list; }

    public List<TagInfo> getTags() { return tags; }
    public void setTags(List<TagInfo> tags) { this.tags = tags; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", list=" + list +
                ", tags=" + tags +
                ", ownerId=" + ownerId +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}