package test.SwingTest;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.advanced.AdvancedPlayer;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpectrumVisualizer extends JFrame {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHUNK_SIZE = 1024; // Giảm để tăng tốc FFT
    private static final int BARS = 32;
    private final FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
    private ExecutorService audioProcessingExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AudioProcessingThread");
        t.setPriority(Thread.MAX_PRIORITY - 1);
        t.setDaemon(true);
        return t;
    });
    // Các hệ số điều chỉnh
    private static final double SMOOTH_FACTOR = 0.3; // Tăng để phản ứng nhanh hơn
    private static final double DECAY_FACTOR = 0.99; // Giảm tốc độ giảm đỉnh
    private static final int FRAME_RATE = 60;
    private static final double AMPLITUDE_SCALE = 4.0;
    private static final int OVERLAP_RATIO = 4; // Tăng overlap để mượt hơn
    private static final int PEAK_HOLD_TIME = 30;
    private static final double NORMALIZATION_FACTOR = 0.7;
    private static final double AVERAGING_ALPHA = 0.03; // Tăng để phản ánh dữ liệu mới nhanh hơn
    private final AtomicBoolean fftInProgress = new AtomicBoolean(false);
    private final long MIN_FFT_INTERVAL_NS = 16_000_000; // ~60fps
    private long lastFFTTimeNs = 0;
    // Peak detection
    private final double[] peakAmplitudes = new double[BARS];
    private final int[] peakHoldFrames = new int[BARS];

    // Buffer và kết quả
    private double[] amplitudes = new double[BARS];
    private double[] targetAmplitudes = new double[BARS];
    private double[] pcmBuffer = new double[CHUNK_SIZE * 8]; // Tăng buffer
    private int pcmBufferIndex = 0;

    // Giá trị tối đa
    private final double[] maxValues = new double[BARS];
    private final double[] averageAmplitudes = new double[BARS];

    // Queue cho khung FFT
    private final Queue<double[]> recentFrames = new ArrayDeque<>();
    private static final int MAX_FRAMES = 8;

    // Phân chia dải tần số
    private static final int BASS_END_PERCENT = 15;
    private static final int MID_END_PERCENT = 70;
    private final double[] frequencyWeights = new double[BARS];

    // Animation timer
    private Timer uiUpdateTimer;

    public SpectrumVisualizer() {
        setTitle("Enhanced Spectrum Visualizer");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Khởi tạo trọng số tần số
        initFrequencyWeights();

        // Khởi tạo max values
        Arrays.fill(maxValues, 0.01);
        Arrays.fill(averageAmplitudes, 0.01);

        VisualizerPanel panel = new VisualizerPanel();
        add(panel);

        // Timer 60 FPS
        uiUpdateTimer = new Timer(1000 / FRAME_RATE, e -> {
            updateAnimationFrame();
            panel.repaint();
        });
        uiUpdateTimer.start();

        String filePath = "src/main/java/com/javaweb/view/mini_musicplayer/audio/mesmerizer.mp3";
        startFilePlayback(filePath);
    }

    private void initFrequencyWeights() {
        for (int i = 0; i < BARS; i++) {
            double normalizedPos = (double) i / BARS;
            if (normalizedPos < BASS_END_PERCENT / 100.0) {
                frequencyWeights[i] = 0.4 - normalizedPos * 0.3;
            } else if (normalizedPos < MID_END_PERCENT / 100.0) {
                double midPos = (normalizedPos - BASS_END_PERCENT / 100.0) / ((MID_END_PERCENT - BASS_END_PERCENT) / 100.0);
                frequencyWeights[i] = 1.2 + midPos;
            } else {
                double treblePos = (normalizedPos - MID_END_PERCENT / 100.0) / ((100 - MID_END_PERCENT) / 100.0);
                frequencyWeights[i] = 1.8 + treblePos * 1.2;
            }
        }
    }

    private void updateAnimationFrame() {
        for (int i = 0; i < BARS; i++) {
            amplitudes[i] += (targetAmplitudes[i] - amplitudes[i]) * SMOOTH_FACTOR;
            if (i < BARS * MID_END_PERCENT / 100) {
                amplitudes[i] *= 1.0 + Math.random() * 0.05 - 0.025;
            }
            amplitudes[i] = Math.max(0.0, Math.min(amplitudes[i], 2.0));

            if (amplitudes[i] > peakAmplitudes[i]) {
                peakAmplitudes[i] = amplitudes[i];
                peakHoldFrames[i] = PEAK_HOLD_TIME;
            } else {
                if (peakHoldFrames[i] > 0) {
                    peakHoldFrames[i]--;
                } else {
                    peakAmplitudes[i] *= DECAY_FACTOR;
                }
            }
        }
    }

    private void startFilePlayback(String filePath) {
        new Thread(() -> {
            try {
                FileInputStream fileInputStreamPlayer = new FileInputStream(filePath);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStreamPlayer);
                AdvancedPlayer player = new AdvancedPlayer(bufferedInputStream);

                FileInputStream fileInputStreamDecoder = new FileInputStream(filePath);
                BufferedInputStream decoderInput = new BufferedInputStream(fileInputStreamDecoder, 8192);
                Bitstream bitstream = new Bitstream(decoderInput);
                Decoder decoder = new Decoder();
                FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

                new Thread(() -> {
                    try {
                        System.out.println("Bắt đầu phát nhạc: " + filePath);
                        player.play();
                        System.out.println("Kết thúc phát nhạc");
                    } catch (javazoom.jl.decoder.JavaLayerException e) {
                        e.printStackTrace();
                    }
                }).start();

                double[] hannWindow = createHannWindow(CHUNK_SIZE);
                long lastFFTTime = System.nanoTime();

                while (true) {
                    Header header = bitstream.readFrame();
                    if (header == null) break;

                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                    short[] pcm = output.getBuffer();
                    int sampleCount = output.getBufferLength();

                    for (int i = 0; i < sampleCount && pcmBufferIndex < pcmBuffer.length; i++) {
                        pcmBuffer[pcmBufferIndex++] = pcm[i] / 32768.0;
                    }

                    while (pcmBufferIndex >= CHUNK_SIZE) {
                        long currentTime = System.nanoTime();
                        if (currentTime - lastFFTTime >= 16_666_666) { // 16,67ms
                            double[] audioData = new double[CHUNK_SIZE];
                            System.arraycopy(pcmBuffer, 0, audioData, 0, CHUNK_SIZE);

                            applyWindow(audioData, hannWindow);
                            int advanceSize = CHUNK_SIZE / OVERLAP_RATIO;
                            System.arraycopy(pcmBuffer, advanceSize, pcmBuffer, 0, pcmBufferIndex - advanceSize);
                            pcmBufferIndex -= advanceSize;

                            long startTime = System.nanoTime();
                            Complex[] fftResult = fft.transform(audioData, TransformType.FORWARD);
                            System.out.println("FFT time: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
                            double[] magnitudes = new double[CHUNK_SIZE / 2];
                            for (int i = 0; i < CHUNK_SIZE / 2; i++) {
                                double re = fftResult[i].getReal();
                                double im = fftResult[i].getImaginary();
                                magnitudes[i] = Math.log10(1 + Math.sqrt(re * re + im * im) * 1000) / 3.0;
                            }

                            double[] bandMagnitudes = calculateLogBands(magnitudes);
                            applyDynamicRangeCompression(bandMagnitudes);
                            recentFrames.add(bandMagnitudes);
                            if (recentFrames.size() > MAX_FRAMES) {
                                recentFrames.poll();
                            }

                            double[] avgMagnitudes = averageRecentFrames();
                            for (int i = 0; i < BARS; i++) {
                                if (avgMagnitudes[i] > maxValues[i]) {
                                    maxValues[i] = avgMagnitudes[i] * 0.95 + maxValues[i] * 0.05;
                                } else {
                                    maxValues[i] *= 0.999;
                                }
                                averageAmplitudes[i] = averageAmplitudes[i] * (1 - AVERAGING_ALPHA) +
                                        avgMagnitudes[i] * AVERAGING_ALPHA;
                                double scaleFactor = maxValues[i] > 0.01 ?
                                        NORMALIZATION_FACTOR / (0.8 * maxValues[i] + 0.2 * averageAmplitudes[i]) :
                                        AMPLITUDE_SCALE;
                                targetAmplitudes[i] = avgMagnitudes[i] * scaleFactor;
                            }

                            lastFFTTime = currentTime;
                        }
                    }

                    bitstream.closeFrame();
                }

                bitstream.close();
                fileInputStreamDecoder.close();
                player.close();
                fileInputStreamPlayer.close();
            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý file MP3: " + e.getMessage());
                e.printStackTrace();
            }
        }, "AudioProcessThread").start();
    }

    private void applyDynamicRangeCompression(double[] bands) {
        double bassThreshold = 0.15;
        double midThreshold = 0.3;
        double trebleThreshold = 0.7;
        double bassRatio = 0.3;
        double midRatio = 0.6;
        double trebleRatio = 0.9;

        for (int i = 0; i < BARS; i++) {
            double threshold, ratio;
            if (i < BARS * BASS_END_PERCENT / 100) {
                threshold = bassThreshold;
                ratio = bassRatio;
            } else if (i < BARS * MID_END_PERCENT / 100) {
                threshold = midThreshold;
                ratio = midRatio;
            } else {
                threshold = trebleThreshold;
                ratio = trebleRatio;
            }
            if (bands[i] > threshold) {
                bands[i] = threshold + (bands[i] - threshold) * ratio;
            }
        }
    }

    private double[] createHannWindow(int size) {
        double[] window = new double[size];
        for (int i = 0; i < size; i++) {
            window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1)));
        }
        return window;
    }

    private void applyWindow(double[] audio, double[] window) {
        for (int i = 0; i < audio.length; i++) {
            audio[i] *= window[i];
        }
    }

    private double[] calculateLogBands(double[] magnitudes) {
        double[] result = new double[BARS];
        double minFreq = 20.0;
        double bassUpperFreq = 250.0;
        double midUpperFreq = 2000.0;
        double maxFreq = SAMPLE_RATE / 2.5;

        for (int i = 0; i < BARS; i++) {
            double normalizedPos = (double) i / BARS;
            double freq;
            if (normalizedPos < BASS_END_PERCENT / 100.0) {
                double bassPos = normalizedPos / (BASS_END_PERCENT / 100.0);
                freq = minFreq + bassPos * (bassUpperFreq - minFreq);
            } else if (normalizedPos < MID_END_PERCENT / 100.0) {
                double midPos = (normalizedPos - BASS_END_PERCENT / 100.0) / ((MID_END_PERCENT - BASS_END_PERCENT) / 100.0);
                freq = bassUpperFreq + midPos * (midUpperFreq - bassUpperFreq);
            } else {
                double treblePos = (normalizedPos - MID_END_PERCENT / 100.0) / ((100 - MID_END_PERCENT) / 100.0);
                freq = midUpperFreq * Math.pow(maxFreq / midUpperFreq, treblePos);
            }

            int binLow = i == 0 ? 1 : (int) ((i - 1) * CHUNK_SIZE / SAMPLE_RATE * minFreq);
            int binHigh = Math.min(magnitudes.length - 1, (int) (freq * CHUNK_SIZE / SAMPLE_RATE));

            double sum = 0;
            double weight = 0;
            for (int bin = binLow; bin <= binHigh; bin++) {
                double binWeight = 1.0 - 0.5 * Math.abs((bin - (binLow + binHigh) / 2.0) / ((binHigh - binLow) / 2.0));
                sum += magnitudes[bin] * binWeight;
                weight += binWeight;
            }
            if (weight > 0) {
                result[i] = sum / weight * frequencyWeights[i];
            }
        }
        return result;
    }

    private double[] averageRecentFrames() {
        double[] result = new double[BARS];
        if (recentFrames.isEmpty()) return result;

        double totalWeight = 0;
        int frameNumber = 0;
        for (double[] frame : recentFrames) {
            frameNumber++;
            double weight = frameNumber / (double) recentFrames.size();
            for (int i = 0; i < BARS; i++) {
                result[i] += frame[i] * weight;
            }
            totalWeight += weight;
        }
        if (totalWeight > 0) {
            for (int i = 0; i < BARS; i++) {
                result[i] /= totalWeight;
            }
        }
        return result;
    }

    class VisualizerPanel extends JPanel {
        private final Color[] barColors = new Color[BARS];
        private final GradientPaint[] barGradients = new GradientPaint[BARS];
        private final Color bassColor = new Color(255, 50, 50);
        private final Color midColor = new Color(50, 255, 50);
        private final Color trebleColor = new Color(50, 50, 255);
        private final int BASS_BARS = BARS * BASS_END_PERCENT / 100;
        private final int MID_BARS = BARS * (MID_END_PERCENT - BASS_END_PERCENT) / 100;
        private final int TREBLE_BARS = BARS - BASS_BARS - MID_BARS;

        public VisualizerPanel() {
            setBackground(new Color(10, 10, 20));
            for (int i = 0; i < BARS; i++) {
                if (i < BASS_BARS) {
                    float factor = (float) i / BASS_BARS;
                    barColors[i] = blendColors(bassColor, midColor, factor * 0.3f);
                } else if (i < BASS_BARS + MID_BARS) {
                    float factor = (float) (i - BASS_BARS) / MID_BARS;
                    barColors[i] = blendColors(midColor, trebleColor, factor);
                } else {
                    float factor = (float) (i - BASS_BARS - MID_BARS) / TREBLE_BARS;
                    barColors[i] = blendColors(trebleColor, new Color(200, 150, 255), factor);
                }
                Color baseColor = barColors[i];
                Color topColor = brightenColor(baseColor, 0.3f);
                barGradients[i] = new GradientPaint(
                        0, 0, darkenColor(baseColor, 0.7f),
                        0, -100, topColor
                );
            }
        }

        private Color blendColors(Color c1, Color c2, float factor) {
            float ifactor = 1.0f - factor;
            int r = (int) (c1.getRed() * ifactor + c2.getRed() * factor);
            int g = (int) (c1.getGreen() * ifactor + c2.getGreen() * factor);
            int b = (int) (c1.getBlue() * ifactor + c2.getBlue() * factor);
            return new Color(r, g, b);
        }

        long lastTime = System.nanoTime();
        int frameCount = 0;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            long currentTime = System.nanoTime();
            if (currentTime - lastTime >= 1_000_000_000) {
                System.out.println("FPS: " + frameCount);
                frameCount = 0;
                lastTime = currentTime;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int width = getWidth();
            int height = getHeight();
            int barWidth = Math.max(4, width / BARS - 1);
            int spacing = 1;
            int startX = (width - (barWidth + spacing) * BARS) / 2;
            int startY = height - 40;

            drawFrequencyBands(g2d, startX, startY, barWidth, spacing, height);
            g2d.setColor(new Color(30, 30, 40));
            for (int y = startY; y > 40; y -= 40) {
                g2d.drawLine(0, y, width, y);
            }

            for (int i = 0; i < BARS; i++) {
                int x = startX + i * (barWidth + spacing);
                int barHeight = (int) (Math.pow(amplitudes[i], 0.9) * startY);
                if (barHeight < 2 && amplitudes[i] > 0.01) barHeight = 2;
                int peakHeight = (int) (Math.pow(peakAmplitudes[i], 0.9) * startY);

                if (barHeight > 0) {
                    g2d.setPaint(barGradients[i]);
                    g2d.fillRoundRect(x, startY - barHeight, barWidth, barHeight, 6, 6);
                }

                if (peakHeight > 0) {
                    Color peakColor = brightenColor(barColors[i], 0.3f);
                    g2d.setColor(new Color(peakColor.getRed(), peakColor.getGreen(), peakColor.getBlue(), 60));
                    g2d.fillRect(x - 1, startY - peakHeight - 3, barWidth + 2, 6);
                    g2d.setColor(peakColor);
                    g2d.fillRect(x, startY - peakHeight - 1, barWidth, 2);
                }
            }

            drawFrequencyLabels(g2d, startX, height, barWidth, spacing);
            drawBandNames(g2d, startX, barWidth, spacing);
        }

        private void drawFrequencyBands(Graphics2D g2d, int startX, int startY, int barWidth, int spacing, int height) {
            int bassWidth = BASS_BARS * (barWidth + spacing);
            int midWidth = MID_BARS * (barWidth + spacing);
            g2d.setColor(new Color(bassColor.getRed(), bassColor.getGreen(), bassColor.getBlue(), 15));
            g2d.fillRect(startX, 0, bassWidth, height);
            g2d.setColor(new Color(midColor.getRed(), midColor.getGreen(), midColor.getBlue(), 15));
            g2d.fillRect(startX + bassWidth, 0, midWidth, height);
            g2d.setColor(new Color(trebleColor.getRed(), trebleColor.getGreen(), trebleColor.getBlue(), 15));
            g2d.fillRect(startX + bassWidth + midWidth, 0, (bassWidth + midWidth) - startX, height);
            g2d.setColor(new Color(255, 255, 255, 30));
            g2d.drawLine(startX + bassWidth, 40, startX + bassWidth, startY);
            g2d.drawLine(startX + bassWidth + midWidth, 40, startX + bassWidth + midWidth, startY);
        }

        private void drawFrequencyLabels(Graphics2D g2d, int startX, int height, int barWidth, int spacing) {
            g2d.setColor(new Color(180, 180, 200));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2d.drawString("20Hz", startX, height - 10);
            g2d.drawString("100Hz", startX + (BASS_BARS / 2) * (barWidth + spacing), height - 10);
            g2d.drawString("250Hz", startX + BASS_BARS * (barWidth + spacing), height - 10);
            g2d.drawString("1kHz", startX + (BASS_BARS + MID_BARS / 2) * (barWidth + spacing), height - 10);
            g2d.drawString("5kHz", startX + (BASS_BARS + MID_BARS) * (barWidth + spacing), height - 10);
            g2d.drawString("20kHz", startX + (BARS - 1) * (barWidth + spacing), height - 10);
        }

        private void drawBandNames(Graphics2D g2d, int startX, int barWidth, int spacing) {
            Font bandFont = new Font("SansSerif", Font.BOLD, 14);
            g2d.setFont(bandFont);
            int bassCenterX = startX + (BASS_BARS * (barWidth + spacing)) / 2;
            int midCenterX = startX + BASS_BARS * (barWidth + spacing) + (MID_BARS * (barWidth + spacing)) / 2;
            int trebleCenterX = startX + (BASS_BARS + MID_BARS) * (barWidth + spacing) +
                    (TREBLE_BARS * (barWidth + spacing)) / 2;
            drawTextWithShadow(g2d, "BASS", bassCenterX, 25, bassColor);
            drawTextWithShadow(g2d, "MID", midCenterX, 25, midColor);
            drawTextWithShadow(g2d, "TREBLE", trebleCenterX, 25, trebleColor);
        }

        private void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Color color) {
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(text, x - textWidth / 2 + 1, y + 1);
            g2d.setColor(color);
            g2d.drawString(text, x - textWidth / 2, y);
        }

        private Color brightenColor(Color c, float factor) {
            int r = Math.min(255, (int) (c.getRed() * (1 + factor)));
            int g = Math.min(255, (int) (c.getGreen() * (1 + factor)));
            int b = Math.min(255, (int) (c.getBlue() * (1 + factor)));
            return new Color(r, g, b);
        }

        private Color darkenColor(Color c, float factor) {
            int r = (int) (c.getRed() * factor);
            int g = (int) (c.getGreen() * factor);
            int b = (int) (c.getBlue() * factor);
            return new Color(r, g, b);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new SpectrumVisualizer().setVisible(true);
            } catch (Exception e) {
                System.err.println("Lỗi khi khởi tạo: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}