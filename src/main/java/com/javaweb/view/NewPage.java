// Template for new pages with theme support
package com.javaweb.view;

import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class NewPage extends JFrame implements ThemeChangeListener {
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    public NewPage() {
        // Initialize with current theme
        backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        textColor = ThemeManager.getInstance().getTextColor();
        accentColor = ThemeManager.getInstance().getAccentColor();

        // Apply initial theme
        applyTheme(backgroundColor, textColor, accentColor);

        // Register for theme changes
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        // Apply updated theme to all components
        applyTheme(backgroundColor, textColor, accentColor);
    }

    private void applyTheme(Color backgroundColor, Color textColor, Color accentColor) {
        // Apply colors to all components in this page
        getContentPane().setBackground(backgroundColor);

        // Example: Apply to a panel
        JPanel mainPanel = (JPanel) getContentPane().getComponent(0);
        mainPanel.setBackground(backgroundColor);

        // Example: Apply radial gradient
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(backgroundColor, 0.1f),
                GuiUtil.darkenColor(backgroundColor, 0.1f),
                0.5f, 0.5f, 0.8f);

        // Apply to other components as needed
    }

    @Override
    public void dispose() {
        // Unregister when window is closed
        ThemeManager.getInstance().removeThemeChangeListener(this);
        super.dispose();
    }
}