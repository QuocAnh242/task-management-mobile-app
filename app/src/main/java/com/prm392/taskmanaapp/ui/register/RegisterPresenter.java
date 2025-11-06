package com.prm392.taskmanaapp.ui.register;

import com.prm392.taskmanaapp.data.Repository.AuthRepository;

public class RegisterPresenter implements RegisterContract.Presenter, AuthRepository.OnRegisterFinishedListener {

    private RegisterContract.View registerView;
    private final AuthRepository authRepository;

    public RegisterPresenter(RegisterContract.View registerView) {
        this.registerView = registerView;
        this.authRepository = new AuthRepository();
    }

    @Override
    public void register(String name, String email, String password, String role) {
        if (registerView != null) {
            registerView.showLoading();
        }
        authRepository.registerUser(name, email, password, role, this);
    }

    @Override
    public void onDestroy() {
        registerView = null;
    }

    @Override
    public void onSuccess() {
        if (registerView != null) {
            registerView.hideLoading();
            registerView.onRegisterSuccess();
        }
    }

    @Override
    public void onError(String message) {
        if (registerView != null) {
            registerView.hideLoading();
            registerView.onRegisterError(message);
        }
    }
}

