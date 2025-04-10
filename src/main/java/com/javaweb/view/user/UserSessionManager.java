package com.javaweb.view.user;

import com.javaweb.model.dto.UserDTO;
import lombok.Getter;

@Getter
public class UserSessionManager {
    private static UserSessionManager instance;

    private UserDTO currentUser;

    private UserSessionManager() {
    }

    public static synchronized UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    public void initializeSession(UserDTO user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }


    public void clearSession() {
        this.currentUser = null;
    }

    public <T> T getUserAttribute(UserAttributeGetter<T> attributeGetter, T defaultValue) {
        return currentUser != null ? attributeGetter.get(currentUser) : defaultValue;
    }

    @FunctionalInterface
    public interface UserAttributeGetter<T> {
        T get(UserDTO user);
    }
}
