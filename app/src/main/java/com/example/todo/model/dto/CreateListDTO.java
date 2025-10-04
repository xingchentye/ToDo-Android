package com.example.todo.model.dto;

public class CreateListDTO {
    private String name;
    private String description;

    public CreateListDTO() {}

    public CreateListDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "CreateListDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}