package com.prm392.taskmanaapp.helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Firebase Helper class for centralized Firebase configuration
 * Similar to the old project's FirebaseHelper but adapted for Firestore
 */
public class FirebaseHelper {

    private static FirebaseHelper instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    private FirebaseHelper() {
        // Initialize Firestore with settings
        this.firestore = FirebaseFirestore.getInstance();
        
        // Configure Firestore settings for better offline support
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        firestore.setFirestoreSettings(settings);

        // Initialize Auth
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Get singleton instance
     */
    public static FirebaseHelper getInstance() {
        if (instance == null) {
            synchronized (FirebaseHelper.class) {
                if (instance == null) {
                    instance = new FirebaseHelper();
                }
            }
        }
        return instance;
    }

    /**
     * Get Firestore instance
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Get Firebase Auth instance
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /**
     * Get users collection reference
     */
    public com.google.firebase.firestore.CollectionReference getUsersReference() {
        return firestore.collection("users");
    }

    /**
     * Get projects collection reference
     */
    public com.google.firebase.firestore.CollectionReference getProjectsReference() {
        return firestore.collection("projects");
    }

    /**
     * Get tasks collection reference
     */
    public com.google.firebase.firestore.CollectionReference getTasksReference() {
        return firestore.collection("tasks");
    }

    /**
     * Get a specific user document reference
     */
    public com.google.firebase.firestore.DocumentReference getUserDocument(String userId) {
        return firestore.collection("users").document(userId);
    }

    /**
     * Get a specific project document reference
     */
    public com.google.firebase.firestore.DocumentReference getProjectDocument(String projectId) {
        return firestore.collection("projects").document(projectId);
    }

    /**
     * Get a specific task document reference
     */
    public com.google.firebase.firestore.DocumentReference getTaskDocument(String taskId) {
        return firestore.collection("tasks").document(taskId);
    }
}

