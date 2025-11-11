package com.prm392.taskmanaapp.ui.profile;

import com.prm392.taskmanaapp.data.User;

public interface ProfileContract {
    interface View {
        void showProfile(User user);
        void showLoading();
        void hideLoading();
        void showError(String message);
        void showSuccess(String message);
        void showUpdateSuccess();
        void navigateToLogin();
    }

    interface Presenter {
        void loadUserProfile();
        void updateProfile(String name, String avatar);
        void changePassword(String currentPassword, String newPassword);
        void logout();
        void onDestroy();
    }
}
