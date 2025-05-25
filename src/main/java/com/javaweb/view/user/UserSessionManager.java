package com.javaweb.view.user;

import com.javaweb.model.dto.UserDTO;
import lombok.Getter;

import java.util.Date;

@Getter
public class UserSessionManager {
    private static UserSessionManager instance;

    private UserDTO currentUser;
    private String authToken;

    private UserSessionManager() {
    }

    public static synchronized UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    public void initializeSession(UserDTO user, String token) {
        this.currentUser = user;
        this.authToken = token;
    }

    public boolean isLoggedIn() {
        return currentUser != null && authToken != null && !isTokenExpired();
    }

    public boolean isTokenExpired() {
        if (currentUser == null || currentUser.getTokenExpiration() == null) {
            return true;
        }
        return new Date().after(currentUser.getTokenExpiration());
    }

    public void clearSession() {
        this.authToken = null;
        this.currentUser = null;
    }

    public <T> T getUserAttribute(UserAttributeGetter<T> attributeGetter, T defaultValue) {
        return currentUser != null ? attributeGetter.get(currentUser) : defaultValue;
    }

    public void updateUserInfo(UserDTO currentUser) {
        this.currentUser.setEmail(currentUser.getEmail());
        this.currentUser.setFullName(currentUser.getFullName());
    }

    @FunctionalInterface
    public interface UserAttributeGetter<T> {
        T get(UserDTO user);
    }


}