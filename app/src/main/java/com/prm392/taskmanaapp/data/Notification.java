package com.prm392.taskmanaapp.data;

public class Notification {
    private int notificationId;
    private String title;
    private String content;
    private String status; // READ, UNREAD
    private String createdAt;
    private String endedAt;

    public Notification() {}

    public Notification(int notificationId, String title, String content,
                        String status, String createdAt, String endedAt) {
        this.notificationId = notificationId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.endedAt = endedAt;
    }

    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEndedAt() { return endedAt; }
    public void setEndedAt(String endedAt) { this.endedAt = endedAt; }
}
