package com.prm392.taskmanaapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.User;
import com.prm392.taskmanaapp.ui.login.LoginActivity;
import com.prm392.taskmanaapp.ui.home.HomeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private TextView tvNoUsers;
    private TextView tvWelcome;
    private TextView tvTotalUsers;
    private TextView tvTotalProjects;
    private TextView tvTotalTasks;
    private Button btnLogout;
    private ProgressBar progressBar;

    private UserAdapter userAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<User> userList;
    private Map<String, String> userDocumentIds; // Map email -> document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load user name
        loadUserName();

        // Load data
        loadUsers();
        loadStatistics();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        rvUsers = findViewById(R.id.rvUsers);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalProjects = findViewById(R.id.tvTotalProjects);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
        userList = new ArrayList<>();
        userDocumentIds = new HashMap<>();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onEditRole(User user) {
                showEditRoleDialog(user);
            }

            @Override
            public void onDeleteUser(User user) {
                showDeleteUserDialog(user);
            }
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvWelcome.setText("Welcome, " + name + " (Admin)");
                            } else {
                                tvWelcome.setText("Welcome, Admin");
                            }
                        } else {
                            tvWelcome.setText("Welcome, Admin");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvWelcome.setText("Welcome, Admin");
                    });
        }
    }

    private void loadUsers() {
        showProgress(true);
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showProgress(false);
                    userList.clear();
                    userDocumentIds.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String role = document.getString("role");
                        String avatar = document.getString("avatar");

                        User user = new User();
                        user.setId(0); // Firestore uses String IDs, but User model uses int
                        user.setName(name != null ? name : "");
                        user.setEmail(email != null ? email : "");
                        user.setRole(role != null ? role : "MEMBER");
                        user.setAvatar(avatar != null ? avatar : "");
                        
                        userList.add(user);
                        if (user.getEmail() != null) {
                            userDocumentIds.put(user.getEmail(), documentId); // Store document ID for later use
                        }
                    }
                    updateUsersUI();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvNoUsers.setVisibility(View.VISIBLE);
                    rvUsers.setVisibility(View.GONE);
                });
    }

    private void updateUsersUI() {
        if (userList.isEmpty()) {
            tvNoUsers.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            tvNoUsers.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
            userAdapter.notifyDataSetChanged();
        }
        tvTotalUsers.setText(String.valueOf(userList.size()));
    }

    private void loadStatistics() {
        // Load total projects
        db.collection("projects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvTotalProjects.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    tvTotalProjects.setText("0");
                });

        // Load total tasks
        db.collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvTotalTasks.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    tvTotalTasks.setText("0");
                });
    }

    private void showEditRoleDialog(User user) {
        String[] roles = {"MEMBER", "MANAGER", "ADMIN"};
        int currentRoleIndex = 0;
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(user.getRole())) {
                currentRoleIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit User Role")
                .setSingleChoiceItems(roles, currentRoleIndex, null)
                .setPositiveButton("Save", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String newRole = roles[selectedPosition];
                    updateUserRole(user, newRole);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserRole(User user, String newRole) {
        showProgress(true);
        // Get document ID from map using email
        String documentId = user.getEmail() != null ? userDocumentIds.get(user.getEmail()) : null;
        if (documentId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("role", newRole);
            
            db.collection("users").document(documentId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        showProgress(false);
                        user.setRole(newRole);
                        userAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "User role updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        showProgress(false);
                        Toast.makeText(this, "Error updating role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Fallback: find by email
            db.collection("users")
                    .whereEqualTo("email", user.getEmail())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("role", newRole);
                            
                            document.getReference().update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        showProgress(false);
                                        user.setRole(newRole);
                                        if (user.getEmail() != null) {
                                            userDocumentIds.put(user.getEmail(), document.getId());
                                        }
                                        userAdapter.notifyDataSetChanged();
                                        Toast.makeText(this, "User role updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        showProgress(false);
                                        Toast.makeText(this, "Error updating role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            showProgress(false);
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showProgress(false);
                        Toast.makeText(this, "Error finding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showDeleteUserDialog(User user) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail().equals(user.getEmail())) {
            Toast.makeText(this, "You cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user: " + user.getEmail() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteUser(user);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        showProgress(true);
        // Get document ID from map using email
        String documentId = user.getEmail() != null ? userDocumentIds.get(user.getEmail()) : null;
        if (documentId != null) {
            db.collection("users").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                                        showProgress(false);
                                        userList.remove(user);
                                        if (user.getEmail() != null) {
                                            userDocumentIds.remove(user.getEmail());
                                        }
                                        updateUsersUI();
                        Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        showProgress(false);
                        Toast.makeText(this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Fallback: find by email
            db.collection("users")
                    .whereEqualTo("email", user.getEmail())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        showProgress(false);
                                        userList.remove(user);
                                        if (user.getEmail() != null) {
                                            userDocumentIds.remove(user.getEmail());
                                        }
                                        updateUsersUI();
                                        Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        showProgress(false);
                                        Toast.makeText(this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            showProgress(false);
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showProgress(false);
                        Toast.makeText(this, "Error finding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
        loadStatistics();
    }
}

