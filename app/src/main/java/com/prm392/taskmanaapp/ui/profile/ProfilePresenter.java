package com.prm392.taskmanaapp.ui.profile;

import com.google.firebase.auth.FirebaseAuth;
import com.prm392.taskmanaapp.data.Repository.AuthRepository;
import com.prm392.taskmanaapp.data.User;

public class ProfilePresenter implements ProfileContract.Presenter {

    private ProfileContract.View view;
    private final AuthRepository authRepository;
    private final FirebaseAuth firebaseAuth;

    public ProfilePresenter(ProfileContract.View view) {
        this.view = view;
        this.authRepository = new AuthRepository();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void loadUserProfile() {
        if (view != null) {
            view.showLoading();
        }

        authRepository.getUserProfile(new AuthRepository.OnProfileLoadedListener() {
            @Override
            public void onSuccess(User user) {
                if (view != null) {
                    view.hideLoading();
                    view.showProfile(user);
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void updateProfile(String name, String avatar) {
        if (view != null) {
            view.showLoading();
        }

        authRepository.updateProfile(name, avatar, new AuthRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.showUpdateSuccess();
                    // Reload profile to get updated data
                    loadUserProfile();
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        if (view != null) {
            view.showLoading();
        }

        authRepository.changePassword(currentPassword, newPassword, new AuthRepository.OnPasswordChangeListener() {
            @Override
            public void onSuccess() {
                if (view != null) {
                    view.hideLoading();
                    view.showSuccess("Password changed successfully");
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(message);
                }
            }
        });
    }

    @Override
    public void logout() {
        firebaseAuth.signOut();
        if (view != null) {
            view.navigateToLogin();
        }
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
