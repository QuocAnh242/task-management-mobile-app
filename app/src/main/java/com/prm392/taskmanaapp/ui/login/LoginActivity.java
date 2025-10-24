package com.prm392.taskmanaapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.ui.home.HomeActivity; // Assuming you have a HomeActivity


public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Create the Presenter, passing it a reference to this View
        presenter = new LoginPresenter(this);

        // The View's only logic is to tell the presenter what happened
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            presenter.login(email, password);
        });
    }

    // --- Implementation of LoginContract.View methods ---

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }

    @Override
    public void onLoginError(String message) {
        Toast.makeText(this, "Login Failed: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish(); // Prevent user from going back to login screen
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // IMPORTANT: Tell the presenter that the view is being destroyed
        presenter.onDestroy();
    }
}