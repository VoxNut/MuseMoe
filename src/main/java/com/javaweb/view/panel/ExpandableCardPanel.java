package com.javaweb.view.panel;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ExpandableCardPanel extends JPanel {
    private final JPanel contentPanel;
    private final JButton toggleButton;
    private boolean expanded = false;

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


    public void updateColors(Color backgroundColor, Color textColor) {
        // Update the toggle button icon
        Icon chevronIcon = GuiUtil.createColoredIcon(
                expanded ? AppConstant.CHEVRON_UP_ICON_PATH : AppConstant.CHEVRON_DOWN_ICON_PATH,
                textColor, 14, 14);
        toggleButton.setIcon(chevronIcon);

        // Find and update icon and title in header
        for (Component component : ((Container) getComponent(0)).getComponents()) {
            if (component instanceof JPanel && ((JPanel) component).getLayout() instanceof FlowLayout) {
                for (Component c : ((Container) component).getComponents()) {
                    if (c instanceof JLabel label) {
                        label.setForeground(textColor);
                        if (label.getText() == null || label.getText().isEmpty()) {
                            if (label.getIcon() instanceof ImageIcon icon) {
                                GuiUtil.changeIconColor(icon, textColor);
                                label.repaint();
                            }
                        }
                    }
                }
            }
        }

        // Update border color
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GuiUtil.darkenColor(backgroundColor, 0.1f)));

        // Repaint the card
        revalidate();
        repaint();
    }
}
