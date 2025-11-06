package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.taskmanaapp.data.Project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectRepository {

    public interface OnProjectsLoadedListener {
        void onSuccess(List<Project> projects);
        void onError(String message);
    }

    public interface OnProjectCreatedListener {
        void onSuccess(Project project);
        void onError(String message);
    }

    public interface OnProjectUpdatedListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnProjectDeletedListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnUserInvitedListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnUsersLoadedListener {
        void onSuccess(List<Map<String, String>> users); // List of {id, name, email}
        void onError(String message);
    }

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public ProjectRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void createProject(String title, String description, OnProjectCreatedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        // Get user data to get name
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userDoc.getString("name");
                    if (userName == null) userName = currentUser.getEmail();

                    String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    Map<String, Object> projectData = new HashMap<>();
                    projectData.put("title", title);
                    projectData.put("description", description);
                    projectData.put("leaderId", currentUser.getUid());
                    projectData.put("leaderName", userName);
                    projectData.put("memberIds", new ArrayList<String>());
                    projectData.put("createdAt", createdAt);
                    projectData.put("endedAt", "");

                    db.collection("projects")
                            .add(projectData)
                            .addOnSuccessListener(documentReference -> {
                                Project project = new Project();
                                project.setProjectId(documentReference.getId());
                                project.setTitle(title);
                                project.setDescription(description);
                                project.setLeaderId(currentUser.getUid());
                                project.setLeaderName(userName);
                                project.setCreatedAt(createdAt);
                                project.setEndedAt("");
                                project.setMemberIds(new ArrayList<>());

                                listener.onSuccess(project);
                            })
                            .addOnFailureListener(e -> listener.onError("Failed to create project: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to get user data: " + e.getMessage()));
    }

    public void loadProjects(OnProjectsLoadedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        String userId = currentUser.getUid();
        List<Project> projects = new ArrayList<>();
        final int[] completedQueries = {0};
        final int totalQueries = 2;

        // Load projects where user is leader
        db.collection("projects")
                .whereEqualTo("leaderId", userId)
                .get()
                .addOnSuccessListener(leaderQuery -> {
                    for (QueryDocumentSnapshot doc : leaderQuery) {
                        Project project = documentToProject(doc);
                        projects.add(project);
                    }
                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        listener.onSuccess(projects);
                    }
                })
                .addOnFailureListener(e -> {
                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        listener.onSuccess(projects);
                    }
                });

        // Load projects where user is member
        db.collection("projects")
                .whereArrayContains("memberIds", userId)
                .get()
                .addOnSuccessListener(memberQuery -> {
                    for (QueryDocumentSnapshot doc : memberQuery) {
                        Project project = documentToProject(doc);
                        // Avoid duplicates
                        boolean exists = false;
                        for (Project p : projects) {
                            if (p.getProjectId().equals(project.getProjectId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            projects.add(project);
                        }
                    }
                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        listener.onSuccess(projects);
                    }
                })
                .addOnFailureListener(e -> {
                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        listener.onSuccess(projects);
                    }
                });
    }

    public void updateProject(Project project, OnProjectUpdatedListener listener) {
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", project.getTitle());
        projectData.put("description", project.getDescription());

        db.collection("projects").document(project.getProjectId())
                .update(projectData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to update project: " + e.getMessage()));
    }

    public void deleteProject(String projectId, OnProjectDeletedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        // Verify user is the leader
        db.collection("projects").document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onError("Project not found");
                        return;
                    }

                    String leaderId = documentSnapshot.getString("leaderId");
                    if (!currentUser.getUid().equals(leaderId)) {
                        listener.onError("Only project creator can delete the project");
                        return;
                    }

                    // Delete all tasks in this project first
                    db.collection("tasks")
                            .whereEqualTo("projectId", projectId)
                            .get()
                            .addOnSuccessListener(taskQuery -> {
                                for (QueryDocumentSnapshot taskDoc : taskQuery) {
                                    taskDoc.getReference().delete();
                                }

                                // Delete the project
                                db.collection("projects").document(projectId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                                        .addOnFailureListener(e -> listener.onError("Failed to delete project: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> {
                                // Even if tasks deletion fails, try to delete project
                                db.collection("projects").document(projectId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                                        .addOnFailureListener(err -> listener.onError("Failed to delete project: " + err.getMessage()));
                            });
                })
                .addOnFailureListener(e -> listener.onError("Failed to verify project: " + e.getMessage()));
    }

    public void inviteUserToProject(String projectId, String userEmail, OnUserInvitedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        // Find user by email
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError("User with email " + userEmail + " not found");
                        return;
                    }

                    DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                    String userId = userDoc.getId();

                    // Add user to project members
                    db.collection("projects").document(projectId)
                            .get()
                            .addOnSuccessListener(projectDoc -> {
                                if (!projectDoc.exists()) {
                                    listener.onError("Project not found");
                                    return;
                                }

                                List<String> memberIds = (List<String>) projectDoc.get("memberIds");
                                if (memberIds == null) {
                                    memberIds = new ArrayList<>();
                                }

                                if (memberIds.contains(userId)) {
                                    listener.onError("User is already a member of this project");
                                    return;
                                }

                                memberIds.add(userId);
                                db.collection("projects").document(projectId)
                                        .update("memberIds", memberIds)
                                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                                        .addOnFailureListener(e -> listener.onError("Failed to invite user: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> listener.onError("Failed to get project: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to find user: " + e.getMessage()));
    }

    public void loadUsersForInvite(String projectId, OnUsersLoadedListener listener) {
        // Load all users except current user and project members
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        db.collection("projects").document(projectId)
                .get()
                .addOnSuccessListener(projectDoc -> {
                    if (!projectDoc.exists()) {
                        listener.onError("Project not found");
                        return;
                    }

                    List<String> memberIds = (List<String>) projectDoc.get("memberIds");
                    if (memberIds == null) {
                        memberIds = new ArrayList<>();
                    }
                    memberIds.add(projectDoc.getString("leaderId")); // Also exclude leader

                    db.collection("users")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<Map<String, String>> users = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    String userId = doc.getId();
                                    if (!userId.equals(currentUser.getUid()) && !memberIds.contains(userId)) {
                                        Map<String, String> user = new HashMap<>();
                                        user.put("id", userId);
                                        user.put("name", doc.getString("name"));
                                        user.put("email", doc.getString("email"));
                                        users.add(user);
                                    }
                                }
                                listener.onSuccess(users);
                            })
                            .addOnFailureListener(e -> listener.onError("Failed to load users: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to get project: " + e.getMessage()));
    }

    private Project documentToProject(QueryDocumentSnapshot doc) {
        Project project = new Project();
        project.setProjectId(doc.getId());
        project.setTitle(doc.getString("title"));
        project.setDescription(doc.getString("description"));
        project.setLeaderId(doc.getString("leaderId"));
        project.setLeaderName(doc.getString("leaderName"));
        project.setCreatedAt(doc.getString("createdAt"));
        project.setEndedAt(doc.getString("endedAt"));
        
        List<String> memberIds = (List<String>) doc.get("memberIds");
        if (memberIds != null) {
            project.setMemberIds(memberIds);
        } else {
            project.setMemberIds(new ArrayList<>());
        }
        
        return project;
    }
}

