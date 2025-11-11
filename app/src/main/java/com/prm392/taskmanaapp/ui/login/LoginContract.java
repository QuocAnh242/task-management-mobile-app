package com.prm392.taskmanaapp.ui.login;

public interface LoginContract {
    interface View {
        void showLoading();
        void hideLoading();
        void onLoginSuccess();
        void onLoginError(String message);
        void navigateToHome();
        void launchGoogleSignIn(); // New method for launching Google Sign-In
        // We can add more specific view actions here later
    }

    interface Presenter {
        void login(String email, String password);
        void loginWithGoogle(String idToken); // New method for Google Sign-In
        void onDestroy(); // Important for preventing memory leaks
    }
}