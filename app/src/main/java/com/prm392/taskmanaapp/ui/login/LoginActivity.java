package com.prm392.taskmanaapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.ui.home.HomeActivity;
import com.prm392.taskmanaapp.ui.register.RegisterActivity;
import com.prm392.taskmanaapp.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private static final String TAG = "LoginActivity";
    
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnGoogleSignIn;
    private ProgressBar progressBar;

    private LoginContract.Presenter presenter;
    private GoogleSignInClient googleSignInClient;

    // Activity Result Launcher for Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleGoogleSignInResult(task);
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Create the Presenter, passing it a reference to this View
        presenter = new LoginPresenter(this);

        // Email/Password login button
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate email format
            if (!ValidationUtils.isValidEmail(email)) {
                String errorMsg = ValidationUtils.getEmailErrorMessage(email);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                etEmail.requestFocus();
                return;
            }

            // Validate password
            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                etPassword.requestFocus();
                return;
            }

            presenter.login(email, password);
        });

        // Google Sign-In button
        btnGoogleSignIn.setOnClickListener(v -> launchGoogleSignIn());

        // Navigate to RegisterActivity
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
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
    public void launchGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, authenticate with Firebase
            String idToken = account.getIdToken();
            if (idToken != null) {
                presenter.loginWithGoogle(idToken);
            } else {
                Toast.makeText(this, "Failed to get ID token", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            // Sign in failed
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // IMPORTANT: Tell the presenter that the view is being destroyed
        presenter.onDestroy();
    }
}