package com.prm392.taskmanaapp.ui.register;

public interface RegisterContract {
    interface View {
        void showLoading();
        void hideLoading();
        void onRegisterSuccess();
        void onRegisterError(String message);
        void navigateToHome();
    }

    interface Presenter {
        void register(String name, String email, String password, String role);
        void onDestroy();
    }
}

