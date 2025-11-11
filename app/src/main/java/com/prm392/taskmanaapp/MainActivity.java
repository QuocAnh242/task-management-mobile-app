package com.prm392.taskmanaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.taskmanaapp.ui.login.LoginActivity;
import com.prm392.taskmanaapp.utils.AdminAccountCreator;
import com.prm392.taskmanaapp.utils.TestAccountCreator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create test accounts (admin and manager) if they don't exist
        TestAccountCreator.createTestAccounts();

        // Create admin account with real email
        AdminAccountCreator.createAdminAccount(
                "anhthq@metadatasolutions.vn",
                "admin123", // Default password - you can change this
                "Admin User",
                new AdminAccountCreator.OnAdminAccountCreatedListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Admin account created successfully: anhthq@metadatasolutions.vn");
                    }

                    @Override
                    public void onError(String message) {
                        // Account might already exist, which is fine
                        Log.d(TAG, "Admin account creation result: " + message);
                    }
                }
        );

        // Chuyển ngay sang LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Đóng MainActivity để không quay lại được
    }
}