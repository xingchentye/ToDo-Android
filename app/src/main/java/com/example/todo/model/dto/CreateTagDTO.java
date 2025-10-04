package com.example.todo.model.dto;

public class CreateTagDTO {
    private String name;

    public CreateTagDTO() {}

    public CreateTagDTO(String name) {
        this.name = name;
    }

    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "CreateTagDTO{" +
                "name='" + name + '\'' +
                '}';
    }
}