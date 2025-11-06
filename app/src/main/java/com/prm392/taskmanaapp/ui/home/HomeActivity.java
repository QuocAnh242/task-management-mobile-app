package com.prm392.taskmanaapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Project;
import com.prm392.taskmanaapp.ui.project.ProjectActivity;
import com.prm392.taskmanaapp.ui.project.ProjectManagementActivity;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private RecyclerView rvTasks;
    private RecyclerView rvNotifications;
    private Button btnManageProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView tvWelcome = findViewById(R.id.tvWelcome);
            tvWelcome.setText("Welcome, " + user.getEmail());
        }

        // Initialize RecyclerViews
        rvProjects = findViewById(R.id.rvProjects);
        rvTasks = findViewById(R.id.rvTasks);
        rvNotifications = findViewById(R.id.rvNotifications);
        btnManageProjects = findViewById(R.id.btnManageProjects);

        // Setup Projects RecyclerView
        setupProjectsRecyclerView();

        // Setup Tasks RecyclerView
        setupTasksRecyclerView();

        // Setup Notifications RecyclerView
        setupNotificationsRecyclerView();

        // Setup Manage Projects button
        btnManageProjects.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProjectManagementActivity.class);
            startActivity(intent);
        });

        // Load data (this would typically come from a repository/database)
        loadData();
    }

    private void setupProjectsRecyclerView() {
        rvProjects.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // TODO: Create and set adapter for projects
    }

    private void setupTasksRecyclerView() {
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Create and set adapter for tasks
    }

    private void setupNotificationsRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Create and set adapter for notifications
    }

    private void loadData() {
        // TODO: Load projects, tasks, and notifications from database
        // For now, this is a placeholder
    }

    public void openProject(Project project) {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("project_id", project.getProjectId());
        intent.putExtra("project_title", project.getTitle());
        startActivity(intent);
    }
}