package com.prm392.taskmanaapp.ui.project;

import com.prm392.taskmanaapp.data.Task;
import com.prm392.taskmanaapp.data.Repository.TaskRepository;

import java.util.List;
import java.util.Map;

public class TaskPresenter implements TaskContract.Presenter {

    private TaskContract.View view;
    private final TaskRepository repository;

    public TaskPresenter(TaskContract.View view) {
        this.view = view;
        this.repository = new TaskRepository();
    }

    @Override
    public void loadTasks(String projectId) {
        if (view != null) {
            view.showLoading();
        }
        repository.loadTasks(projectId, new TaskRepository.OnTasksLoadedListener() {
            @Override
            public void onSuccess(List<Task> tasks) {
                if (view != null) {
                    view.hideLoading();
                    view.showTasks(tasks);
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
    public void createTask(String projectId, String title, String description, String priority, String status, String assignedToUserId) {
        if (view != null) {
            view.showLoading();
        }
        repository.createTask(projectId, title, description, priority, status, assignedToUserId, new TaskRepository.OnTaskCreatedListener() {
            @Override
            public void onSuccess(Task task) {
                if (view != null) {
                    view.hideLoading();
                    view.onTaskCreated(task);
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
    public void updateTask(Task task, String title, String description, String priority, String status, String assignedToUserId) {
        if (view != null) {
            view.showLoading();
        }
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(status);
        task.setAssignedTo(assignedToUserId);
        repository.updateTask(task, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.onTaskUpdated();
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
    public void deleteTask(Task task) {
        if (view != null) {
            view.showLoading();
        }
        repository.deleteTask(task.getTaskId(), new TaskRepository.OnTaskDeletedListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.onTaskDeleted();
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
    public void assignTaskToUser(String taskId, String userId) {
        if (view != null) {
            view.showLoading();
        }
        repository.assignTaskToUser(taskId, userId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.onTaskUpdated();
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
    public void loadUsersForAssignment(String projectId) {
        repository.loadUsersForAssignment(projectId, new TaskRepository.OnUsersLoadedListener() {
            @Override
            public void onSuccess(List<Map<String, String>> users) {
                if (view != null) {
                    view.showUsersForAssignment(users);
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

