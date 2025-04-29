package com.javaweb.utils;

import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;


public class ToastManager {
    private static ToastManager instance;
    private JWindow currentToast;
    private Timer currentTimer;

    // Private constructor for singleton
    private ToastManager() {
    }

    /**
     * Get the singleton instance of ToastManager
     */
    public static synchronized ToastManager getInstance() {
        if (instance == null) {
            instance = new ToastManager();
        }
        return instance;
    }


    public synchronized void showToast(Component parentComponent, String message, int durationMs) {
        // Dismiss any existing toast
        dismissCurrentToast();

        // Create the toast panel with styling
        JPanel toastPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER));
        toastPanel.setOpaque(true);
        toastPanel.setBackground(ThemeManager.getInstance().getBackgroundColor());
        toastPanel.setBorder(GuiUtil.createCompoundBorder(2));

        // Create and style the message label
        JLabel toastLabel = GuiUtil.createLabel(message);
        toastLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 16));
        toastPanel.add(toastLabel);

        // Find the parent window to anchor the toast to
        Window parentWindow = SwingUtilities.getWindowAncestor(parentComponent);
        if (parentWindow == null && parentComponent instanceof Window) {
            parentWindow = (Window) parentComponent;
        }

        // Create new undecorated window for the toast
        currentToast = new JWindow(parentWindow);
        currentToast.setContentPane(toastPanel);
        currentToast.pack();

        // Position the toast
        positionToast(parentWindow, currentToast);

        // Show the toast with a smooth fade-in effect
        fadeInToast();

        // Set up timer to auto-dismiss
        currentTimer = new Timer();
        currentTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fadeOutToast();
            }
        }, durationMs);
    }

    /**
     * Show a toast with default duration (3 seconds)
     */
    public void showToast(Component parentComponent, String message) {
        showToast(parentComponent, message, 3000);
    }

    /**
     * Position the toast window relative to the parent window
     */
    private void positionToast(Window parentWindow, JWindow toastWindow) {
        if (parentWindow != null) {
            // Position at top of parent window
            int x = parentWindow.getX() + (parentWindow.getWidth() - toastWindow.getWidth()) / 2;
            int y = parentWindow.getY() + 100; // Show 100px from top
            toastWindow.setLocation(x, y);
        } else {
            // Fallback to center of screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            toastWindow.setLocation(
                    (screenSize.width - toastWindow.getWidth()) / 2,
                    screenSize.height / 4
            );
        }
    }

    /**
     * Immediately dismiss the current toast if one exists
     */
    private synchronized void dismissCurrentToast() {
        if (currentToast != null) {
            // Cancel any existing timers
            if (currentTimer != null) {
                currentTimer.cancel();
                currentTimer = null;
            }

            // Fade out and dispose the current toast
            fadeOutToast();
        }
    }

    /**
     * Fade in the toast with animation
     */
    private void fadeInToast() {
        if (currentToast == null) return;

        // Show with initial transparency
        currentToast.setOpacity(0.0f);
        currentToast.setVisible(true);

        // Create fade-in animation
        Timer fadeInTimer = new Timer();
        fadeInTimer.scheduleAtFixedRate(new TimerTask() {
            float opacity = 0.0f;

            @Override
            public void run() {
                opacity += 0.1f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    cancel();
                }
                SwingUtilities.invokeLater(() -> {
                    if (currentToast != null) {
                        currentToast.setOpacity(opacity);
                    }
                });
            }
        }, 0, 30);
    }

    /**
     * Fade out and dispose the toast
     */
    private void fadeOutToast() {
        if (currentToast == null) return;

        JWindow toastToRemove = currentToast;
        Timer fadeOutTimer = new Timer();
        fadeOutTimer.scheduleAtFixedRate(new TimerTask() {
            float opacity = 1.0f;

            @Override
            public void run() {
                opacity -= 0.1f;
                if (opacity <= 0.0f) {
                    opacity = 0.0f;
                    SwingUtilities.invokeLater(() -> {
                        toastToRemove.dispose();
                    });
                    cancel();
                    if (currentToast == toastToRemove) {
                        currentToast = null;
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    toastToRemove.setOpacity(opacity);
                });
            }
        }, 0, 20);
    }
}