package com.javaweb.view.custom.button;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class CustomButtonUI extends BasicButtonUI {
    private Color backgroundColor;
    private Color textColor;
    private Color disabledBackgroundColor;
    private Color disabledTextColor;
    private Color hoverBackgroundColor;
    private Color hoverTextColor;

    public CustomButtonUI(Color backgroundColor, Color textColor, Color disabledBackgroundColor, Color disabledTextColor, Color hoverBackgroundColor, Color hoverTextColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.disabledBackgroundColor = disabledBackgroundColor;
        this.disabledTextColor = disabledTextColor;
        this.hoverBackgroundColor = hoverBackgroundColor;
        this.hoverTextColor = hoverTextColor;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton) c;
        if (button.isEnabled()) {
            if (button.getModel().isRollover()) {
                button.setBackground(hoverBackgroundColor);
                button.setForeground(hoverTextColor);
            } else {
                button.setBackground(backgroundColor);
                button.setForeground(textColor);
            }
        } else {
            button.setBackground(disabledBackgroundColor);
            button.setForeground(disabledTextColor);
        }
        super.paint(g, c);
    }

    @Override
    public void update(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton) c;
        if (button.isEnabled()) {
            if (button.getModel().isRollover()) {
                button.setBackground(hoverBackgroundColor);
                button.setForeground(hoverTextColor);
            } else {
                button.setBackground(backgroundColor);
                button.setForeground(textColor);
            }
        } else {
            button.setBackground(disabledBackgroundColor);
            button.setForeground(disabledTextColor);
        }
        super.update(g, c);
    }
}