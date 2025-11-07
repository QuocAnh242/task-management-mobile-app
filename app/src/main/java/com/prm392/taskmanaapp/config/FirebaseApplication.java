package com.prm392.taskmanaapp.config;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class FirebaseApplication extends Application {

    private static final String TAG = "FirebaseApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase using the config helper
        try {
            FirebaseConfig.initialize(this);
            Log.d(TAG, "Application initialized with Firebase");
        } catch (Exception e) {
            Log.e(TAG, "Critical error: Failed to initialize Firebase in Application", e);
            // Don't throw - let the app continue, but log the error
        }
    }
}

