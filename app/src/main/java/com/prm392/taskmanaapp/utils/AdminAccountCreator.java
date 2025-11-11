package com.prm392.taskmanaapp.utils;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminAccountCreator {
    private static final String TAG = "AdminAccountCreator";
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnAdminAccountCreatedListener {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Create an admin account with the specified credentials
     * @param email Admin email
     * @param password Admin password
     * @param name Admin name
     * @param listener Callback for success/error
     */
    public static void createAdminAccount(String email, String password, String name, OnAdminAccountCreatedListener listener) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            if (listener != null) {
                listener.onError("Email and password cannot be empty");
            }
            return;
        }

        if (password.length() < 6) {
            if (listener != null) {
                listener.onError("Password must be at least 6 characters");
            }
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore with ADMIN role
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name != null && !name.isEmpty() ? name : "Admin User");
                            userData.put("email", email);
                            userData.put("role", "ADMIN");
                            userData.put("avatar", "");

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Admin account created successfully: " + email);
                                        if (listener != null) {
                                            listener.onSuccess();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error saving admin account data: " + e.getMessage());
                                        if (listener != null) {
                                            listener.onError("Account created but failed to save user data: " + e.getMessage());
                                        }
                                    });
                        } else {
                            if (listener != null) {
                                listener.onError("Account creation failed: User is null");
                            }
                        }
                    } else {
                        String errorMessage = "Failed to create admin account";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && (error.contains("already exists") || error.contains("EMAIL_EXISTS"))) {
                                // Account already exists - try to update role to ADMIN in Firestore
                                Log.d(TAG, "Admin account already exists: " + email + ". Updating role to ADMIN...");
                                
                                // Find user by email and update role
                                db.collection("users")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                // Update role to ADMIN
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("role", "ADMIN");
                                                
                                                queryDocumentSnapshots.getDocuments().get(0).getReference()
                                                        .update(updates)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "Updated existing account to ADMIN role: " + email);
                                                            if (listener != null) {
                                                                listener.onSuccess(); // Treat as success since role is now ADMIN
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Error updating role: " + e.getMessage());
                                                            if (listener != null) {
                                                                listener.onError("Account exists but failed to update role: " + e.getMessage());
                                                            }
                                                        });
                                            } else {
                                                Log.w(TAG, "Account exists in Auth but not in Firestore: " + email);
                                                if (listener != null) {
                                                    listener.onError("Account exists but not found in database. Please login first.");
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error finding user: " + e.getMessage());
                                            if (listener != null) {
                                                listener.onError("Account exists but failed to verify: " + e.getMessage());
                                            }
                                        });
                            } else {
                                errorMessage = error;
                                Log.e(TAG, "Error creating admin account: " + error);
                                if (listener != null) {
                                    listener.onError(errorMessage);
                                }
                            }
                        } else {
                            if (listener != null) {
                                listener.onError(errorMessage);
                            }
                        }
                    }
                });
    }

    /**
     * Create a default admin account (admin@taskapp.com / admin123)
     */
    public static void createDefaultAdminAccount(OnAdminAccountCreatedListener listener) {
        createAdminAccount("admin@taskapp.com", "admin123", "Admin User", listener);
    }
}

