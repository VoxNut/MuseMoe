package test.SwingTest;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CavaVisualizerPanel extends JPanel {
    private float[] bars = new float[16]; // vì bạn để bars=16 trong config
    private Process cavaProcess;
    private Thread readerThread;
    private boolean running = false;

    public CavaVisualizerPanel() {
        setBackground(Color.BLACK);
        startCava();
    }

    private void startCava() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "cava",
                    "-p",
                    "C:\\Users\\HI\\.config\\cava\\swing.conf"
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
                        synchronized (bars) {
                            for (int i = 0; i < parts.length && i < bars.length; i++) {
                                try {
                                    bars[i] = Float.parseFloat(parts[i]) / 100.0f;
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                        repaint();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "CavaReaderThread");

            readerThread.setDaemon(true);
            readerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to start cava! Check if cava is installed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (bars) {
            Graphics2D g2d = (Graphics2D) g.create();
            int width = getWidth();
            int height = getHeight();
            int barWidth = Math.max(2, width / bars.length);
            int spacing = 2;

            for (int i = 0; i < bars.length; i++) {
                float value = bars[i];
                int barHeight = (int) (value * height);
                int x = i * (barWidth + spacing);
                int y = height - barHeight;

                GradientPaint gradient = new GradientPaint(
                        x, y, Color.CYAN,
                        x, y + barHeight, Color.BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(x, y, barWidth, barHeight, 4, 4);
            }
            g2d.dispose();
        }
    }

    public void stop() {
        running = false;
        if (cavaProcess != null) {
            cavaProcess.destroy();
        }
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
    }
}
