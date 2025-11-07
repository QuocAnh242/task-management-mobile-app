package com.prm392.taskmanaapp.ui.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectActivity extends AppCompatActivity implements TaskContract.View {

    private TextView tvProjectTitle;
    private RecyclerView rvTodoTasks;
    private RecyclerView rvInProgressTasks;
    private RecyclerView rvDoneTasks;
    private TextView tvTodoCount;
    private TextView tvInProgressCount;
    private TextView tvDoneCount;
    private TextView tvNoTasks;
    private View kanbanScrollView;
    private Button btnCreateTask;
    private ProgressBar progressBar;
    private TaskAdapter todoAdapter;
    private TaskAdapter inProgressAdapter;
    private TaskAdapter doneAdapter;
    private List<Task> taskList;
    private List<Map<String, String>> usersForAssignment;
    private String projectId;
    private String projectTitle;
    private TaskContract.Presenter presenter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // Get project info from intent
        projectId = getIntent().getStringExtra("project_id");
        if (projectId == null) {
            // Try to get as int (for backward compatibility)
            int projectIdInt = getIntent().getIntExtra("project_id", -1);
            if (projectIdInt != -1) {
                projectId = String.valueOf(projectIdInt);
            }
        }
        projectTitle = getIntent().getStringExtra("project_title");

        // Initialize views
        tvProjectTitle = findViewById(R.id.tvProjectTitle);
        TextView tvProjectDescription = findViewById(R.id.tvProjectDescription);
        ImageButton btnBack = findViewById(R.id.btnBack);
        rvTodoTasks = findViewById(R.id.rvTodoTasks);
        rvInProgressTasks = findViewById(R.id.rvInProgressTasks);
        rvDoneTasks = findViewById(R.id.rvDoneTasks);
        tvTodoCount = findViewById(R.id.tvTodoCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvDoneCount = findViewById(R.id.tvDoneCount);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        kanbanScrollView = findViewById(R.id.kanbanScrollView);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        progressBar = findViewById(R.id.progressBar);

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Initialize Firebase FIRST
        db = FirebaseFirestore.getInstance();

        // Set project title
        if (projectTitle != null) {
            tvProjectTitle.setText(projectTitle);
        }

        // Load project details from Firestore
        if (projectId != null) {
            loadProjectDetails();
        }

        // Initialize task list
        taskList = new ArrayList<>();
        usersForAssignment = new ArrayList<>();

        // Create presenter
        presenter = new TaskPresenter(this);

        // Setup RecyclerView
        setupTasksRecyclerView();

        // Create Task button click listener
        btnCreateTask.setOnClickListener(v -> {
            // Load users for assignment first, then show dialog
            if (usersForAssignment.isEmpty()) {
                presenter.loadUsersForAssignment(projectId);
            } else {
                showCreateTaskDialog();
            }
        });

        // Load tasks
        presenter.loadTasks(projectId);
    }

    private void setupTasksRecyclerView() {
        // Setup TODO column
        rvTodoTasks.setLayoutManager(new LinearLayoutManager(this));
        todoAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showManageTaskDialog(task);
            }

            @Override
            public void onTaskLongClick(Task task) {
                showAssignTaskDialog(task);
            }
        });
        rvTodoTasks.setAdapter(todoAdapter);

        // Setup IN PROGRESS column
        rvInProgressTasks.setLayoutManager(new LinearLayoutManager(this));
        inProgressAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showManageTaskDialog(task);
            }

            @Override
            public void onTaskLongClick(Task task) {
                showAssignTaskDialog(task);
            }
        });
        rvInProgressTasks.setAdapter(inProgressAdapter);

        // Setup DONE column
        rvDoneTasks.setLayoutManager(new LinearLayoutManager(this));
        doneAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showManageTaskDialog(task);
            }

            @Override
            public void onTaskLongClick(Task task) {
                showAssignTaskDialog(task);
            }
        });
        rvDoneTasks.setAdapter(doneAdapter);
    }

    private void showCreateTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_task, null);
        builder.setView(dialogView);

        EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
        EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        Spinner spinnerAssignTo = dialogView.findViewById(R.id.spinnerAssignTo);

        // Populate spinners
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this, R.array.task_priorities, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.task_statuses, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Populate assignTo spinner with users
        List<String> userNames = new ArrayList<>();
        userNames.add("Unassigned");
        Map<String, String> userIdMap = new HashMap<>();
        userIdMap.put("Unassigned", "");

        for (Map<String, String> user : usersForAssignment) {
            String userName = user.get("name");
            if (userName == null) userName = user.get("email");
            userNames.add(userName);
            userIdMap.put(userName, user.get("id"));
        }

        ArrayAdapter<String> assignAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
        assignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignTo.setAdapter(assignAdapter);

        builder.setTitle("Create New Task")
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = etTaskTitle.getText().toString().trim();
                    String description = etTaskDescription.getText().toString().trim();
                    String priority = spinnerPriority.getSelectedItem().toString();
                    String status = spinnerStatus.getSelectedItem().toString();
                    String assignedToName = spinnerAssignTo.getSelectedItem().toString();
                    String assignedToUserId = userIdMap.get(assignedToName);

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    presenter.createTask(projectId, title, description, priority, status, assignedToUserId);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showManageTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_task, null);
        builder.setView(dialogView);

        EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
        EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        Spinner spinnerAssignTo = dialogView.findViewById(R.id.spinnerAssignTo);

        // Populate spinners
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this, R.array.task_priorities, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.task_statuses, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Populate assignTo spinner with users
        List<String> userNames = new ArrayList<>();
        userNames.add("Unassigned");
        Map<String, String> userIdMap = new HashMap<>();
        userIdMap.put("Unassigned", "");

        for (Map<String, String> user : usersForAssignment) {
            String userName = user.get("name");
            if (userName == null) userName = user.get("email");
            userNames.add(userName);
            userIdMap.put(userName, user.get("id"));
        }

        ArrayAdapter<String> assignAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
        assignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignTo.setAdapter(assignAdapter);

        // Populate fields with current task data
        etTaskTitle.setText(task.getTitle());
        etTaskDescription.setText(task.getDescription());

        // Set spinner selections based on current values
        int priorityPosition = priorityAdapter.getPosition(task.getPriority());
        if (priorityPosition >= 0) spinnerPriority.setSelection(priorityPosition);

        int statusPosition = statusAdapter.getPosition(task.getStatus());
        if (statusPosition >= 0) spinnerStatus.setSelection(statusPosition);

        String assignedName = task.getAssignedName() != null && !task.getAssignedName().isEmpty() ? task.getAssignedName() : "Unassigned";
        int assignPosition = assignAdapter.getPosition(assignedName);
        if (assignPosition >= 0) spinnerAssignTo.setSelection(assignPosition);

        builder.setTitle("Manage Task")
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = etTaskTitle.getText().toString().trim();
                    String description = etTaskDescription.getText().toString().trim();
                    String priority = spinnerPriority.getSelectedItem().toString();
                    String status = spinnerStatus.getSelectedItem().toString();
                    String assignedToName = spinnerAssignTo.getSelectedItem().toString();
                    String assignedToUserId = userIdMap.get(assignedToName);

                    presenter.updateTask(task, title, description, priority, status, assignedToUserId);
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    deleteTask(task);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteTask(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    presenter.deleteTask(task);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void showAssignTaskDialog(Task task) {
        if (usersForAssignment.isEmpty()) {
            Toast.makeText(this, "Loading users...", Toast.LENGTH_SHORT).show();
            presenter.loadUsersForAssignment(projectId);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign Task to User");

        List<String> userNames = new ArrayList<>();
        userNames.add("Unassigned");
        Map<String, String> userIdMap = new HashMap<>();
        userIdMap.put("Unassigned", "");

        for (Map<String, String> user : usersForAssignment) {
            String userName = user.get("name");
            if (userName == null) userName = user.get("email");
            userNames.add(userName);
            userIdMap.put(userName, user.get("id"));
        }

        String[] userArray = userNames.toArray(new String[0]);

        builder.setItems(userArray, (dialog, which) -> {
            String selectedUser = userArray[which];
            String userId = userIdMap.get(selectedUser);
            presenter.assignTaskToUser(task.getTaskId(), userId);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnCreateTask.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnCreateTask.setEnabled(true);
    }

    @Override
    public void showTasks(List<Task> tasks) {
        taskList.clear();
        taskList.addAll(tasks);

        // Filter tasks by status
        List<Task> todoTasks = new ArrayList<>();
        List<Task> inProgressTasks = new ArrayList<>();
        List<Task> doneTasks = new ArrayList<>();

        for (Task task : tasks) {
            String status = task.getStatus();
            if (status != null) {
                switch (status.toUpperCase()) {
                    case "TODO":
                        todoTasks.add(task);
                        break;
                    case "IN_PROGRESS":
                        inProgressTasks.add(task);
                        break;
                    case "DONE":
                        doneTasks.add(task);
                        break;
                    default:
                        // Default to TODO if status is not set or invalid
                        todoTasks.add(task);
                        break;
                }
            } else {
                todoTasks.add(task);
            }
        }

        // Update adapters
        todoAdapter.updateTasks(todoTasks);
        inProgressAdapter.updateTasks(inProgressTasks);
        doneAdapter.updateTasks(doneTasks);

        // Update counts
        tvTodoCount.setText(String.valueOf(todoTasks.size()));
        tvInProgressCount.setText(String.valueOf(inProgressTasks.size()));
        tvDoneCount.setText(String.valueOf(doneTasks.size()));

        // Show/hide empty state
        if (tasks.isEmpty()) {
            tvNoTasks.setVisibility(View.VISIBLE);
            kanbanScrollView.setVisibility(View.GONE);
        } else {
            tvNoTasks.setVisibility(View.GONE);
            kanbanScrollView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTaskCreated(Task task) {
        Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();
        presenter.loadTasks(projectId); // Reload tasks
    }

    @Override
    public void onTaskUpdated() {
        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
        presenter.loadTasks(projectId); // Reload tasks
    }

    @Override
    public void onTaskDeleted() {
        Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
        presenter.loadTasks(projectId); // Reload tasks
    }

    @Override
    public void showUsersForAssignment(List<Map<String, String>> users) {
        usersForAssignment.clear();
        usersForAssignment.addAll(users);
        // Show create task dialog after users are loaded
        showCreateTaskDialog();
    }

    private void loadProjectDetails() {
        db.collection("projects").document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        
                        if (title != null) {
                            tvProjectTitle.setText(title);
                        }
                        if (description != null) {
                            TextView tvProjectDescription = findViewById(R.id.tvProjectDescription);
                            tvProjectDescription.setText(description);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Silent fail - just use title from intent
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}
