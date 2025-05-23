package com.javaweb.view.panel;

import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class InstructionPanel extends JPanel implements ThemeChangeListener {

    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    public InstructionPanel() {
        // Initialize from ThemeManager
        backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        textColor = ThemeManager.getInstance().getTextColor();
        accentColor = ThemeManager.getInstance().getAccentColor();

        initialize();
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void initialize() {
        setOpaque(false);
        setLayout(new MigLayout("fillx, insets 10", "[grow]", "[]10[]10[]"));

        JLabel titleLabel = GuiUtil.createLabel("Keyboard Shortcuts", Font.BOLD, 18);

        String[][] shortcuts = {
                {"Shift + V", "Toggle audio visualizer"},
                {"Shift + C", "Toggle commit history view"},
                {"B", "Change visualizer bands (when active)"},
                {"H", "Toggle Home"},
                {"K", "Toggle search field"},
                {"Q", "Toggle Queue panel to see what's currently is in queue"},
                {"E", "Open Miniplayer"},
                {"?", "Toggle help panel"},
                {"RMB", "You can right click on a song to see a popup menu with varies options"},
                {"LMB", "You can left click on an album, playlist, artist to see its details"},
                {"Right Arrow, F1", "Go forward"},
                {"Left Arrow, ESC", "Go backward"}
        };

        JPanel shortcutsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 2, insets 10", "[][grow]", ""));

        for (String[] shortcut : shortcuts) {
            JLabel keyLabel = GuiUtil.createLabel(shortcut[0], Font.BOLD, 16);
            JLabel descLabel = GuiUtil.createLabel(shortcut[1], Font.PLAIN, 16);

            shortcutsPanel.add(keyLabel, "");
            shortcutsPanel.add(descLabel, "growx");
        }

        add(titleLabel, "growx, wrap");
        add(shortcutsPanel, "growx, wrap");
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);

    }

}
