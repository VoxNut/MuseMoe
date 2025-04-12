// Template for custom components with theme support
package com.javaweb.view.components;

import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.theme.Themeable;

import javax.swing.*;
import java.awt.*;

public class CustomPanel extends JPanel implements Themeable {
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    public CustomPanel() {
        // Initialize from ThemeManager
        backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        textColor = ThemeManager.getInstance().getTextColor();
        accentColor = ThemeManager.getInstance().getAccentColor();

        // Apply initial theme
        applyTheme(backgroundColor, textColor, accentColor);
    }

    @Override
    public void applyTheme(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        setBackground(backgroundColor);

        // Apply to all child components
        for (Component component : getComponents()) {
            if (component instanceof Themeable) {
                ((Themeable) component).applyTheme(backgroundColor, textColor, accentColor);
            } else {
                applyThemeToComponent(component, backgroundColor, textColor, accentColor);
            }
        }

        repaint();
    }

    private void applyThemeToComponent(Component component, Color bg, Color text, Color accent) {
        component.setBackground(bg);
        if (component instanceof JComponent) {
            ((JComponent) component).setOpaque(false);
        }

        if (component instanceof JLabel) {
            component.setForeground(text);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setForeground(text);
            GuiUtil.changeButtonIconColor(button, text);
        } else if (component instanceof JSlider) {
            JSlider slider = (JSlider) component;
            slider.setForeground(accent);
        }
        // Add more component types as needed
    }
}