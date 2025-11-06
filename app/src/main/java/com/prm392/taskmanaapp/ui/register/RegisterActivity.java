package com.prm392.taskmanaapp.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.ui.home.HomeActivity;
import com.prm392.taskmanaapp.ui.login.LoginActivity;

public class RegisterActivity extends AppCompatActivity implements RegisterContract.View {

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Spinner spinnerRole;
    private Button btnRegister;
    private ProgressBar progressBar;

    private RegisterContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // Create the Presenter
        presenter = new RegisterPresenter(this);

        // Populate role spinner - Remove ADMIN and MANAGER from registration (only for test accounts)
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        
        // Set default selection to MEMBER (index 0)
        spinnerRole.setSelection(0);

        // Navigate to login
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Register button click listener
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            // Validate inputs
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Default role to MEMBER for new registrations
            // ADMIN and MANAGER should only be created as test accounts
            if (role.equals("ADMIN") || role.equals("MANAGER")) {
                Toast.makeText(this, "ADMIN and MANAGER roles are restricted. Defaulting to MEMBER.", Toast.LENGTH_LONG).show();
                role = "MEMBER";
            }

            presenter.register(name, email, password, role);
        });
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
    }

    @Override
    public void onRegisterSuccess() {
        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }

    @Override
    public void onRegisterError(String message) {
        Toast.makeText(this, "Registration Failed: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}

