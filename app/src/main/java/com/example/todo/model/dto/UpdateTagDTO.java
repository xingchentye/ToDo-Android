package com.example.todo.model.dto;

public class UpdateTagDTO {
    private String name;

    public UpdateTagDTO() {}

    public UpdateTagDTO(String name) {
        this.name = name;
    }

    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "UpdateTagDTO{" +
                "name='" + name + '\'' +
                '}';
    }
}