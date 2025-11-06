package com.prm392.taskmanaapp.ui.project;

import com.prm392.taskmanaapp.data.Project;
import com.prm392.taskmanaapp.data.Repository.ProjectRepository;

import java.util.List;
import java.util.Map;

public class ProjectPresenter implements ProjectContract.Presenter {

    private ProjectContract.View view;
    private final ProjectRepository repository;

    public ProjectPresenter(ProjectContract.View view) {
        this.view = view;
        this.repository = new ProjectRepository();
    }

    @Override
    public void loadProjects() {
        if (view != null) {
            view.showLoading();
        }
        repository.loadProjects(new ProjectRepository.OnProjectsLoadedListener() {
            @Override
            public void onSuccess(List<Project> projects) {
                if (view != null) {
                    view.hideLoading();
                    view.showProjects(projects);
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void createProject(String title, String description) {
        if (view != null) {
            view.showLoading();
        }
        repository.createProject(title, description, new ProjectRepository.OnProjectCreatedListener() {
            @Override
            public void onSuccess(Project project) {
                if (view != null) {
                    view.hideLoading();
                    view.onProjectCreated(project);
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void updateProject(Project project, String title, String description) {
        if (view != null) {
            view.showLoading();
        }
        project.setTitle(title);
        project.setDescription(description);
        repository.updateProject(project, new ProjectRepository.OnProjectUpdatedListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.onProjectUpdated();
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void deleteProject(Project project) {
        if (view != null) {
            view.showLoading();
        }
        repository.deleteProject(project.getProjectId(), new ProjectRepository.OnProjectDeletedListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.onProjectDeleted();
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void inviteUserToProject(String projectId, String userEmail) {
        if (view != null) {
            view.showLoading();
        }
        repository.inviteUserToProject(projectId, userEmail, new ProjectRepository.OnUserInvitedListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.onUserInvited();
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void loadUsersForInvite(String projectId) {
        repository.loadUsersForInvite(projectId, new ProjectRepository.OnUsersLoadedListener() {
            @Override
            public void onSuccess(List<Map<String, String>> users) {
                if (view != null) {
                    view.showUsersForInvite(users);
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}

