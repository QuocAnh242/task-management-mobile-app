package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    // Listener to communicate results back to the Presenter
    public interface OnLoginFinishedListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnRegisterFinishedListener {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void loginUser(String email, String password, OnLoginFinishedListener listener) {
        if (email.isEmpty() || password.isEmpty()) {
            listener.onError("Email and password cannot be empty.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        // Provide a more specific error message if possible
                        listener.onError(task.getException().getMessage());
                    }
                });
    }

    public void registerUser(String name, String email, String password, String role, OnRegisterFinishedListener listener) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            listener.onError("Name, email and password cannot be empty.");
            return;
        }

        if (password.length() < 6) {
            listener.onError("Password must be at least 6 characters.");
            return;
        }

        // Default role to MEMBER if not specified
        String userRole = (role == null || role.isEmpty()) ? "MEMBER" : role;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("role", userRole);
                            userData.put("avatar", "");

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        listener.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        listener.onError("Registration successful but failed to save user data: " + e.getMessage());
                                    });
                        } else {
                            listener.onSuccess();
                        }
                    } else {
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && error.contains("CONFIGURATION_NOT_FOUND")) {
                                errorMessage = "Firebase configuration error. Please check Firebase Console settings.";
                            } else {
                                errorMessage = error;
                            }
                        }
                        listener.onError(errorMessage);
                    }
                });
    }
}