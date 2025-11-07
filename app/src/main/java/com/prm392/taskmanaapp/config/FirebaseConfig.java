package com.prm392.taskmanaapp.config;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Firebase Configuration Helper
 * Provides centralized Firebase configuration and initialization
 */
public class FirebaseConfig {

    private static final String TAG = "FirebaseConfig";
    private static boolean isInitialized = false;

    /**
     * Initialize Firebase with proper configuration
     * Should be called in Application.onCreate()
     */
    public static void initialize(Context context) {
        if (isInitialized) {
            Log.d(TAG, "Firebase already initialized");
            return;
        }

        try {
            // Initialize Firebase App
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
                Log.d(TAG, "Firebase App initialized");
            }

            // Configure Firestore
            configureFirestore();

            // Verify Firebase Auth
            verifyFirebaseAuth();

            isInitialized = true;
            Log.d(TAG, "Firebase configuration completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    /**
     * Configure Firestore settings
     */
    private static void configureFirestore() {
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true) // Enable offline persistence
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            firestore.setFirestoreSettings(settings);
            Log.d(TAG, "Firestore configured successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error configuring Firestore: " + e.getMessage(), e);
        }
    }

    /**
     * Verify Firebase Auth is available
     */
    private static void verifyFirebaseAuth() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth != null) {
                Log.d(TAG, "Firebase Auth verified");
            } else {
                Log.w(TAG, "Firebase Auth is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying Firebase Auth: " + e.getMessage(), e);
        }
    }

    /**
     * Check if Firebase is properly configured
     */
    public static boolean isConfigured() {
        return isInitialized && FirebaseApp.getInstance() != null;
    }

    /**
     * Get Firebase App instance
     */
    public static FirebaseApp getFirebaseApp(Context context) {
        if (!isInitialized) {
            initialize(context);
        }
        return FirebaseApp.getInstance();
    }
}

