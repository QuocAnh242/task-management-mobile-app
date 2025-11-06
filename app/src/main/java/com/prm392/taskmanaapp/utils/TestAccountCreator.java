package com.prm392.taskmanaapp.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TestAccountCreator {
    private static final String TAG = "TestAccountCreator";
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void createTestAccounts() {
        // Create Admin account
        createTestAccount(
                "admin@taskapp.com",
                "admin123",
                "Admin User",
                "ADMIN",
                () -> Log.d(TAG, "Admin account created successfully")
        );

        // Create Manager account
        createTestAccount(
                "manager@taskapp.com",
                "manager123",
                "Manager User",
                "MANAGER",
                () -> Log.d(TAG, "Manager account created successfully")
        );
    }

    private static void createTestAccount(String email, String password, String name, String role, Runnable onSuccess) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("role", role);
                            userData.put("avatar", "");

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, role + " account data saved to Firestore");
                                        if (onSuccess != null) {
                                            onSuccess.run();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error saving " + role + " account data: " + e.getMessage());
                                    });
                        }
                    } else {
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && error.contains("already exists")) {
                                Log.d(TAG, role + " account already exists: " + email);
                            } else {
                                Log.e(TAG, "Error creating " + role + " account: " + error);
                            }
                        }
                    }
                });
    }

    public static void signOut() {
        mAuth.signOut();
    }
}

