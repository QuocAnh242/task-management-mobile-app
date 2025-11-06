package com.prm392.taskmanaapp.data;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String projectId;
    private String title;
    private String description;
    private String leaderId; // Firebase UID of project creator
    private String leaderName;
    private List<String> memberIds; // List of Firebase UIDs of project members
    private String createdAt;
    private String endedAt;
    
    public Project() {
        this.memberIds = new ArrayList<>();
    }

    public Project(String projectId, String title, String description, String leaderId,
                   String leaderName, String createdAt, String endedAt) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.createdAt = createdAt;
        this.endedAt = endedAt;
        this.memberIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }

    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEndedAt() { return endedAt; }
    public void setEndedAt(String endedAt) { this.endedAt = endedAt; }
}

