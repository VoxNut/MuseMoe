package com.javaweb.view.theme;

import com.javaweb.constant.AppConstant;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerMediator;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ThemeManager implements ThemeSubject {
    private static ThemeManager instance;

    private Color backgroundColor = AppConstant.BACKGROUND_COLOR;
    private Color textColor = AppConstant.TEXT_COLOR;
    private Color accentColor = AppConstant.BACKGROUND_COLOR.darker();

    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    private ThemeManager() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }


    /**
     * Update the global theme colors and notify all listeners
     */
    public void setThemeColors(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        // Notify all listeners about the theme change
        notifyListeners();

        // Also notify through the existing mediator for backward compatibility
        MusicPlayerMediator.getInstance().notifyColorsChanged(backgroundColor, textColor, accentColor);
    }

    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(backgroundColor, textColor, accentColor);
        }
    }
}
