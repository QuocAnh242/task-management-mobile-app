package com.prm392.taskmanaapp.data;

public class Notification {
    private String notificationId; // Firebase document ID
    private String userId; // Firebase UID of user
    private String projectId; // Firebase project ID (if related to project)
    private String title;
    private String content;
    private String status; // READ, UNREAD
    private String type; // PROJECT_INVITE, TASK_ASSIGNED, etc.
    private String createdAt;

    public Notification() {}

    public Notification(String notificationId, String userId, String projectId, String title, 
                        String content, String status, String type, String createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.projectId = projectId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.type = type;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
