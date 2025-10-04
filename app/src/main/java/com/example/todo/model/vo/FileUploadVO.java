// FileUploadVO.java
package com.example.todo.model.vo;

public class FileUploadVO {
    private int fileId;
    private String url;

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "FileUploadVO{" +
                "fileId=" + fileId +
                ", url='" + url + '\'' +
                '}';
    }
}