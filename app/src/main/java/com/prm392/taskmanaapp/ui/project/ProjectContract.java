package com.prm392.taskmanaapp.ui.project;

import com.prm392.taskmanaapp.data.Project;

import java.util.List;
import java.util.Map;

public interface ProjectContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showProjects(List<Project> projects);
        void showError(String message);
        void onProjectCreated(Project project);
        void onProjectUpdated();
        void onProjectDeleted();
        void onUserInvited();
        void showUsersForInvite(List<Map<String, String>> users);
    }

    interface Presenter {
        void loadProjects();
        void createProject(String title, String description);
        void updateProject(Project project, String title, String description);
        void deleteProject(Project project);
        void inviteUserToProject(String projectId, String userEmail);
        void loadUsersForInvite(String projectId);
        void onDestroy();
    }
}

