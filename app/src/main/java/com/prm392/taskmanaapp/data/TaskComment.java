package com.prm392.taskmanaapp.data;

public class TaskComment {
    private int id;
    private String comment;
    private String createdAt;
    private int userId;
    private int taskId;

    public TaskComment() {}

    public TaskComment(int id, String comment, String createdAt, int userId, int taskId) {
        this.id = id;
        this.comment = comment;
        this.createdAt = createdAt;
        this.userId = userId;
        this.taskId = taskId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
}
