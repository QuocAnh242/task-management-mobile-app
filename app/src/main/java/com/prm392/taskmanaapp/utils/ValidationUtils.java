package com.prm392.taskmanaapp.utils;

import android.util.Patterns;

public class ValidationUtils {

    /**
     * Validates email format
     * @param email Email string to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Validates password strength
     * @param password Password to validate
     * @return true if password is at least 6 characters, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Get email validation error message
     */
    public static String getEmailErrorMessage(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address (e.g., user@example.com)";
        }
        return null;
    }

    /**
     * Get password validation error message
     */
    public static String getPasswordErrorMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        return null;
    }
}

