package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            listener.onError("Please enter a valid email address (e.g., user@example.com)");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        String errorMessage = "Login failed";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && error.contains("CONFIGURATION_NOT_FOUND")) {
                                errorMessage = "Firebase configuration error. Please add SHA-1/SHA-256 fingerprints to Firebase Console.";
                            } else {
                                errorMessage = error;
                            }
                        }
                        listener.onError(errorMessage);
                    }
                });
    }

    public void registerUser(String name, String email, String password, String role, OnRegisterFinishedListener listener) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            listener.onError("Name, email and password cannot be empty.");
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            listener.onError("Please enter a valid email address (e.g., user@example.com)");
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

    public void loginWithGoogle(String idToken, OnLoginFinishedListener listener) {
        if (idToken == null || idToken.isEmpty()) {
            listener.onError("Invalid Google ID token");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if user exists in Firestore, if not create profile
                            db.collection("users")
                                    .document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (!documentSnapshot.exists()) {
                                            // Create new user profile for Google Sign-In user
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "Google User");
                                            userData.put("email", user.getEmail());
                                            userData.put("role", "MEMBER"); // Default role
                                            userData.put("avatar", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

                                            db.collection("users")
                                                    .document(user.getUid())
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                                                    .addOnFailureListener(e -> listener.onError("Login successful but failed to create profile: " + e.getMessage()));
                                        } else {
                                            // User already exists
                                            listener.onSuccess();
                                        }
                                    })
                                    .addOnFailureListener(e -> listener.onError("Login successful but failed to verify user: " + e.getMessage()));
                        } else {
                            listener.onSuccess();
                        }
                    } else {
                        String errorMessage = "Google Sign-In failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        listener.onError(errorMessage);
                    }
                });
    }
}