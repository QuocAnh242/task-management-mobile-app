package com.prm392.taskmanaapp.ui.login;

public interface LoginContract {
    interface View {
        void showLoading();
        void hideLoading();
        void onLoginSuccess();
        void onLoginError(String message);
        void navigateToHome();
        // We can add more specific view actions here later
    }

    interface Presenter {
        void login(String email, String password);
        void onDestroy(); // Important for preventing memory leaks
    }
}