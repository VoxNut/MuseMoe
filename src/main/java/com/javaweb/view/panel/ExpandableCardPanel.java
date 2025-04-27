package com.javaweb.view.panel;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeManager;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ExpandableCardPanel extends JPanel {
    private JPanel contentPanel;
    private final JButton toggleButton;
    @Getter
    private boolean expanded = false;
    @Getter
    private final String title;

    public ExpandableCardPanel(String title, String iconPath, JPanel content) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f)));

        // Header panel with title and toggle button
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout(5, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Create title with icon
        JLabel titleLabel = GuiUtil.createLabel(title, Font.BOLD, 14);
        JLabel iconLabel = new JLabel(GuiUtil.createColoredIcon(iconPath,
                ThemeManager.getInstance().getTextColor(), 18, 18));

        JPanel titleWithIcon = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titleWithIcon.add(iconLabel);
        titleWithIcon.add(titleLabel);

        // Create toggle button
        toggleButton = new JButton(GuiUtil.createColoredIcon(
                AppConstant.CHEVRON_DOWN_ICON_PATH,
                ThemeManager.getInstance().getTextColor(), 14, 14));
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);

        headerPanel.add(titleWithIcon, BorderLayout.WEST);
        headerPanel.add(toggleButton, BorderLayout.EAST);

        // Setup content panel
        this.contentPanel = content;
        this.contentPanel.setVisible(false);
        this.title = title;
        // Add components
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Add hover effect to header
        GuiUtil.addHoverEffect(headerPanel);

        // Setup click action
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExpanded();
            }
        });

        toggleButton.addActionListener(e -> toggleExpanded());
    }

    private void toggleExpanded() {
        expanded = !expanded;
        contentPanel.setVisible(expanded);

        // Rotate the chevron icon
        Icon icon = GuiUtil.createColoredIcon(
                expanded ? AppConstant.CHEVRON_UP_ICON_PATH : AppConstant.CHEVRON_DOWN_ICON_PATH,
                ThemeManager.getInstance().getTextColor(), 14, 14);
        toggleButton.setIcon(icon);

        // Request layout update
        revalidate();
        repaint();

        // Ensure the expanded section is visible by scrolling to it if needed
        if (expanded) {
            Rectangle bounds = getBounds();
            scrollRectToVisible(new Rectangle(0, 0, bounds.width, bounds.height));
        }
    }


    public void setContent(JPanel newContent) {
        // Remove the old content panel
        this.remove(contentPanel);

        // Replace with the new content
        this.contentPanel = newContent;
        this.contentPanel.setVisible(expanded);
        this.add(contentPanel, BorderLayout.CENTER);

        // Force layout update
        revalidate();
        repaint();
    }

    public void expandPanel() {
        if (!expanded) {
            toggleExpanded();
        }
    }
}
