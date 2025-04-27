package com.javaweb.view.panel;

import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class EnhancedSpectrumVisualizer extends JPanel implements ThemeChangeListener {

    // Cấu hình
    private static class Config {
        static final int DEFAULT_NUM_BANDS = 16;
        static final int DEFAULT_FPS = 60;
        // Giảm giá trị GRAVITY để thanh rơi chậm hơn
        static final double GRAVITY = 0.06;
        // Giảm SMOOTH_FACTOR để thanh tăng chậm hơn
        static final float SMOOTH_FACTOR = 0.06f;

        static final int SAMPLE_RATE = 44100;
        static final int CHUNK_SIZE = 2048;
        // Tăng overlap để lấy mẫu nhiều hơn
        static final int OVERLAP_RATIO = 20;

        static final double MIN_FREQ = 50.0;
        static final double MAX_FREQ = 15000.0;
    }

    // Trạng thái hiển thị
    private float[] currentBands;
    private float[] targetBands;
    private float[] frequencyLimits;
    /**
     * -- GETTER --
     *
     * @return Số lượng thanh hiển thị hiện tại
     */
    @Getter
    private int numberOfBands;
    private float[][] bandHistory;
    private int historySize = 4; // Số frame lịch sử lưu trữ
    private int historyIndex = 0;
    // Xử lý âm thanh
    @Getter
    private AudioProcessor audioProcessor;
    private double[] hannWindow;
    private final Object lock = new Object(); // <--- THÊM DÒNG NÀY

    // Màu sắc
    private Color foregroundColor;

    // Hiển thị
    @Getter
    @Setter
    private int barWidth = 15;
    @Getter
    @Setter
    private int barSpacing = 2;

    // Biến trạng thái
    private Timer animationTimer;
    private boolean demoMode = true;
    private long demoPhase = 0;
    private float[] demoData;


    public EnhancedSpectrumVisualizer() {
        this(Config.DEFAULT_NUM_BANDS);
    }

    public EnhancedSpectrumVisualizer(int numberOfBands) {
        this.numberOfBands = numberOfBands;
        this.currentBands = new float[numberOfBands];
        this.targetBands = new float[numberOfBands];
        this.demoData = new float[numberOfBands];
        this.frequencyLimits = calculateFrequencyLimits();
        this.hannWindow = createHannWindow(Config.CHUNK_SIZE);
        this.audioProcessor = new AudioProcessor(this);
        this.bandHistory = new float[historySize][numberOfBands];

        // Khởi tạo màu sắc
        ThemeManager themeManager = ThemeManager.getInstance();
        this.foregroundColor = themeManager.getTextColor();
        themeManager.addThemeChangeListener(this);

        // Cài đặt panel
        setOpaque(false);

        // Timer để cập nhật animation
        animationTimer = new Timer(1000 / Config.DEFAULT_FPS, e -> {
            updateBands();

            // Chế độ demo nếu không có dữ liệu âm thanh
            if (demoMode) {
                updateDemoVisualization();
            }

            repaint();
        });

        animationTimer.setRepeats(true);
        animationTimer.start();
    }

    /**
     * Tính toán giới hạn tần số cho mỗi band với thang logarithmic đơn giản
     */
    private float[] calculateFrequencyLimits() {
        float[] limits = new float[numberOfBands + 1];

        // Sử dụng thang logarithmic cơ bản
        double log_low = Math.log(Config.MIN_FREQ);
        double log_high = Math.log(Config.MAX_FREQ);

        // Phân phối đều các band trên thang logarithmic
        for (int i = 0; i <= numberOfBands; i++) {
            // Vị trí chuẩn hóa từ 0 đến 1
            double normalized = (double) i / numberOfBands;

            double logValue = log_low + normalized * (log_high - log_low);

            // Chuyển đổi từ log trở lại tần số thực
            limits[i] = (float) Math.exp(logValue);
        }

        return limits;
    }

    /**
     * Kết nối với nguồn âm thanh
     */
    public void connectToAudioSource(String audioFilePath) {
        if (audioFilePath == null || audioFilePath.isEmpty()) {
            return;
        }

        // Reset visualizer
        Arrays.fill(currentBands, 0);
        Arrays.fill(targetBands, 0);

        // Start processing audio file
        File audioFile = new File(audioFilePath);
        if (audioFile.exists()) {
            log.debug("Starting processing for: {}", audioFile.getName());
            audioProcessor.startProcessing(audioFile);
            demoMode = false;
        } else {
            log.error("Audio file not found: {}", audioFilePath);
        }
    }


    /**
     * Cập nhật giá trị của các thanh với hiệu ứng mượt mà - phiên bản đơn giản
     */
    private void updateBands() {
        // Tính giá trị trung bình từ các bands liền kề
        float[] smoothedTargets = new float[numberOfBands];

        // Bước 1: Làm mịn giữa các bands kề nhau
        for (int i = 0; i < numberOfBands; i++) {
            // Lấy giá trị của band hiện tại
            float currentValue = targetBands[i];

            // Tính trung bình với các band lân cận
            float sum = currentValue;
            int count = 1;

            // Xét band bên trái
            if (i > 0) {
                sum += targetBands[i - 1] * 0.5f;
                count++;
            }

            // Xét band bên phải
            if (i < numberOfBands - 1) {
                sum += targetBands[i + 1] * 0.5f;
                count++;
            }

            // Tính giá trị trung bình có trọng số
            smoothedTargets[i] = (sum / count) * 0.3f + currentValue * 0.7f;
        }

        // Bước 2: Cập nhật giá trị hiển thị với tốc độ đồng nhất
        final float riseFactor = Config.SMOOTH_FACTOR;
        final float fallRate = (float) Config.GRAVITY;

        for (int i = 0; i < numberOfBands; i++) {
            float target = smoothedTargets[i];

            if (target > currentBands[i]) {
                // Sử dụng một hệ số rise duy nhất cho tất cả dải tần số
                float diff = target - currentBands[i];
                // Điều chỉnh tốc độ tăng dựa trên khoảng cách đến target
                float adjustedRate = Math.min(riseFactor, diff * 0.5f);

                // Cập nhật giá trị với hiệu ứng tăng dần
                currentBands[i] += diff * Math.max(0.01f, adjustedRate);
            } else {
                // Sử dụng một hệ số fall duy nhất cho tất cả dải tần số
                currentBands[i] = Math.max(0f, currentBands[i] - fallRate);
            }

            // Đặt giá trị rất nhỏ thành 0
            if (currentBands[i] < 0.001f) {
                currentBands[i] = 0f;
            }
        }
    }

    /**
     * Cập nhật dữ liệu hiển thị chế độ demo
     */
    private void updateDemoVisualization() {
        demoPhase += 50; // Thúc đẩy animation

        for (int i = 0; i < numberOfBands; i++) {
            float normalizedPos = (float) i / numberOfBands;
            float time = demoPhase / 1000.0f;
            float value;

            // Tạo các pattern khác nhau cho tần số thấp, trung và cao
            if (normalizedPos < 0.3f) { // Bass
                value = 0.3f + 0.5f * (float) Math.sin(time * 1.2 + normalizedPos * Math.PI);
            } else if (normalizedPos < 0.7f) { // Mid
                value = 0.2f + 0.3f * (float) Math.sin(time * 2.5 + normalizedPos * 3.0 * Math.PI);
            } else { // Treble
                value = 0.1f + 0.2f * (float) Math.sin(time * 3.7 + normalizedPos * 5.0 * Math.PI);
            }

            // Thêm một chút ngẫu nhiên
            value += 0.05f * (float) Math.random();

            // Giới hạn giá trị
            demoData[i] = Math.max(0.0f, Math.min(1.0f, value));
        }

        // Áp dụng dữ liệu demo cho targets
        System.arraycopy(demoData, 0, targetBands, 0, numberOfBands);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Thiết lập để vẽ chất lượng cao
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
            float value = currentBands[i];
            int minBarHeight = 2;  // Chiều cao tối thiểu nếu có giá trị > 0
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
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("DEMO MODE", 10, 20);

            // Hiển thị thông tin về số lượng thanh và kích thước
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString("Bands: " + numberOfBands + " | Width: " + optimalBarWidth + "px", 10, 40);
        }

        g2d.dispose();
    }

    /**
     * Xử lý dữ liệu FFT mới từ âm thanh
     */
    public void updateVisualizationData(float[] fftData) {
        if (fftData == null || fftData.length == 0) return;

        demoMode = false;

        // Xử lý dữ liệu FFT thành số band thích hợp
        float[] bandValues = processFftToBands(fftData);
        // Lưu giá trị vào lịch sử
        System.arraycopy(bandValues, 0, bandHistory[historyIndex], 0, numberOfBands);
        historyIndex = (historyIndex + 1) % historySize;

        // Tính trung bình từ lịch sử để làm mịn dữ liệu
        for (int i = 0; i < numberOfBands; i++) {
            float sum = 0;
            for (int h = 0; h < historySize; h++) {
                sum += bandHistory[h][i];
            }
            bandValues[i] = sum / historySize;
        }

        // Cập nhật giá trị mục tiêu
        System.arraycopy(bandValues, 0, targetBands, 0, numberOfBands);
    }

    /**
     * Thay đổi số lượng thanh hiển thị và tính toán lại các giá trị cần thiết
     */

    public void setNumberOfBands(int newNumberOfBands) {
        if (newNumberOfBands < 2) {
            newNumberOfBands = 2; // Đảm bảo ít nhất 2 thanh
        } else if (newNumberOfBands > 256) {
            newNumberOfBands = 256; // Giới hạn tối đa
        }

        if (this.numberOfBands == newNumberOfBands) {
            return; // Không có thay đổi
        }

        // Lưu trữ giá trị cũ để nội suy
        float[] oldCurrentBands = this.currentBands;
        float[] oldTargetBands = this.targetBands;
        int oldNumberOfBands = this.numberOfBands;

        // Cập nhật số lượng thanh
        this.numberOfBands = newNumberOfBands;

        // Tạo mới các mảng với kích thước mới
        this.currentBands = new float[newNumberOfBands];
        this.targetBands = new float[newNumberOfBands];
        this.demoData = new float[newNumberOfBands];

        // Tính toán lại giới hạn tần số
        this.frequencyLimits = calculateFrequencyLimits();

        // Tạo lại bandHistory
        this.bandHistory = new float[historySize][newNumberOfBands];
        this.historyIndex = 0;

        // Nội suy các giá trị từ số lượng thanh cũ sang mới
        if (oldNumberOfBands > 0) {
            for (int i = 0; i < newNumberOfBands; i++) {
                float oldIndex = (float) i * oldNumberOfBands / newNumberOfBands;
                int lowerIndex = (int) oldIndex;
                int upperIndex = Math.min(lowerIndex + 1, oldNumberOfBands - 1);
                float fraction = oldIndex - lowerIndex;

                // Nội suy giữa 2 giá trị gần nhất
                currentBands[i] = oldCurrentBands[lowerIndex] * (1 - fraction) +
                        oldCurrentBands[upperIndex] * fraction;

                targetBands[i] = oldTargetBands[lowerIndex] * (1 - fraction) +
                        oldTargetBands[upperIndex] * fraction;
            }
        }

        // Yêu cầu vẽ lại
        repaint();
    }

    /**
     * Xử lý dữ liệu FFT thô thành các band tần số
     */
    private float[] processFftToBands(float[] fftData) {
        float[] result = new float[numberOfBands];

        // Lấy kích thước FFT
        int fftSize = fftData.length * 2;

        // Với mỗi band, tính tổng các bin FFT tương ứng với phạm vi tần số đó
        for (int i = 0; i < numberOfBands; i++) {
            double startFreq = frequencyLimits[i];
            double endFreq = frequencyLimits[i + 1];

            // Chuyển đổi tần số thành chỉ số bin FFT
            int startBin = Math.max(1, (int) Math.round(startFreq * fftSize / Config.SAMPLE_RATE));
            int endBin = Math.min(fftSize / 2, (int) Math.round(endFreq * fftSize / Config.SAMPLE_RATE));

            // Tính tổng các bin FFT cho band này
            double sum = 0;
            int count = 0;
            for (int bin = startBin; bin <= endBin && bin < fftData.length; bin++) {
                sum += fftData[bin];
                count++;
            }

            // Tính trung bình cho band này (tránh chia cho 0)
            if (count > 0) {
                result[i] = (float) (sum / count);
            }

            // Giới hạn giá trị
            result[i] = Math.min(0.9f, result[i]);
        }

        return result;
    }


    /**
     * Tạo cửa sổ Hann cho FFT
     */
    private double[] createHannWindow(int size) {
        double[] window = new double[size];
        for (int i = 0; i < size; i++) {
            window[i] = 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (size - 1.0)));
        }
        return window;
    }

    /**
     * Dọn dẹp tài nguyên khi không còn cần thiết
     */
    public void dispose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        audioProcessor.stopProcessing();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.foregroundColor = textColor;
        repaint();
    }


    /**
     * Audio processor để xử lý tất cả đầu vào âm thanh và tính toán FFT
     */
    public class AudioProcessor {
        private final EnhancedSpectrumVisualizer visualizer;
        private Thread processorThread;
        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private File currentAudioFile;
        private double[] pcmBuffer;
        private int bufferPosition;

        public AudioProcessor(EnhancedSpectrumVisualizer visualizer) {
            this.visualizer = visualizer;
            this.pcmBuffer = new double[Config.CHUNK_SIZE * 4];
            this.bufferPosition = 0;
        }

        /**
         * Bắt đầu xử lý file âm thanh
         */
        public void startProcessing(File audioFile) {
            if (audioFile == null || !audioFile.exists()) {
                log.error("Audio file doesn't exist: {}", audioFile);
                return;
            }

            stopProcessing();

            synchronized (lock) {
                // Reset visualization state
                Arrays.fill(currentBands, 0);
                Arrays.fill(targetBands, 0);
                for (int i = 0; i < historySize; i++) {
                    Arrays.fill(bandHistory[i], 0);
                }

                this.currentAudioFile = audioFile;
                isRunning.set(true);
                bufferPosition = 0;  // Reset buffer position

                processorThread = new Thread(this::processAudioFile, "AudioProcessorThread");
                processorThread.setDaemon(true);
                processorThread.start();

                demoMode = false;
            }
        }

        /**
         * Dừng xử lý âm thanh
         */
        public void stopProcessing() {
            synchronized (lock) {
                isRunning.set(false);
                if (processorThread != null && processorThread.isAlive()) {
                    try {
                        processorThread.interrupt();
                        processorThread.join(500); // đợi tối đa 500ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    processorThread = null;
                }
            }
        }


        private void processAudioFile() {
            try (FileInputStream fis = new FileInputStream(currentAudioFile);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                Bitstream bitstream = new Bitstream(bis);
                Decoder decoder = new Decoder();
                bufferPosition = 0;

                while (isRunning.get()) {
                    try {
                        Header header = bitstream.readFrame();
                        if (header == null) {
                            // Loop file
                            try {
                                // Đóng và khởi tạo lại stream
                                bitstream.close();
                                bufferPosition = 0; // Reset buffer position

                                // Tạo stream mới
                                FileInputStream newFis = new FileInputStream(currentAudioFile);
                                BufferedInputStream newBis = new BufferedInputStream(newFis);
                                bitstream = new Bitstream(newBis);
                                decoder = new Decoder();
                                log.debug("Audio file looped");
                                continue;
                            } catch (Exception e) {
                                log.error("Error looping audio file: {}", e.getMessage());
                                break;
                            }
                        }

                        try {
                            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                            if (output != null) {
                                processPcmSamples(output.getBuffer(), output.getBufferLength());
                            }
                        } finally {
                            bitstream.closeFrame();
                        }

                        // Thời gian nghỉ để giảm sử dụng CPU
                        Thread.sleep(15);
                    } catch (Exception e) {
                        log.error("Frame processing error: {}", e.getMessage());
                        // Try to continue with the next frame
                        try {
                            bitstream.closeFrame();
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing audio: {}", e.getMessage());
            }
        }

        /**
         * Xử lý mẫu PCM bằng cách thêm chúng vào buffer và phân tích các đoạn
         */
        private void processPcmSamples(short[] samples, int length) {
            try {
                // Chuyển đổi mẫu PCM (16-bit) sang double (-1.0 đến 1.0 range)
                for (int i = 0; i < length && bufferPosition < pcmBuffer.length; i++) {
                    pcmBuffer[bufferPosition++] = samples[i] / 32768.0;
                }

                // Xử lý đoạn khi chúng ta có đủ dữ liệu
                while (bufferPosition >= Config.CHUNK_SIZE) {
                    // Trích xuất đoạn cho FFT
                    double[] chunk = new double[Config.CHUNK_SIZE];
                    System.arraycopy(pcmBuffer, 0, chunk, 0, Config.CHUNK_SIZE);

                    // Áp dụng hàm cửa sổ để giảm rò rỉ phổ
                    applyWindow(chunk);

                    // Tính toán FFT
                    float[] fftOutput = performFft(chunk);

                    // Gửi đến visualizer
                    visualizer.updateVisualizationData(fftOutput);

                    // Dịch buffer theo số lượng overlap cho sliding window
                    int advanceAmount = Config.CHUNK_SIZE / Config.OVERLAP_RATIO;

                    if (bufferPosition > advanceAmount) {
                        System.arraycopy(pcmBuffer, advanceAmount, pcmBuffer, 0, bufferPosition - advanceAmount);
                        bufferPosition -= advanceAmount;
                    } else {
                        // Nếu số mẫu còn lại ít hơn advanceAmount, reset buffer
                        bufferPosition = 0;
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error processing PCM samples: {}", e.getMessage());
                bufferPosition = 0; // Reset buffer to recover from error
            }
        }


        /**
         * Áp dụng hàm cửa sổ cho dữ liệu âm thanh
         */
        private void applyWindow(double[] audio) {
            for (int i = 0; i < audio.length; i++) {
                audio[i] *= hannWindow[i];
            }
        }

        /**
         * Thực hiện FFT trên dữ liệu âm thanh và trả về phổ biên độ
         */
        private float[] performFft(double[] audioData) {
            try {
                // Đảm bảo độ dài là lũy thừa của 2 cho FFT
                int nextPowerOfTwo = 1;
                while (nextPowerOfTwo < audioData.length) {
                    nextPowerOfTwo <<= 1;
                }

                double[] paddedAudio;
                if (nextPowerOfTwo != audioData.length) {
                    paddedAudio = new double[nextPowerOfTwo];
                    System.arraycopy(audioData, 0, paddedAudio, 0, audioData.length);
                } else {
                    paddedAudio = audioData;
                }

                // Thực hiện FFT
                FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
                Complex[] fftResult = fft.transform(paddedAudio, TransformType.FORWARD);

                // Chuyển đổi sang phổ biên độ (chỉ cần nửa do tính đối xứng)
                int magnitudeLength = paddedAudio.length / 2;
                float[] magnitudes = new float[magnitudeLength];

                for (int i = 0; i < magnitudeLength && i < fftResult.length; i++) {
                    double re = fftResult[i].getReal();
                    double im = fftResult[i].getImaginary();

                    // Tính biên độ và áp dụng thang logarit
                    double magnitude = Math.sqrt(re * re + im * im);
                    magnitudes[i] = (float) Math.min(1.0, Math.log10(1.0 + magnitude * 50) / 4.0);
                }

                return magnitudes;
            } catch (Exception e) {
                log.error("FFT processing error: {}", e.getMessage());
                return new float[Config.CHUNK_SIZE / 2];
            }
        }
    }
}