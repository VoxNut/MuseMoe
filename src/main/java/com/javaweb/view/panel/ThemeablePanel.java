package com.javaweb.view.panel;

import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;

/**
 * Base panel class that automatically applies theme changes to all components,
 * including consistent scrollbar styling
 */
public abstract class ThemeablePanel extends JPanel implements ThemeChangeListener {
    protected Color backgroundColor;
    protected Color textColor;
    protected Color accentColor;

    public ThemeablePanel() {
        // Get initial colors from ThemeManager
        backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        textColor = ThemeManager.getInstance().getTextColor();
        accentColor = ThemeManager.getInstance().getAccentColor();

        // Register for theme changes
        ThemeManager.getInstance().addThemeChangeListener(this);

        // Add hierarchy listener to clean up when panel is removed
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
                if (getParent() == null) {
                    cleanup();
                }
            }
        });
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        // Apply background color to this panel
        setBackground(backgroundColor);
        // Apply scrollbar style to any scroll panes
        applyThemeToComponents(this);
    }

    /**
     * Recursively applies theme colors to all components
     */
    protected void applyThemeToComponents(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JScrollPane scrollPane) {
                // Apply modern scrollbar with current theme colors
                GuiUtil.applyModernScrollBar(scrollPane);

                // Also theme the viewport's background
                scrollPane.getViewport().setBackground(backgroundColor);

                // Force update
                SwingUtilities.invokeLater(() -> {
                    scrollPane.revalidate();
                    scrollPane.repaint();
                });
            }
            if (c instanceof Container) {
                applyThemeToComponents((Container) c);
            }
        }
    }

    /**
     * Cleanup when panel is no longer needed
     */
    public void cleanup() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}