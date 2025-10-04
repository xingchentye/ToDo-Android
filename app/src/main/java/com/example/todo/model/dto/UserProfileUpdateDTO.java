package com.example.todo.model.dto;

public class UserProfileUpdateDTO {
    private String username;
    private String nickName;
    private Integer avatarFileId;
    private String email;

    public UserProfileUpdateDTO() {}

    public UserProfileUpdateDTO(String username, String nickName, String email) {
        this.username = username;
        this.nickName = nickName;
        this.email = email;
    }

    // Getter和Setter方法
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public Integer getAvatarFileId() { return avatarFileId; }
    public void setAvatarFileId(Integer avatarFileId) { this.avatarFileId = avatarFileId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "UserProfileUpdateDTO{" +
                "username='" + username + '\'' +
                ", nickName='" + nickName + '\'' +
                ", avatarFileId=" + avatarFileId +
                ", email='" + email + '\'' +
                '}';
    }
}