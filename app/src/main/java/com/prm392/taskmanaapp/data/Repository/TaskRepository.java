package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.taskmanaapp.data.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskRepository {

    public interface OnTasksLoadedListener {
        void onSuccess(List<Task> tasks);
        void onError(String message);
    }

    public interface OnTaskCreatedListener {
        void onSuccess(Task task);
        void onError(String message);
    }

    public interface OnTaskUpdatedListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnTaskDeletedListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnUsersLoadedListener {
        void onSuccess(List<Map<String, String>> users); // List of {id, name, email}
        void onError(String message);
    }

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public TaskRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void createTask(String projectId, String title, String description, String priority, String status, String assignedToUserId, OnTaskCreatedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", title);
        taskData.put("description", description);
        taskData.put("priority", priority);
        taskData.put("status", status);
        taskData.put("projectId", projectId);
        taskData.put("assignedTo", assignedToUserId != null && !assignedToUserId.isEmpty() ? assignedToUserId : "");
        taskData.put("assignedName", "");
        taskData.put("createdAt", createdAt);
        taskData.put("endedAt", "");

        // If assigned to user, get user name
        if (assignedToUserId != null && !assignedToUserId.isEmpty()) {
            db.collection("users").document(assignedToUserId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String userName = userDoc.getString("name");
                        if (userName == null) userName = userDoc.getString("email");
                        taskData.put("assignedName", userName != null ? userName : "");

                        db.collection("tasks")
                                .add(taskData)
                                .addOnSuccessListener(documentReference -> {
                                    Task task = new Task();
                                    task.setTaskId(documentReference.getId());
                                    task.setTitle(title);
                                    task.setDescription(description);
                                    task.setPriority(priority);
                                    task.setStatus(status);
                                    task.setProjectId(projectId);
                                    task.setAssignedTo(assignedToUserId);
                                    task.setAssignedName(userName != null ? userName : "");
                                    task.setCreatedAt(createdAt);
                                    task.setEndedAt("");

                                    listener.onSuccess(task);
                                })
                                .addOnFailureListener(e -> listener.onError("Failed to create task: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> {
                        // Create task without user name if user not found
                        db.collection("tasks")
                                .add(taskData)
                                .addOnSuccessListener(documentReference -> {
                                    Task task = new Task();
                                    task.setTaskId(documentReference.getId());
                                    task.setTitle(title);
                                    task.setDescription(description);
                                    task.setPriority(priority);
                                    task.setStatus(status);
                                    task.setProjectId(projectId);
                                    task.setAssignedTo(assignedToUserId);
                                    task.setAssignedName("");
                                    task.setCreatedAt(createdAt);
                                    task.setEndedAt("");

                                    listener.onSuccess(task);
                                })
                                .addOnFailureListener(err -> listener.onError("Failed to create task: " + err.getMessage()));
                    });
        } else {
            // Create task without assignment
            db.collection("tasks")
                    .add(taskData)
                    .addOnSuccessListener(documentReference -> {
                        Task task = new Task();
                        task.setTaskId(documentReference.getId());
                        task.setTitle(title);
                        task.setDescription(description);
                        task.setPriority(priority);
                        task.setStatus(status);
                        task.setProjectId(projectId);
                        task.setAssignedTo("");
                        task.setAssignedName("");
                        task.setCreatedAt(createdAt);
                        task.setEndedAt("");

                        listener.onSuccess(task);
                    })
                    .addOnFailureListener(e -> listener.onError("Failed to create task: " + e.getMessage()));
        }
    }

    public void loadTasks(String projectId, OnTasksLoadedListener listener) {
        db.collection("tasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = documentToTask(doc);
                        tasks.add(task);
                    }
                    listener.onSuccess(tasks);
                })
                .addOnFailureListener(e -> listener.onError("Failed to load tasks: " + e.getMessage()));
    }

    public void updateTask(Task task, OnTaskUpdatedListener listener) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", task.getTitle());
        taskData.put("description", task.getDescription());
        taskData.put("priority", task.getPriority());
        taskData.put("status", task.getStatus());
        taskData.put("assignedTo", task.getAssignedTo() != null ? task.getAssignedTo() : "");
        taskData.put("assignedName", task.getAssignedName() != null ? task.getAssignedName() : "");

        db.collection("tasks").document(task.getTaskId())
                .update(taskData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to update task: " + e.getMessage()));
    }

    public void deleteTask(String taskId, OnTaskDeletedListener listener) {
        db.collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to delete task: " + e.getMessage()));
    }

    public void assignTaskToUser(String taskId, String userId, OnTaskUpdatedListener listener) {
        // Get user name
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userDoc.getString("name");
                    if (userName == null) userName = userDoc.getString("email");

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("assignedTo", userId);
                    updates.put("assignedName", userName != null ? userName : "");

                    db.collection("tasks").document(taskId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onError("Failed to assign task: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Failed to get user data: " + e.getMessage()));
    }

    public void loadUsersForAssignment(String projectId, OnUsersLoadedListener listener) {
        // Load project members and leader
        db.collection("projects").document(projectId)
                .get()
                .addOnSuccessListener(projectDoc -> {
                    if (!projectDoc.exists()) {
                        listener.onError("Project not found");
                        return;
                    }

                    List<String> userIds = new ArrayList<>();
                    String leaderId = projectDoc.getString("leaderId");
                    if (leaderId != null) {
                        userIds.add(leaderId);
                    }

                    List<String> memberIds = (List<String>) projectDoc.get("memberIds");
                    if (memberIds != null) {
                        userIds.addAll(memberIds);
                    }

                    if (userIds.isEmpty()) {
                        listener.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Load user details
                    List<Map<String, String>> users = new ArrayList<>();
                    final int[] loaded = {0};
                    final int total = userIds.size();

                    for (String userId : userIds) {
                        db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    Map<String, String> user = new HashMap<>();
                                    user.put("id", userId);
                                    user.put("name", userDoc.getString("name"));
                                    user.put("email", userDoc.getString("email"));
                                    users.add(user);

                                    loaded[0]++;
                                    if (loaded[0] == total) {
                                        listener.onSuccess(users);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    loaded[0]++;
                                    if (loaded[0] == total) {
                                        listener.onSuccess(users);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> listener.onError("Failed to get project: " + e.getMessage()));
    }

    private Task documentToTask(QueryDocumentSnapshot doc) {
        Task task = new Task();
        task.setTaskId(doc.getId());
        task.setTitle(doc.getString("title"));
        task.setDescription(doc.getString("description"));
        task.setPriority(doc.getString("priority"));
        task.setStatus(doc.getString("status"));
        task.setProjectId(doc.getString("projectId"));
        task.setAssignedTo(doc.getString("assignedTo"));
        task.setAssignedName(doc.getString("assignedName"));
        task.setCreatedAt(doc.getString("createdAt"));
        task.setEndedAt(doc.getString("endedAt"));
        return task;
    }
}

