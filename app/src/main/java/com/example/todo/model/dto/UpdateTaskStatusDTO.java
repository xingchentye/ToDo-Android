package com.example.todo.model.dto;

public class UpdateTaskStatusDTO {
    private String status; // TODO, DONE

    public UpdateTaskStatusDTO() {}

    public UpdateTaskStatusDTO(String status) {
        this.status = status;
    }

    // Getter和Setter方法
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "UpdateTaskStatusDTO{" +
                "status='" + status + '\'' +
                '}';
    }
}