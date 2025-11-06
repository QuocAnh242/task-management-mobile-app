package com.prm392.taskmanaapp.ui.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Task;

import java.util.ArrayList;
import java.util.List;

public class ProjectActivity extends AppCompatActivity {

    private TextView tvProjectTitle;
    private RecyclerView rvTasks;
    private Button btnCreateTask;

    private int projectId;
    private String projectTitle;
    private List<Task> taskList;
    // TODO: Create TaskAdapter
    // private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // Get project info from intent
        projectId = getIntent().getIntExtra("project_id", -1);
        projectTitle = getIntent().getStringExtra("project_title");

        // Initialize views
        tvProjectTitle = findViewById(R.id.tvProjectTitle);
        rvTasks = findViewById(R.id.rvTasks);
        btnCreateTask = findViewById(R.id.btnCreateTask);

        // Set project title
        if (projectTitle != null) {
            tvProjectTitle.setText(projectTitle);
        }

        // Initialize task list
        taskList = new ArrayList<>();

        // Setup RecyclerView
        setupTasksRecyclerView();

        // Create Task button click listener
        btnCreateTask.setOnClickListener(v -> showCreateTaskDialog());

        // Load tasks
        loadTasks();
    }

    private void setupTasksRecyclerView() {
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Create and set TaskAdapter
        // taskAdapter = new TaskAdapter(taskList, this);
        // rvTasks.setAdapter(taskAdapter);
    }

    private void loadTasks() {
        // TODO: Load tasks from database for this project
        // This is a placeholder
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

        // TODO: Populate assignTo spinner with actual users from database
        ArrayAdapter<String> assignAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        assignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignAdapter.add("Unassigned");
        assignAdapter.add("User 1");
        assignAdapter.add("User 2");
        spinnerAssignTo.setAdapter(assignAdapter);

        builder.setTitle("Create New Task")
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = etTaskTitle.getText().toString().trim();
                    String description = etTaskDescription.getText().toString().trim();
                    String priority = spinnerPriority.getSelectedItem().toString();
                    String status = spinnerStatus.getSelectedItem().toString();
                    String assignedTo = spinnerAssignTo.getSelectedItem().toString();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createTask(title, description, priority, status, assignedTo);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createTask(String title, String description, String priority, String status, String assignedTo) {
        // TODO: Create task in database
        // For now, create a local task object
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(status);
        task.setProjectId(projectId);
        // TODO: Set assignedTo userId and assignedName from spinner
        task.setAssignedName(assignedTo);

        // Add to list and notify adapter
        taskList.add(task);
        // taskAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();
    }

    public void showManageTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_manage_task, null);
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

        // TODO: Populate assignTo spinner with actual users from database
        ArrayAdapter<String> assignAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        assignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignAdapter.add("Unassigned");
        assignAdapter.add("User 1");
        assignAdapter.add("User 2");
        spinnerAssignTo.setAdapter(assignAdapter);

        // Populate fields with current task data
        etTaskTitle.setText(task.getTitle());
        etTaskDescription.setText(task.getDescription());
        
        // Set spinner selections based on current values
        int priorityPosition = priorityAdapter.getPosition(task.getPriority());
        if (priorityPosition >= 0) spinnerPriority.setSelection(priorityPosition);
        
        int statusPosition = statusAdapter.getPosition(task.getStatus());
        if (statusPosition >= 0) spinnerStatus.setSelection(statusPosition);
        
        int assignPosition = assignAdapter.getPosition(task.getAssignedName() != null ? task.getAssignedName() : "Unassigned");
        if (assignPosition >= 0) spinnerAssignTo.setSelection(assignPosition);

        builder.setTitle("Manage Task")
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = etTaskTitle.getText().toString().trim();
                    String description = etTaskDescription.getText().toString().trim();
                    String priority = spinnerPriority.getSelectedItem().toString();
                    String status = spinnerStatus.getSelectedItem().toString();
                    String assignedTo = spinnerAssignTo.getSelectedItem().toString();

                    updateTask(task, title, description, priority, status, assignedTo);
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    deleteTask(task);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateTask(Task task, String title, String description, String priority, String status, String assignedTo) {
        // Update task properties
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(status);
        // TODO: Update assignedTo userId and assignedName from spinner
        task.setAssignedName(assignedTo);

        // TODO: Update task in database

        // Notify adapter
        // taskAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void deleteTask(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Delete task from database
                    taskList.remove(task);
                    // taskAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void showAssignTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign Task to User");

        // TODO: Load users from database
        List<String> userNames = new ArrayList<>();
        userNames.add("User 1");
        userNames.add("User 2");
        userNames.add("User 3");

        String[] userArray = userNames.toArray(new String[0]);

        builder.setItems(userArray, (dialog, which) -> {
            String selectedUser = userArray[which];
            assignTaskToUser(task, selectedUser);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void assignTaskToUser(Task task, String userName) {
        // TODO: Get user ID from userName and update task
        task.setAssignedName(userName);
        // TODO: Update task in database with assignedTo userId

        // Notify adapter
        // taskAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Task assigned to " + userName, Toast.LENGTH_SHORT).show();
    }
}

