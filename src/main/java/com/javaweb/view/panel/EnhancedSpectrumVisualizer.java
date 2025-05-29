package com.javaweb.view.panel;

import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Enhanced audio spectrum visualizer that uses CAVA (Console-based Audio Visualizer for ALSA)
 * for high-quality, system audio-level visualization
 */
@Slf4j
public class EnhancedSpectrumVisualizer extends JPanel implements ThemeChangeListener {

    // Configuration constants
    private static final int DEFAULT_NUM_BANDS = 16;
    private static final int DEFAULT_FRAMERATE = 240;

    // Visualization state
    private float[] bands;

    // CAVA process handling
    private Process cavaProcess;
    private Thread readerThread;
    private boolean running = false;
    private String configPath;

    // Display properties
    @Getter
    private int numberOfBands;
    private Color foregroundColor;
    private Color backgroundColor;
    private Color accentColor;

    // Animation properties
    private Timer animationTimer;
    private boolean demoMode = true;
    private long demoPhase = 0;
    private float[] demoData;
    private int historySize = 3;
    private float[][] barHistory;
    private int historyIndex = 0;


    public EnhancedSpectrumVisualizer() {
        this(DEFAULT_NUM_BANDS);
    }


    public EnhancedSpectrumVisualizer(int numberOfBands) {
        this.numberOfBands = numberOfBands;
        this.bands = new float[numberOfBands];
        this.demoData = new float[numberOfBands];
        this.barHistory = new float[historySize][numberOfBands];

        // Apply theme colors
        ThemeManager themeManager = ThemeManager.getInstance();
        this.foregroundColor = themeManager.getTextColor();
        this.backgroundColor = themeManager.getBackgroundColor();
        this.accentColor = themeManager.getAccentColor();
        themeManager.addThemeChangeListener(this);

        // Setup panel
        setOpaque(false);

        // Create the CAVA config file
        setupCavaConfig();

        // Timer to animate bars
        animationTimer = new Timer(1000 / DEFAULT_FRAMERATE, e -> {
            if (demoMode) {
                updateDemoVisualization();
            }
            repaint();
        });

        animationTimer.setRepeats(true);
        animationTimer.start();
    }

    /**
     * Create or update CAVA configuration file
     */
    private void setupCavaConfig() {
        try {
            String userHome = System.getProperty("user.home");
            Path configDir = Paths.get(userHome, ".config", "cava");
            Files.createDirectories(configDir);

            configPath = configDir.resolve("musemoe.conf").toString();

            String config =
                    "[general]\n" +
                            "framerate = " + DEFAULT_FRAMERATE + "\n" +
                            "bars = " + numberOfBands + "\n" +
                            "autosens = 1\n" +
                            "sensitivity = 100\n" +
                            "\n" +
                            "[output]\n" +
                            "method = raw\n" +
                            "raw_target = /dev/stdout\n" +
                            "data_format = ascii\n" +
                            "ascii_max_range = 100\n" +
                            "\n" +
                            "[smoothing]\n" +
                            "monstercat = 1.5\n" +
                            "noise_reduction = 85\n";

            try (FileWriter writer = new FileWriter(configPath)) {
                writer.write(config);
            }

            log.info("Created CAVA configuration at: {}", configPath);
        } catch (IOException e) {
            log.error("Failed to create CAVA config file: {}", e.getMessage());
        }
    }

    /**
     * Starts CAVA visualization process
     */
    public void startCava() {
        if (running) {
            return;
        }

        try {
            if (!isCavaAvailable()) {
                log.error("CAVA is not installed or not in PATH. Falling back to demo mode.");
                demoMode = true;
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "cava",
                    "-p",
                    configPath
            );
            pb.redirectErrorStream(true);
            cavaProcess = pb.start();
            running = true;

            readerThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(cavaProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && running) {
                        String[] parts = line.trim().split(";");
                        if (parts.length > 0) {
                            synchronized (bands) {
                                for (int i = 0; i < parts.length && i < bands.length; i++) {
                                    try {
                                        float value = Float.parseFloat(parts[i]) / 100.0f;
                                        bands[i] = value;

                                        barHistory[historyIndex][i] = value;
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                                historyIndex = (historyIndex + 1) % historySize;
                            }

                            demoMode = false;
                        }
                    }
                } catch (IOException e) {
                    if (running) {
                        log.error("Error reading from CAVA: {}", e.getMessage());
                        demoMode = true;
                    }
                }
            }, "CavaReaderThread");

            readerThread.setDaemon(true);
            readerThread.start();

            log.info("CAVA visualization started with {} bands", numberOfBands);
        } catch (IOException e) {
            log.error("Failed to start CAVA: {}", e.getMessage());
            demoMode = true;
            GuiUtil.showErrorMessageDialog(this,
                    "Failed to start CAVA! Falling back to demo mode.");
        }
    }

    /**
     * Check if CAVA is available on the system
     */
    private boolean isCavaAvailable() {
        try {
            Process process;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                process = Runtime.getRuntime().exec("where cava");
            } else {
                process = Runtime.getRuntime().exec("which cava");
            }
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Stops CAVA process and resources
     */
    public void stop() {
        running = false;

        if (cavaProcess != null) {
            cavaProcess.destroy();
            cavaProcess = null;
        }

        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
            readerThread = null;
        }
    }


    /**
     * Update demo visualization with wave-like animation when CAVA is not running
     */
    private void updateDemoVisualization() {
        demoPhase += 100;
        float timeSeconds = demoPhase / 1000.0f;

        float pulseRate = 0.3f;
        float pulseDepth = 0.4f;
        float pulseAmount = 1.0f + pulseDepth * (float) Math.sin(timeSeconds * pulseRate);

        for (int i = 0; i < numberOfBands; i++) {
            float baseFrequency = 0.8f + (i / (float) numberOfBands) * 1.0f;
            float baseValue = 0.35f + 0.3f * (float) Math.sin(timeSeconds * baseFrequency + i * 0.2f);

            baseValue += 0.05f * (float) Math.random();

            float value = baseValue * pulseAmount;

            if (i > 0) {
                value = value * 0.9f + demoData[i - 1] * 0.1f;
            }

            if (Math.random() < 0.03) {
                value += 0.2f * (float) Math.random();
            }

            demoData[i] = demoData[i] * 0.7f + value * 0.3f;

            demoData[i] = Math.max(0.05f, Math.min(0.95f, demoData[i]));
        }

        smoothBarValues();

        System.arraycopy(demoData, 0, bands, 0, numberOfBands);
    }

    /**
     * Apply smoothing to create more natural transitions between adjacent bars
     */
    private void smoothBarValues() {
        float[] smoothed = new float[numberOfBands];

        for (int i = 0; i < numberOfBands; i++) {
            float sum = 0;
            float weight = 0;

            for (int j = Math.max(0, i - 2); j <= Math.min(numberOfBands - 1, i + 2); j++) {
                float neighborWeight = 1.0f - Math.abs(i - j) / 3.0f;
                sum += demoData[j] * neighborWeight;
                weight += neighborWeight;
            }

            smoothed[i] = sum / weight;
        }

        System.arraycopy(smoothed, 0, demoData, 0, numberOfBands);
    }

    /**
     * Draw the visualizer
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Thiết lập để vẽ chất lượng cao
        GuiUtil.configureGraphicsForHighQuality(g2d);

        // Tính toán kích thước layout
        int width = getWidth();
        int height = getHeight();

        // Đảm bảo các thanh và khoảng cách luôn lấp đầy chiều ngang của component
        int maxBarWidth = 50;       // Chiều rộng tối đa cho mỗi thanh
        int minBarWidth = 3;        // Chiều rộng tối thiểu cho mỗi thanh
        int minBarSpacing = 1;      // Khoảng cách tối thiểu giữa các thanh
        int maxTotalWidth = width - 20;  // Để lại lề trái phải 10px

        // Tính toán barWidth và barSpacing để lấp đầy chiều ngang
        int optimalBarWidth = Math.max(minBarWidth,
                Math.min(maxBarWidth,
                        (maxTotalWidth - (numberOfBands - 1) * minBarSpacing) / numberOfBands));

        int optimalBarSpacing;
        if (numberOfBands > 1) {
            int remainingSpace = maxTotalWidth - (optimalBarWidth * numberOfBands);
            optimalBarSpacing = Math.max(minBarSpacing, remainingSpace / (numberOfBands - 1));
        } else {
            optimalBarSpacing = minBarSpacing;
        }

        // Tính lại tổng chiều rộng thực tế
        int totalBarWidth = optimalBarWidth * numberOfBands + optimalBarSpacing * (numberOfBands - 1);
        int startX = (width - totalBarWidth) / 2;
        int startY = height - 10; // Padding từ dưới lên

        // Vẽ mỗi thanh
        g2d.setColor(foregroundColor);

        for (int i = 0; i < numberOfBands; i++) {
            int x = startX + i * (optimalBarWidth + optimalBarSpacing);

            // Tính chiều cao thanh với giới hạn tối thiểu và tối đa
            float value = bands[i]; // Use bands array directly
            int minBarHeight = 2;
            int maxBarHeight = (int) (height * 0.85); // 85% chiều cao component

            int barHeight = (int) (value * maxBarHeight);

            // Đảm bảo thanh có chiều cao tối thiểu nếu có giá trị > 0
            if (barHeight < minBarHeight && value > 0.001f) {
                barHeight = minBarHeight;
            }

            // Vẽ thanh nếu có chiều cao
            if (barHeight > 0) {
                // Tạo gradient cho thanh để nhìn đẹp hơn
                Paint originalPaint = g2d.getPaint();

                GradientPaint gradient = new GradientPaint(
                        x, startY - barHeight, foregroundColor,
                        x, startY, foregroundColor.darker()
                );

                g2d.setPaint(gradient);

                // Vẽ thanh với góc bo tròn
                g2d.fillRoundRect(x, startY - barHeight, optimalBarWidth, barHeight,
                        Math.min(optimalBarWidth / 2, 4), Math.min(optimalBarWidth / 2, 4));

                g2d.setPaint(originalPaint);
            }
        }

        // Hiển thị chỉ báo chế độ demo
        if (demoMode) {
            g2d.setColor(foregroundColor);
            g2d.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
            g2d.drawString("DEMO MODE", 10, 20);

            // Hiển thị thông tin về số lượng thanh và kích thước
            g2d.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 12));
            g2d.setColor(foregroundColor);
            g2d.drawString("Bands: " + numberOfBands + " | Width: " + optimalBarWidth + "px", 10, 40);
        } else {
            g2d.setColor(foregroundColor);
            g2d.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
            g2d.drawString("Audio Visualizer", 10, 20);

            // Hiển thị thông tin về số lượng thanh và kích thước
            g2d.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 12));
            g2d.setColor(foregroundColor);
            g2d.drawString("Bands: " + numberOfBands + " | Width: " + optimalBarWidth + "px", 10, 40);
        }

        g2d.dispose();
    }


    /**
     * Change number of frequency bands
     */
    public void setNumberOfBands(int newNumberOfBands) {
        if (newNumberOfBands < 2) {
            newNumberOfBands = 2;
        } else if (newNumberOfBands > 256) {
            newNumberOfBands = 256;
        }

        if (this.numberOfBands == newNumberOfBands) {
            return;
        }

        this.numberOfBands = newNumberOfBands;
        this.bands = new float[newNumberOfBands];
        this.demoData = new float[newNumberOfBands];
        this.barHistory = new float[historySize][newNumberOfBands];

        boolean wasRunning = running;
        stop();
        setupCavaConfig();

        if (wasRunning) {
            startCava();
        }

        repaint();
    }


    public void toggleCAVA(boolean enable) {
        if (enable && !running) {
            startCava();
        } else if (!enable && running) {
            stop();
            demoMode = true;
        }
    }


    /**
     * Handle theme changes
     */
    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.foregroundColor = textColor;
        this.backgroundColor = backgroundColor;
        this.accentColor = accentColor;
        repaint();
    }

    /**
     * Clean up resources when visualizer is no longer needed
     */
    public void dispose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
            animationTimer = null;
        }

        stop();

        ThemeManager.getInstance().removeThemeChangeListener(this);
    }


    public boolean isRunning() {
        return running && !demoMode;
    }
}