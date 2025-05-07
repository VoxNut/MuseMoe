package com.javaweb.view.components;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AsyncImageLabel extends JLabel {
    private boolean isLoading = false;
    private Timer animationTimer;
    private int loadingAngle = 0;
    private static final int ANIMATION_SPEED = 10;
    private static final int ANIMATION_STEP = 10;

    private final int width;
    private final int height;
    private final int cornerRadius;
    private boolean isRounded;
    private boolean isCircular;

    public AsyncImageLabel(int width, int height) {
        this(width, height, 0, false);
    }

    public AsyncImageLabel(int width, int height, int cornerRadius) {
        this(width, height, cornerRadius, false);
    }

    public AsyncImageLabel(int width, int height, int cornerRadius, boolean isCircular) {
        this.width = width;
        this.height = height;
        this.cornerRadius = cornerRadius;
        this.isRounded = cornerRadius > 0;
        this.isCircular = isCircular;

        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));

        animationTimer = new Timer(ANIMATION_SPEED, e -> {
            loadingAngle = (loadingAngle + ANIMATION_STEP) % 360;
            repaint();
        });
    }

    public void startLoading() {
        isLoading = true;
        setIcon(null);
        animationTimer.start();
    }

    public void setLoadedImage(BufferedImage image) {
        isLoading = false;
        animationTimer.stop();

        if (image != null) {
            if (isCircular) {
                setIcon(new ImageIcon(GuiUtil.createSmoothCircularAvatar(image, width)));
            } else if (isRounded) {
                setIcon(GuiUtil.createRoundedCornerImageIcon(image, cornerRadius, width, height));
            } else {
                setIcon(new ImageIcon(GuiUtil.createBufferImage(image, width, height)));
            }
        } else {
            setIcon(GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, width, height));
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isLoading) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(width, height) / 4;

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(2));

            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, loadingAngle, 270);
            g2d.dispose();
        }
    }

    public void cleanup() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationTimer = null;
    }
}