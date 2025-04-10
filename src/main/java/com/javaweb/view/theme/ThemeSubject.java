package com.javaweb.view.theme;

public interface ThemeSubject {
    void addThemeChangeListener(ThemeChangeListener changeListener);

    void removeThemeChangeListener(ThemeChangeListener changeListener);

    void notifyListeners();
}
