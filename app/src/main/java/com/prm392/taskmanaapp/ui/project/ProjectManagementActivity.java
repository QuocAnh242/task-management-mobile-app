package com.prm392.taskmanaapp.ui.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectManagementActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private Button btnCreateProject;
    private List<Project> projectList;
    // TODO: Create ProjectAdapter
    // private ProjectAdapter projectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_management);

        // Initialize views
        rvProjects = findViewById(R.id.rvProjects);
        btnCreateProject = findViewById(R.id.btnCreateProject);

        // Initialize project list
        projectList = new ArrayList<>();

        // Setup RecyclerView
        setupProjectsRecyclerView();

        // Create Project button click listener
        btnCreateProject.setOnClickListener(v -> showCreateProjectDialog());

        // Load projects
        loadProjects();
    }

    private void setupProjectsRecyclerView() {
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Create and set ProjectAdapter
        // projectAdapter = new ProjectAdapter(projectList, this);
        // rvProjects.setAdapter(projectAdapter);
    }

    private void loadProjects() {
        // TODO: Load projects from database
        // This is a placeholder
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

                    createProject(title, description);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createProject(String title, String description) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Create project in database
        // For now, create a local project object
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        // TODO: Set leaderId from current user
        // project.setLeaderId(currentUser.getUid());

        // Add to list and notify adapter
        projectList.add(project);
        // projectAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Project created successfully", Toast.LENGTH_SHORT).show();
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

                    updateProject(project, title, description);
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    deleteProject(project);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProject(Project project, String title, String description) {
        // Update project properties
        project.setTitle(title);
        project.setDescription(description);

        // TODO: Update project in database

        // Notify adapter
        // projectAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void deleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete this project? All tasks in this project will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Delete project from database
                    projectList.remove(project);
                    // projectAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Project deleted successfully", Toast.LENGTH_SHORT).show();
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
}

