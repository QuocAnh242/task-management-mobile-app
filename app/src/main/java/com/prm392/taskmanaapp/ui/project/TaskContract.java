package com.prm392.taskmanaapp.ui.project;

import com.prm392.taskmanaapp.data.Task;

import java.util.List;
import java.util.Map;

public interface TaskContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showTasks(List<Task> tasks);
        void showError(String message);
        void onTaskCreated(Task task);
        void onTaskUpdated();
        void onTaskDeleted();
        void showUsersForAssignment(List<Map<String, String>> users);
    }

    interface Presenter {
        void loadTasks(String projectId);
        void createTask(String projectId, String title, String description, String priority, String status, String assignedToUserId);
        void updateTask(Task task, String title, String description, String priority, String status, String assignedToUserId);
        void deleteTask(Task task);
        void assignTaskToUser(String taskId, String userId);
        void loadUsersForAssignment(String projectId);
        void onDestroy();
    }
}

