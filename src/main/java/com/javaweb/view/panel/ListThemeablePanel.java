package com.javaweb.view.panel;

import com.javaweb.utils.GuiUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Extends ThemeablePanel with consistent styling for lists and buttons
 */
public abstract class ListThemeablePanel extends ThemeablePanel {

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        super.onThemeChanged(backgroundColor, textColor, accentColor);
        // Apply styling to all lists and buttons in this panel
        applyStylesToLists(this);
        applyStylesToButtons(this);
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    /**
     * Recursively applies theme colors to all JLists
     */
    protected void applyStylesToLists(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JList<?> list) {
                list.setBackground(backgroundColor);
                list.setForeground(textColor);
                list.setSelectionBackground(accentColor);
                list.setSelectionForeground(
                        GuiUtil.calculateContrast(accentColor, textColor) > 4.5 ? textColor : backgroundColor);
            }

            if (c instanceof Container) {
                applyStylesToLists((Container) c);
            }
        }
    }

    /**
     * Recursively applies theme colors to all JButtons
     */
    protected void applyStylesToButtons(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton button) {
                GuiUtil.styleButton(button, backgroundColor, textColor, accentColor);
            }

            if (c instanceof Container) {
                applyStylesToButtons((Container) c);
            }
        }
    }
}