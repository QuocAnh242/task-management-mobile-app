package com.prm392.taskmanaapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.User;
import com.prm392.taskmanaapp.ui.login.LoginActivity;

public class ProfileActivity extends AppCompatActivity implements ProfileContract.View {

    private EditText etName;
    private EditText etEmail;
    private EditText etRole;
    private EditText etAvatar;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnUpdateProfile;
    private Button btnChangePassword;
    private Button btnLogout;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private ProfileContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etRole = findViewById(R.id.etRole);
        etAvatar = findViewById(R.id.etAvatar);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // Create presenter
        presenter = new ProfilePresenter(this);

        // Load user profile
        presenter.loadUserProfile();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Update profile button
        btnUpdateProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String avatar = etAvatar.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                etName.requestFocus();
                return;
            }

            presenter.updateProfile(name, avatar);
        });

        // Change password button
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Current password cannot be empty", Toast.LENGTH_SHORT).show();
                etCurrentPassword.requestFocus();
                return;
            }

            if (newPassword.isEmpty() || newPassword.length() < 6) {
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                etNewPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                etConfirmPassword.requestFocus();
                return;
            }

            presenter.changePassword(currentPassword, newPassword);
        });

        // Logout button
        btnLogout.setOnClickListener(v -> {
            presenter.logout();
        });
    }

    @Override
    public void showProfile(User user) {
        if (user != null) {
            etName.setText(user.getName());
            etEmail.setText(user.getEmail());
            etRole.setText(user.getRole());
            etAvatar.setText(user.getAvatar());
        }
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnUpdateProfile.setEnabled(false);
        btnChangePassword.setEnabled(false);
        btnLogout.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnUpdateProfile.setEnabled(true);
        btnChangePassword.setEnabled(true);
        btnLogout.setEnabled(true);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUpdateSuccess() {
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        // Clear password fields after successful update
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
