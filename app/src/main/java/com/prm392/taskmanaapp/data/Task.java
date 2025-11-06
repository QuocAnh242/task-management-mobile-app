package com.prm392.taskmanaapp.data;

public class Task {
    private String taskId;
    private String title;
    private String description;
    private String priority; // LOW, MEDIUM, HIGH
    private String status; // TODO, IN_PROGRESS, DONE
    private String projectId; // Firebase project ID
    private String assignedTo; // Firebase UID of assigned user
    private String assignedName;
    private String createdAt;
    private String endedAt;

    public Task() {}

    public Task(String taskId, String title, String description, String priority,
                String status, String projectId, String assignedTo, String assignedName,
                String createdAt, String endedAt) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.projectId = projectId;
        this.assignedTo = assignedTo;
        this.assignedName = assignedName;
        this.createdAt = createdAt;
        this.endedAt = endedAt;
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedName() { return assignedName; }
    public void setAssignedName(String assignedName) { this.assignedName = assignedName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEndedAt() { return endedAt; }
    public void setEndedAt(String endedAt) { this.endedAt = endedAt; }
}
