package com.prm392.taskmanaapp.ui.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectManagementActivity extends AppCompatActivity implements ProjectContract.View {

    private RecyclerView rvProjects;
    private Button btnCreateProject;
    private ProgressBar progressBar;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private ProjectContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_management);

        // Initialize views
        rvProjects = findViewById(R.id.rvProjects);
        btnCreateProject = findViewById(R.id.btnCreateProject);
        progressBar = findViewById(R.id.progressBar);

        // Initialize project list
        projectList = new ArrayList<>();

        // Create presenter
        presenter = new ProjectPresenter(this);

        // Setup RecyclerView
        setupProjectsRecyclerView();

        // Create Project button click listener
        btnCreateProject.setOnClickListener(v -> showCreateProjectDialog());

        // Load projects
        presenter.loadProjects();
    }

    private void setupProjectsRecyclerView() {
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new ProjectAdapter(projectList, new ProjectAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                openProject(project);
            }

            @Override
            public void onProjectLongClick(Project project) {
                showManageProjectDialog(project);
            }

            @Override
            public void onInviteUserClick(Project project) {
                showInviteUserDialog(project.getProjectId());
            }
        });
        rvProjects.setAdapter(projectAdapter);
    }

    private void showCreateProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        builder.setView(dialogView);

        EditText etProjectTitle = dialogView.findViewById(R.id.etProjectTitle);
        EditText etProjectDescription = dialogView.findViewById(R.id.etProjectDescription);

        builder.setTitle("Create New Project")
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = etProjectTitle.getText().toString().trim();
                    String description = etProjectDescription.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Project title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    presenter.createProject(title, description);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showManageProjectDialog(Project project) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        builder.setView(dialogView);

        EditText etProjectTitle = dialogView.findViewById(R.id.etProjectTitle);
        EditText etProjectDescription = dialogView.findViewById(R.id.etProjectDescription);

        // Populate fields with current project data
        etProjectTitle.setText(project.getTitle());
        etProjectDescription.setText(project.getDescription());

        builder.setTitle("Manage Project")
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = etProjectTitle.getText().toString().trim();
                    String description = etProjectDescription.getText().toString().trim();

                    presenter.updateProject(project, title, description);
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    deleteProject(project);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete this project? All tasks in this project will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    presenter.deleteProject(project);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void openProject(Project project) {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("project_id", project.getProjectId());
        intent.putExtra("project_title", project.getTitle());
        startActivity(intent);
    }

    public void showInviteUserDialog(String projectId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_user, null);
        builder.setView(dialogView);

        EditText etUserEmail = dialogView.findViewById(R.id.etUserEmail);

        builder.setTitle("Invite User to Project")
                .setPositiveButton("Invite", (dialog, which) -> {
                    String email = etUserEmail.getText().toString().trim();
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    presenter.inviteUserToProject(projectId, email);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnCreateProject.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnCreateProject.setEnabled(true);
    }

    @Override
    public void showProjects(List<Project> projects) {
        projectList.clear();
        projectList.addAll(projects);
        projectAdapter.updateProjects(projectList);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProjectCreated(Project project) {
        Toast.makeText(this, "Project created successfully", Toast.LENGTH_SHORT).show();
        presenter.loadProjects(); // Reload projects
    }

    @Override
    public void onProjectUpdated() {
        Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
        presenter.loadProjects(); // Reload projects
    }

    @Override
    public void onProjectDeleted() {
        Toast.makeText(this, "Project deleted successfully", Toast.LENGTH_SHORT).show();
        presenter.loadProjects(); // Reload projects
    }

    @Override
    public void onUserInvited() {
        Toast.makeText(this, "User invited successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUsersForInvite(List<Map<String, String>> users) {
        // This can be used to show a list of users to invite
        // For now, we use email input dialog
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}
