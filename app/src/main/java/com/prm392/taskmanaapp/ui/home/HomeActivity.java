package com.prm392.taskmanaapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Notification;
import com.prm392.taskmanaapp.data.Project;
import com.prm392.taskmanaapp.data.Repository.NotificationRepository;
import com.prm392.taskmanaapp.data.Repository.ProjectRepository;
import com.prm392.taskmanaapp.data.Repository.TaskRepository;
import com.prm392.taskmanaapp.data.Task;
import com.prm392.taskmanaapp.ui.login.LoginActivity;
import com.prm392.taskmanaapp.ui.project.ProjectActivity;
import com.prm392.taskmanaapp.ui.project.ProjectAdapter;
import com.prm392.taskmanaapp.ui.project.ProjectManagementActivity;
import com.prm392.taskmanaapp.ui.project.TaskAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private RecyclerView rvTasks;
    private RecyclerView rvNotifications;
    private TextView tvWelcome;
    private TextView tvNoProjects;
    private TextView tvNoTasks;
    private TextView tvNoNotifications;
    private TextView tvNotificationCount;
    private TextView tvProjectCount;
    private TextView tvTaskCount;
    private Button fabManageProjects;
    private Button btnLogout;
    private ProgressBar progressBar;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;

    private ProjectAdapter projectAdapter;
    private TaskAdapter taskAdapter;
    private NotificationAdapter notificationAdapter;

    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;
    private NotificationRepository notificationRepository;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Repositories
        projectRepository = new ProjectRepository();
        taskRepository = new TaskRepository();
        notificationRepository = new NotificationRepository();

        // Initialize Views
        initializeViews();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load user name
        loadUserName();

        // Load data
        loadProjects();
        loadTasks();
        loadNotifications();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        rvProjects = findViewById(R.id.rvProjects);
        rvTasks = findViewById(R.id.rvTasks);
        rvNotifications = findViewById(R.id.rvNotifications);
        tvNoProjects = findViewById(R.id.tvNoProjects);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        tvNoNotifications = findViewById(R.id.tvNoNotifications);
        tvNotificationCount = findViewById(R.id.tvNotificationCount);
        tvProjectCount = findViewById(R.id.tvProjectCount);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        fabManageProjects = findViewById(R.id.fabManageProjects);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Disable bottom navigation (UI only)
        if (bottomNavigation != null) {
            bottomNavigation.setEnabled(false);
            bottomNavigation.setClickable(false);
            bottomNavigation.setFocusable(false);
        }
    }

    private void setupRecyclerViews() {
        // Projects RecyclerView
        projectAdapter = new ProjectAdapter(new ArrayList<>(), new ProjectAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                openProject(project);
            }

            @Override
            public void onProjectLongClick(Project project) {
                // Handle long click if needed
            }

            @Override
            public void onInviteUserClick(Project project) {
                // Handle invite user
            }
        });
        rvProjects.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvProjects.setAdapter(projectAdapter);

        // Tasks RecyclerView
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                // Handle task click
            }

            @Override
            public void onTaskLongClick(Task task) {
                // Handle task long click
            }
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);

        // Notifications RecyclerView
        notificationAdapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            // Handle notification click
            if (notification.getStatus().equals("UNREAD")) {
                notificationRepository.markAsRead(notification.getNotificationId());
                notification.setStatus("READ");
                notificationAdapter.notifyDataSetChanged();
            }
        });
        
        // Set action listener for accept/decline buttons
        notificationAdapter.setActionListener(new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onAcceptInvite(Notification notification) {
                handleAcceptInvite(notification);
            }

            @Override
            public void onDeclineInvite(Notification notification) {
                handleDeclineInvite(notification);
            }
        });
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Load user name from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvWelcome.setText("Xin chào, " + name + "!");
                            } else {
                                tvWelcome.setText("Xin chào, " + user.getEmail() + "!");
                            }
                        } else {
                            tvWelcome.setText("Xin chào, " + user.getEmail() + "!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvWelcome.setText("Xin chào, " + user.getEmail() + "!");
                    });
        }
    }

    private void loadProjects() {
        showProgress(true);
        projectRepository.loadProjects(new ProjectRepository.OnProjectsLoadedListener() {
            @Override
            public void onSuccess(List<Project> projects) {
                showProgress(false);
                if (projects.isEmpty()) {
                    tvNoProjects.setVisibility(View.VISIBLE);
                    rvProjects.setVisibility(View.GONE);
                } else {
                    tvNoProjects.setVisibility(View.GONE);
                    rvProjects.setVisibility(View.VISIBLE);
                    projectAdapter.updateProjects(projects);
                }
                // Update stats
                if (tvProjectCount != null) {
                    tvProjectCount.setText(String.valueOf(projects.size()));
                }
            }

            @Override
            public void onError(String message) {
                showProgress(false);
                tvNoProjects.setVisibility(View.VISIBLE);
                rvProjects.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Lỗi tải dự án: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasks() {
        // Load all tasks for current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Load all projects first, then load tasks from those projects
        projectRepository.loadProjects(new ProjectRepository.OnProjectsLoadedListener() {
            @Override
            public void onSuccess(List<Project> projects) {
                List<Task> allTasks = new ArrayList<>();
                if (projects.isEmpty()) {
                    updateTasksUI(allTasks);
                    return;
                }

                final int[] completed = {0};
                final int total = projects.size();

                for (Project project : projects) {
                    taskRepository.loadTasks(project.getProjectId(), new TaskRepository.OnTasksLoadedListener() {
                        @Override
                        public void onSuccess(List<Task> tasks) {
                            // Filter tasks assigned to current user
                            for (Task task : tasks) {
                                if (task.getAssignedTo() != null && task.getAssignedTo().equals(user.getUid())) {
                                    allTasks.add(task);
                                }
                            }
                            completed[0]++;
                            if (completed[0] == total) {
                                updateTasksUI(allTasks);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            completed[0]++;
                            if (completed[0] == total) {
                                updateTasksUI(allTasks);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                updateTasksUI(new ArrayList<>());
            }
        });
    }

    private void updateTasksUI(List<Task> tasks) {
        if (tasks.isEmpty()) {
            tvNoTasks.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            tvNoTasks.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
            taskAdapter.updateTasks(tasks);
        }
        // Update stats
        if (tvTaskCount != null) {
            tvTaskCount.setText(String.valueOf(tasks.size()));
        }
    }

    private void loadNotifications() {
        notificationRepository.loadNotifications(new NotificationRepository.OnNotificationsLoadedListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (notifications.isEmpty()) {
                    tvNoNotifications.setVisibility(View.VISIBLE);
                    rvNotifications.setVisibility(View.GONE);
                    tvNotificationCount.setVisibility(View.GONE);
                } else {
                    tvNoNotifications.setVisibility(View.GONE);
                    rvNotifications.setVisibility(View.VISIBLE);
                    notificationAdapter.updateNotifications(notifications);

                    // Update stats
                    if (tvNotificationCount != null) {
                        tvNotificationCount.setText(String.valueOf(notifications.size()));
                    }
                }
            }

            @Override
            public void onError(String message) {
                tvNoNotifications.setVisibility(View.VISIBLE);
                rvNotifications.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        // Logout button
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Manage Projects FAB
        fabManageProjects.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProjectManagementActivity.class);
            startActivity(intent);
        });
    }

    private void openProject(Project project) {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("project_id", project.getProjectId());
        intent.putExtra("project_title", project.getTitle());
        startActivity(intent);
    }

    private void handleAcceptInvite(Notification notification) {
        if (notification.getProjectId() == null || notification.getProjectId().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin dự án", Toast.LENGTH_SHORT).show();
            return;
        }

        projectRepository.acceptInvite(notification.getProjectId(), new ProjectRepository.OnInviteResponseListener() {
            @Override
            public void onSuccess() {
                // Mark notification as read
                notificationRepository.markAsRead(notification.getNotificationId());
                notification.setStatus("READ");
                
                // Reload notifications and projects
                loadNotifications();
                loadProjects();
                
                Toast.makeText(HomeActivity.this, "Đã chấp nhận lời mời tham gia dự án", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeclineInvite(Notification notification) {
        // Just mark notification as read
        notificationRepository.markAsRead(notification.getNotificationId());
        notification.setStatus("READ");
        
        // Reload notifications
        loadNotifications();
        
        Toast.makeText(this, "Đã từ chối lời mời", Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to home
        loadProjects();
        loadTasks();
        loadNotifications();
    }
}
