package com.prm392.taskmanaapp.data;

public class Project {
    private int projectId;
    private String title;
    private String description;
    private int leaderId; // FK to user_app
    private String createdAt;
    private String endedAt;
    public Project() {}

    public Project(int projectId, String title, String description, int leaderId,
                   String createdAt, String endedAt) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.leaderId = leaderId;
        this.createdAt = createdAt;
        this.endedAt = endedAt;
    }

    // Getters and Setters
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getLeaderId() { return leaderId; }
    public void setLeaderId(int leaderId) { this.leaderId = leaderId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEndedAt() { return endedAt; }
    public void setEndedAt(String endedAt) { this.endedAt = endedAt; }
}

