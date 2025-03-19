package com.javaweb.utils;

import java.util.regex.Pattern;

public class ValidateUtil {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        return email.matches(emailRegex);
    }

    public static boolean isValidVietnamesePhoneNumber(String phone) {
        String phoneRegex = "^(03|05|07|08|09)\\d{8}$";
        return Pattern.matches(phoneRegex, phone);
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }

        if (username.length() < 5 || username.length() > 50) {
            return false;
        }

        if (!username.matches("[a-zA-Z0-9_]+")) {
            return false;
        }

        return !Character.isDigit(username.charAt(0));

    }

    public static boolean isValidPassword(String password) {
        // Check if password is null or empty
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Check password length (e.g., at least 8 characters)
//        if (password.length() < 8) {
//            return false;
//        }

        // Check for at least one uppercase letter
//        if (!password.matches(".*[A-Z].*")) {
//            return false;
//        }

        // Check for at least one lowercase letter
//        if (!password.matches(".*[a-z].*")) {
//            return false;
//        }

        // Check for at least one digit
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

//        if (!password.matches(".*[!@#$%^&*().?<>~_+=-].*")) {
//            return false;
//        }

        return !password.contains(" ");
    }
}
