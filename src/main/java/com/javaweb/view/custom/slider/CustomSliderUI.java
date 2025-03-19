package com.javaweb.view.custom.slider;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;


public class CustomSliderUI extends BasicSliderUI {
    private Color trackColor;
    private Color thumbColor;

    public CustomSliderUI(JSlider slider, Color trackColor, Color thumbColor) {
        super(slider);
        this.trackColor = trackColor;
        this.thumbColor = thumbColor;
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Custom thumb shape (circle)
        int thumbRadius = 8;
        int x = thumbRect.x + thumbRect.width / 2 - thumbRadius;
        int y = thumbRect.y + thumbRect.height / 2 - thumbRadius;
        g2d.setColor(thumbColor); // Custom thumb color
        g2d.fillOval(x, y, thumbRadius * 2, thumbRadius * 2);

        g2d.dispose();
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Custom track painting
        int trackHeight = 8;
        int trackY = trackRect.y + (trackRect.height - trackHeight) / 2;
        g2d.setColor(trackColor); // Custom track color
        g2d.fillRoundRect(trackRect.x, trackY, trackRect.width, trackHeight, trackHeight, trackHeight);

        // Highlight the filled portion of the track
        int fillWidth = thumbRect.x + thumbRect.width / 2 - trackRect.x;
        g2d.setColor(trackColor.darker()); // Custom filled track color
        g2d.fillRoundRect(trackRect.x, trackY, fillWidth, trackHeight, trackHeight, trackHeight);

        g2d.dispose();
    }

    @Override
    public void paintLabels(Graphics g) {
        super.paintLabels(g);
        // Custom label painting if needed
    }
}