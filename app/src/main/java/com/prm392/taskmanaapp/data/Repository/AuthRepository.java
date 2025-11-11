package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prm392.taskmanaapp.data.User;

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

    public interface OnProfileLoadedListener {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface OnProfileUpdateListener {
        void onSuccess();
        void onError(String message);
    }

    public interface OnPasswordChangeListener {
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

    // Get current user profile from Firestore
    public void getUserProfile(OnProfileLoadedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = mapDocumentToUser(documentSnapshot, currentUser);
                        listener.onSuccess(user);
                    } else {
                        listener.onError("User profile not found");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Failed to load profile: " + e.getMessage()));
    }

    // Update user profile in Firestore
    public void updateProfile(String name, String avatar, OnProfileUpdateListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            listener.onError("Name cannot be empty");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name.trim());
        if (avatar != null) {
            updates.put("avatar", avatar);
        }

        db.collection("users")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to update profile: " + e.getMessage()));
    }

    // Change password (requires re-authentication)
    public void changePassword(String currentPassword, String newPassword, OnPasswordChangeListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not logged in");
            return;
        }

        if (currentPassword == null || currentPassword.isEmpty()) {
            listener.onError("Current password cannot be empty");
            return;
        }

        if (newPassword == null || newPassword.length() < 6) {
            listener.onError("New password must be at least 6 characters");
            return;
        }

        String email = currentUser.getEmail();
        if (email == null) {
            listener.onError("Cannot change password for this account");
            return;
        }

        // Re-authenticate user before changing password
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Update password
                    currentUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid2 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onError("Failed to update password: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Current password is incorrect"));
    }

    // Helper method to map Firestore document to User object
    private User mapDocumentToUser(DocumentSnapshot doc, FirebaseUser firebaseUser) {
        User user = new User();
        user.setName(doc.getString("name"));
        user.setEmail(firebaseUser.getEmail());
        user.setRole(doc.getString("role"));
        user.setAvatar(doc.getString("avatar"));
        return user;
    }
}