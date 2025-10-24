package com.prm392.taskmanaapp.data.Repository;

import com.google.firebase.auth.FirebaseAuth;

public class AuthRepository {

    // Listener to communicate results back to the Presenter
    public interface OnLoginFinishedListener {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseAuth mAuth;

    public AuthRepository() {
        this.mAuth = FirebaseAuth.getInstance();
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
}