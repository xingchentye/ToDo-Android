package com.example.todo.model.dto;

import java.util.List;

public class CreateTaskDTO {
    private String title;
    private String description;
    private String dueDate;
    private String priority; // LOW, MEDIUM, HIGH
    private Integer listId;
    private List<Integer> tagIds;

    public CreateTaskDTO() {}

    public CreateTaskDTO(String title, String description, String dueDate, String priority, Integer listId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.listId = listId;
    }

    // Getter和Setter方法
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getListId() { return listId; }
    public void setListId(Integer listId) { this.listId = listId; }

    public List<Integer> getTagIds() { return tagIds; }
    public void setTagIds(List<Integer> tagIds) { this.tagIds = tagIds; }

    @Override
    public String toString() {
        return "CreateTaskDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", priority='" + priority + '\'' +
                ", listId=" + listId +
                ", tagIds=" + tagIds +
                '}';
    }
}