package com.prm392.taskmanaapp.ui.login;

import com.prm392.taskmanaapp.data.Repository.AuthRepository;

public class LoginPresenter implements LoginContract.Presenter, AuthRepository.OnLoginFinishedListener {

    private LoginContract.View loginView;
    private final AuthRepository authRepository;

    // The Presenter gets a reference to the View and creates the Model
    public LoginPresenter(LoginContract.View loginView) {
        this.loginView = loginView;
        this.authRepository = new AuthRepository();
    }

    @Override
    public void login(String email, String password) {
        if (loginView != null) {
            loginView.showLoading();
        }
        // Delegate the login task to the model
        authRepository.loginUser(email, password, this);
    }

    // This is called when the View (Activity) is destroyed
    @Override
    public void onDestroy() {
        loginView = null; // Avoid memory leaks
    }

    // --- Callbacks from the Model (AuthRepository.OnLoginFinishedListener) ---

    @Override
    public void onSuccess() {
        if (loginView != null) {
            loginView.hideLoading();
            loginView.onLoginSuccess(); // Or directly navigateToHome()
        }
    }

    @Override
    public void onError(String message) {
        if (loginView != null) {
            loginView.hideLoading();
            loginView.onLoginError(message);
        }
    }
}
