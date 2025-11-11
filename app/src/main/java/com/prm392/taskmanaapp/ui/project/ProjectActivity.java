package com.prm392.taskmanaapp.ui.project;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Comment;
import com.prm392.taskmanaapp.data.Project;
import com.prm392.taskmanaapp.data.Repository.CommentRepository;
import com.prm392.taskmanaapp.data.Repository.ProjectRepository;
import com.prm392.taskmanaapp.data.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectActivity extends AppCompatActivity implements TaskContract.View {

    private TextView tvProjectTitle;
    private TextView tvProjectLeader;
    private TextView tvMemberCount;
    private TextView tvNoMembers;
    private RecyclerView rvMembers;
    private RecyclerView rvTodoTasks;
    private RecyclerView rvInProgressTasks;
    private RecyclerView rvDoneTasks;
    private TextView tvTodoCount;
    private TextView tvInProgressCount;
    private TextView tvDoneCount;
    private TextView tvNoTasks;
    private View kanbanScrollView;
    private View cardCreateTask;
    private Button btnCreateTask;
    private ProgressBar progressBar;
    private TaskAdapter todoAdapter;
    private TaskAdapter inProgressAdapter;
    private TaskAdapter doneAdapter;
    private List<Task> taskList;
    private List<Map<String, String>> usersForAssignment;
    private List<Map<String, String>> membersList;
    private CommentRepository commentRepository;
    private String projectId;
    private String projectTitle;
    private String projectDescription;
    private String leaderId;
    private boolean isAdmin;
    private TaskContract.Presenter presenter;
    private ProjectRepository projectRepository;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnEditProject;
    private Button btnDeleteProject;
    private Button btnAddMember;

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
        tvProjectLeader = findViewById(R.id.tvProjectLeader);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        tvNoMembers = findViewById(R.id.tvNoMembers);
        rvMembers = findViewById(R.id.rvMembers);
        btnEditProject = findViewById(R.id.btnEditProject);
        btnDeleteProject = findViewById(R.id.btnDeleteProject);
        btnAddMember = findViewById(R.id.btnAddMember);
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
        cardCreateTask = findViewById(R.id.cardCreateTask);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        progressBar = findViewById(R.id.progressBar);
        
        mAuth = FirebaseAuth.getInstance();
        projectRepository = new ProjectRepository();
        commentRepository = new CommentRepository();
        membersList = new ArrayList<>();

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
                if (isAdmin) {
                    showTaskDetailDialog(task);
                } else {
                    showTaskDetailDialog(task);
                }
            }

            @Override
            public void onTaskLongClick(Task task) {
                if (isAdmin) {
                    showAssignTaskDialog(task);
                }
            }
        });
        rvTodoTasks.setAdapter(todoAdapter);

        // Setup IN PROGRESS column
        rvInProgressTasks.setLayoutManager(new LinearLayoutManager(this));
        inProgressAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showTaskDetailDialog(task);
            }

            @Override
            public void onTaskLongClick(Task task) {
                if (isAdmin) {
                    showAssignTaskDialog(task);
                }
            }
        });
        rvInProgressTasks.setAdapter(inProgressAdapter);

        // Setup DONE column
        rvDoneTasks.setLayoutManager(new LinearLayoutManager(this));
        doneAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showTaskDetailDialog(task);
            }

            @Override
            public void onTaskLongClick(Task task) {
                if (isAdmin) {
                    showAssignTaskDialog(task);
                }
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
            if (userName == null || userName.isEmpty()) {
                userName = user.get("email");
            }
            // Only add if we have a valid name/email
            if (userName != null && !userName.isEmpty()) {
                userNames.add(userName);
                String userId = user.get("id");
                userIdMap.put(userName, userId != null ? userId : "");
            }
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

    private void showTaskDetailDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_task_detail, null);
        builder.setView(dialogView);

        TextView tvTaskTitle = dialogView.findViewById(R.id.tvTaskTitle);
        TextView tvTaskDescription = dialogView.findViewById(R.id.tvTaskDescription);
        TextView tvTaskPriority = dialogView.findViewById(R.id.tvTaskPriority);
        TextView tvTaskStatus = dialogView.findViewById(R.id.tvTaskStatus);
        TextView tvTaskAssignedTo = dialogView.findViewById(R.id.tvTaskAssignedTo);
        RecyclerView rvComments = dialogView.findViewById(R.id.rvComments);
        TextView tvNoComments = dialogView.findViewById(R.id.tvNoComments);
        com.google.android.material.textfield.TextInputLayout tilComment = dialogView.findViewById(R.id.tilComment);
        com.google.android.material.textfield.TextInputEditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnAddComment = dialogView.findViewById(R.id.btnAddComment);

        // Set task info
        tvTaskTitle.setText(task.getTitle());
        tvTaskDescription.setText(task.getDescription() != null && !task.getDescription().isEmpty() 
                ? task.getDescription() : "No description");
        
        // Set priority
        String priority = task.getPriority();
        tvTaskPriority.setText(priority);
        if (priority != null) {
            switch (priority.toUpperCase()) {
                case "HIGH":
                    tvTaskPriority.setBackgroundTintList(getColorStateList(R.color.priority_high));
                    break;
                case "MEDIUM":
                    tvTaskPriority.setBackgroundTintList(getColorStateList(R.color.priority_medium));
                    break;
                case "LOW":
                    tvTaskPriority.setBackgroundTintList(getColorStateList(R.color.priority_low));
                    break;
            }
        }

        // Set status
        String status = task.getStatus();
        tvTaskStatus.setText(status);
        if (status != null) {
            switch (status.toUpperCase()) {
                case "DONE":
                    tvTaskStatus.setBackgroundTintList(getColorStateList(R.color.status_done));
                    break;
                case "IN_PROGRESS":
                    tvTaskStatus.setBackgroundTintList(getColorStateList(R.color.status_in_progress));
                    break;
                case "TODO":
                default:
                    tvTaskStatus.setBackgroundTintList(getColorStateList(R.color.status_todo));
                    break;
            }
        }

        // Set assigned to
        if (task.getAssignedName() != null && !task.getAssignedName().isEmpty()) {
            tvTaskAssignedTo.setText("Assigned to: " + task.getAssignedName());
        } else {
            tvTaskAssignedTo.setText("Not assigned");
        }

        // Setup comments RecyclerView
        CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        rvComments.setLayoutManager(layoutManager);
        rvComments.setAdapter(commentAdapter);
        
        // Setup comment action listeners (only once)
        commentAdapter.setActionListener(new CommentAdapter.OnCommentActionListener() {
            @Override
            public void onEditComment(Comment comment) {
                showEditCommentDialog(comment, task, commentAdapter);
            }

            @Override
            public void onDeleteComment(Comment comment) {
                new AlertDialog.Builder(ProjectActivity.this)
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            commentRepository.deleteComment(comment.getCommentId(), new CommentRepository.OnCommentDeletedListener() {
                                @Override
                                public void onSuccess() {
                                    showSuccess("Comment deleted");
                                    // Reload comments
                                    reloadComments(task.getTaskId(), commentAdapter, tvNoComments, rvComments);
                                }

                                @Override
                                public void onError(String message) {
                                    showError(message);
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        // Load comments
        reloadComments(task.getTaskId(), commentAdapter, tvNoComments, rvComments);

        // Show comment input for all users
        tilComment.setVisibility(View.VISIBLE);
        btnAddComment.setVisibility(View.VISIBLE);
        
        // Setup @ mention support
        setupMentionSupport(etComment, task);
        
        // Store reference to prevent multiple listeners
        final boolean[] isSubmitting = {false};
        
        btnAddComment.setOnClickListener(v -> {
            // Prevent duplicate clicks
            if (isSubmitting[0]) {
                return;
            }
            
            String commentText = etComment.getText().toString().trim();
            
            // Clear previous errors
            tilComment.setError(null);
            tilComment.setErrorEnabled(false);
            
            // Validate input
            if (commentText.isEmpty()) {
                tilComment.setError("Comment cannot be empty");
                tilComment.setErrorEnabled(true);
                etComment.requestFocus();
                return;
            }
            
            if (commentText.length() > 1000) {
                tilComment.setError("Comment is too long (max 1000 characters)");
                tilComment.setErrorEnabled(true);
                etComment.requestFocus();
                return;
            }

            // Disable button to prevent duplicate submissions
            isSubmitting[0] = true;
            btnAddComment.setEnabled(false);
            btnAddComment.setText("Adding...");

            commentRepository.createComment(task.getTaskId(), commentText, new CommentRepository.OnCommentCreatedListener() {
                @Override
                public void onSuccess() {
                    etComment.setText("");
                    tilComment.setError(null);
                    tilComment.setErrorEnabled(false);
                    isSubmitting[0] = false;
                    btnAddComment.setEnabled(true);
                    btnAddComment.setText("Add Comment");
                    showSuccess("Comment added");
                    // Reload comments after a short delay to ensure data is saved
                    rvComments.postDelayed(() -> {
                        reloadComments(task.getTaskId(), commentAdapter, tvNoComments, rvComments);
                    }, 500);
                }

                @Override
                public void onError(String message) {
                    tilComment.setError(message);
                    tilComment.setErrorEnabled(true);
                    isSubmitting[0] = false;
                    btnAddComment.setEnabled(true);
                    btnAddComment.setText("Add Comment");
                    showError(message);
                }
            });
        });
        
        builder.setTitle("Task Details");
        
        // Add "Change Status" button for all users
        builder.setNeutralButton("Change Status", (dialog, which) -> {
            showChangeStatusDialog(task);
        });

        // Add manage button for admin
        if (isAdmin) {
            builder.setPositiveButton("Manage", (dialog, which) -> {
                showManageTaskDialog(task);
            });
            builder.setNegativeButton("Close", null);
        } else {
            builder.setPositiveButton("Close", null);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void reloadComments(String taskId, CommentAdapter adapter, TextView tvNoComments, RecyclerView rvComments) {
        if (taskId == null || taskId.isEmpty()) {
            if (tvNoComments != null) {
                tvNoComments.setVisibility(View.VISIBLE);
            }
            if (rvComments != null) {
                rvComments.setVisibility(View.GONE);
            }
            return;
        }
        
        // Prevent multiple simultaneous loads
        if (adapter == null) {
            return;
        }
        
        commentRepository.loadComments(taskId, new CommentRepository.OnCommentsLoadedListener() {
            @Override
            public void onSuccess(List<Comment> comments) {
                if (adapter != null) {
                    adapter.updateComments(comments);
                    if (comments == null || comments.isEmpty()) {
                        if (tvNoComments != null) {
                            tvNoComments.setVisibility(View.VISIBLE);
                        }
                        if (rvComments != null) {
                            rvComments.setVisibility(View.GONE);
                        }
                    } else {
                        if (tvNoComments != null) {
                            tvNoComments.setVisibility(View.GONE);
                        }
                        if (rvComments != null) {
                            rvComments.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (tvNoComments != null) {
                    tvNoComments.setVisibility(View.VISIBLE);
                    tvNoComments.setText("Error loading comments: " + message);
                }
                if (rvComments != null) {
                    rvComments.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void showEditCommentDialog(Comment comment, Task task, CommentAdapter adapter) {
        android.widget.EditText etEditComment = new android.widget.EditText(this);
        etEditComment.setText(comment.getContent());
        etEditComment.setHint("Edit comment... (Use @username to mention)");
        etEditComment.setMinLines(3);
        etEditComment.setMaxLines(5);
        etEditComment.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Comment")
                .setView(etEditComment)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newContent = etEditComment.getText().toString().trim();
                    if (newContent.isEmpty()) {
                        showError("Comment cannot be empty");
                        return;
                    }
                    
                    commentRepository.updateComment(comment.getCommentId(), newContent, new CommentRepository.OnCommentUpdatedListener() {
                        @Override
                        public void onSuccess() {
                            showSuccess("Comment updated");
                            // Note: Comments will be reloaded when dialog is reopened
                            // For better UX, you could use a callback to refresh the dialog
                        }

                        @Override
                        public void onError(String message) {
                            showError(message);
                        }
                    });
                })
                .setNegativeButton("Cancel", null);
        
        builder.create().show();
    }
    
    private void showChangeStatusDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Task Status");
        
        String[] statuses = {"TODO", "IN_PROGRESS", "DONE"};
        int currentIndex = 0;
        String currentStatus = task.getStatus();
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                currentIndex = i;
                break;
            }
        }
        
        builder.setSingleChoiceItems(statuses, currentIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String newStatus = statuses[selectedPosition];
                    presenter.updateTask(task, task.getTitle(), task.getDescription(), 
                        task.getPriority(), newStatus, task.getAssignedTo());
                    showSuccess("Task status updated to " + newStatus);
                })
                .setNegativeButton("Cancel", null);
        
        builder.create().show();
    }
    
    private void setupMentionSupport(com.google.android.material.textfield.TextInputEditText etComment, Task task) {
        // Load project members for @ mentions
        if (usersForAssignment.isEmpty() && projectId != null) {
            presenter.loadUsersForAssignment(projectId);
        }
        
        // Simple @ mention support - user types @username manually
        // Users can type @ followed by username (matching name or email prefix)
        // The system will automatically detect and notify mentioned users
        // For full Instagram/Facebook-style dropdown, would need custom EditText with TextWatcher
        // This simplified version allows typing @username and Enter to complete
        etComment.setHint("Add a comment... (Type @username to mention)");
        
        // Add Enter key support for better UX
        etComment.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                // Allow Enter to create new line, but could also trigger mention completion
                return false;
            }
            return false;
        });
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
            if (userName == null || userName.isEmpty()) {
                userName = user.get("email");
            }
            // Only add if we have a valid name/email
            if (userName != null && !userName.isEmpty()) {
                userNames.add(userName);
                String userId = user.get("id");
                userIdMap.put(userName, userId != null ? userId : "");
            }
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
            if (userName == null || userName.isEmpty()) {
                userName = user.get("email");
            }
            // Only add if we have a valid name/email
            if (userName != null && !userName.isEmpty()) {
                userNames.add(userName);
                String userId = user.get("id");
                userIdMap.put(userName, userId != null ? userId : "");
            }
        }

        String[] userArray = userNames.toArray(new String[0]);

        builder.setItems(userArray, (dialog, which) -> {
            String selectedUser = userArray[which];
            String userId = userIdMap.get(selectedUser);
            presenter.assignTaskToUser(task.getTaskId(), userId, projectId);
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
        showErrorSnackbar(message);
    }
    
    private void showErrorSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(R.color.project_color_2));
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    @Override
    public void onTaskCreated(Task task) {
        showSuccess("Task created successfully");
        presenter.loadTasks(projectId); // Reload tasks
    }

    @Override
    public void onTaskUpdated() {
        showSuccess("Task updated successfully");
        presenter.loadTasks(projectId); // Reload tasks
    }

    @Override
    public void onTaskDeleted() {
        showSuccess("Task deleted successfully");
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("projects").document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        leaderId = documentSnapshot.getString("leaderId");
                        String leaderName = documentSnapshot.getString("leaderName");
                        List<String> memberIds = (List<String>) documentSnapshot.get("memberIds");
                        
                        // Check if current user is admin (leader)
                        isAdmin = currentUser.getUid().equals(leaderId);
                        
                        // Update UI based on admin/member role
                        if (isAdmin) {
                            cardCreateTask.setVisibility(View.VISIBLE);
                            btnEditProject.setVisibility(View.VISIBLE);
                            btnDeleteProject.setVisibility(View.VISIBLE);
                            btnAddMember.setVisibility(View.VISIBLE);
                            
                            // Setup admin action listeners
                            setupAdminActions();
                        } else {
                            cardCreateTask.setVisibility(View.GONE);
                            btnEditProject.setVisibility(View.GONE);
                            btnDeleteProject.setVisibility(View.GONE);
                            btnAddMember.setVisibility(View.GONE);
                        }
                        
                        if (title != null) {
                            tvProjectTitle.setText(title);
                        }
                        if (description != null) {
                            projectDescription = description;
                            TextView tvProjectDescription = findViewById(R.id.tvProjectDescription);
                            tvProjectDescription.setText(description);
                        }
                        if (leaderName != null) {
                            tvProjectLeader.setText(leaderName);
                        }
                        
                        // Load members
                        if (memberIds != null && !memberIds.isEmpty()) {
                            tvMemberCount.setText(String.valueOf(memberIds.size()));
                            loadMembers(memberIds);
                        } else {
                            tvMemberCount.setText("0");
                            tvNoMembers.setVisibility(View.VISIBLE);
                            rvMembers.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Silent fail - just use title from intent
                });
    }

    private void loadMembers(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            tvNoMembers.setVisibility(View.VISIBLE);
            rvMembers.setVisibility(View.GONE);
            return;
        }

        membersList.clear();
        final int[] loadedCount = {0};
        final int totalMembers = memberIds.size();

        for (String memberId : memberIds) {
            db.collection("users").document(memberId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, String> member = new HashMap<>();
                            member.put("id", memberId);
                            member.put("name", documentSnapshot.getString("name"));
                            member.put("email", documentSnapshot.getString("email"));
                            membersList.add(member);
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] == totalMembers) {
                            updateMembersUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == totalMembers) {
                            updateMembersUI();
                        }
                    });
        }
    }

    private void updateMembersUI() {
        if (membersList.isEmpty()) {
            tvNoMembers.setVisibility(View.VISIBLE);
            rvMembers.setVisibility(View.GONE);
        } else {
            tvNoMembers.setVisibility(View.GONE);
            rvMembers.setVisibility(View.VISIBLE);
            // Simple adapter for members
            rvMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvMembers.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    TextView textView = new TextView(ProjectActivity.this);
                    textView.setPadding(12, 8, 12, 8);
                    textView.setTextSize(12);
                    textView.setTextColor(getResources().getColor(R.color.text_primary));
                    textView.setBackgroundResource(R.drawable.badge_background);
                    textView.getBackground().setTint(getResources().getColor(R.color.primary_light));
                    return new RecyclerView.ViewHolder(textView) {};
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    TextView textView = (TextView) holder.itemView;
                    Map<String, String> member = membersList.get(position);
                    String name = member.get("name");
                    if (name == null || name.isEmpty()) {
                        name = member.get("email");
                    }
                    final String finalName = name;
                    final String finalMemberId = member.get("id");
                    textView.setText(finalName);
                    
                    // Add remove button for admin
                    if (isAdmin) {
                        textView.setOnLongClickListener(v -> {
                            showRemoveMemberDialog(finalMemberId, finalName);
                            return true;
                        });
                    }
                }

                @Override
                public int getItemCount() {
                    return membersList.size();
                }
            });
        }
    }

    private void setupAdminActions() {
        // Edit Project
        btnEditProject.setOnClickListener(v -> showEditProjectDialog());
        
        // Delete Project
        btnDeleteProject.setOnClickListener(v -> showDeleteProjectDialog());
        
        // Add Member
        btnAddMember.setOnClickListener(v -> showAddMemberDialog());
    }

    private void showEditProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etProjectTitle);
        EditText etDescription = dialogView.findViewById(R.id.etProjectDescription);

        etTitle.setText(projectTitle);
        etDescription.setText(projectDescription);

        builder.setTitle("Edit Project")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newDescription = etDescription.getText().toString().trim();

                    if (newTitle.isEmpty()) {
                        showError("Project title cannot be empty");
                        return;
                    }

                    Project project = new Project();
                    project.setProjectId(projectId);
                    project.setTitle(newTitle);
                    project.setDescription(newDescription);

                    projectRepository.updateProject(project, new ProjectRepository.OnProjectUpdatedListener() {
                        @Override
                        public void onSuccess() {
                            projectTitle = newTitle;
                            projectDescription = newDescription;
                            tvProjectTitle.setText(newTitle);
                            TextView tvProjectDescription = findViewById(R.id.tvProjectDescription);
                            tvProjectDescription.setText(newDescription);
                            showSuccess("Project updated successfully");
                        }

                        @Override
                        public void onError(String message) {
                            showError(message);
                        }
                    });
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showDeleteProjectDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete this project? All tasks will be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    projectRepository.deleteProject(projectId, new ProjectRepository.OnProjectDeletedListener() {
                        @Override
                        public void onSuccess() {
                            showSuccess("Project deleted successfully");
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            showError(message);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        EditText etEmail = new EditText(this);
        etEmail.setHint("Enter member email");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        
        builder.setView(etEmail);
        builder.setTitle("Invite Member")
                .setPositiveButton("Invite", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    if (email.isEmpty()) {
                        showError("Please enter an email address");
                        return;
                    }

                    projectRepository.inviteUserToProject(projectId, email, new ProjectRepository.OnUserInvitedListener() {
                        @Override
                        public void onSuccess() {
                            showSuccess("Invitation sent successfully");
                        }

                        @Override
                        public void onError(String message) {
                            showError(message);
                        }
                    });
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showRemoveMemberDialog(String memberId, String memberName) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + memberName + " from the project?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    projectRepository.removeMember(projectId, memberId, new ProjectRepository.OnProjectUpdatedListener() {
                        @Override
                        public void onSuccess() {
                            showSuccess("Member removed successfully");
                            // Reload project details
                            loadProjectDetails();
                        }

                        @Override
                        public void onError(String message) {
                            showError(message);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSuccess(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(R.color.status_done));
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}
