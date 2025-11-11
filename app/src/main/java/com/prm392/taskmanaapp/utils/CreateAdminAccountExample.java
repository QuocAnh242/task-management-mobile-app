package com.prm392.taskmanaapp.utils;

import android.util.Log;

/**
 * Example utility class showing how to create an admin account
 * You can call this from anywhere in your app, for example in MainActivity or a setup screen
 */
public class CreateAdminAccountExample {
    private static final String TAG = "CreateAdminAccount";

    /**
     * Example: Create an admin account
     * Call this method to create an admin account with:
     * Email: admin@taskapp.com
     * Password: admin123
     * Name: Admin User
     */
    public static void createDefaultAdmin() {
        AdminAccountCreator.createDefaultAdminAccount(new AdminAccountCreator.OnAdminAccountCreatedListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Admin account created successfully!");
                // You can show a toast or notification here
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to create admin account: " + message);
                // Handle error - might already exist, which is fine
            }
        });
    }

    /**
     * Example: Create a custom admin account
     */
    public static void createCustomAdmin(String email, String password, String name) {
        AdminAccountCreator.createAdminAccount(email, password, name, new AdminAccountCreator.OnAdminAccountCreatedListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Custom admin account created successfully: " + email);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to create custom admin account: " + message);
            }
        });
    }
}

