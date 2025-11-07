package com.prm392.taskmanaapp.data;

public class Comment {
    private String commentId;
    private String taskId;
    private String userId;
    private String userName;
    private String content;
    private String createdAt;

    public Comment() {}

    public Comment(String commentId, String taskId, String userId, String userName, String content, String createdAt) {
        this.commentId = commentId;
        this.taskId = taskId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

