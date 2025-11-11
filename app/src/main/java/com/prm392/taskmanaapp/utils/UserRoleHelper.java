package com.prm392.taskmanaapp.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRoleHelper {

    public interface OnRoleLoadedListener {
        void onRoleLoaded(String role);
        void onError(String message);
    }

    public static void getCurrentUserRole(OnRoleLoadedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            listener.onError("User not logged in");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null && !role.isEmpty()) {
                            listener.onRoleLoaded(role);
                        } else {
                            listener.onRoleLoaded("MEMBER"); // Default role
                        }
                    } else {
                        listener.onRoleLoaded("MEMBER"); // Default role
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }
}

