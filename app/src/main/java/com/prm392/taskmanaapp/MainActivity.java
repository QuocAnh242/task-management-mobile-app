package com.prm392.taskmanaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.taskmanaapp.ui.login.LoginActivity;
import com.prm392.taskmanaapp.utils.TestAccountCreator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create test accounts (admin and manager) if they don't exist
        TestAccountCreator.createTestAccounts();

        // Chuyển ngay sang LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Đóng MainActivity để không quay lại được
    }
}