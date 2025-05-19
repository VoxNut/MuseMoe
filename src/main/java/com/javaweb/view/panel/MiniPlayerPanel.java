package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.enums.PlaylistSourceType;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Slf4j
public class MiniPlayerPanel extends JPanel implements PlayerEventListener, ThemeChangeListener {
    private final MusicPlayerFacade playerFacade;

    private JLabel spinningDisc;
    private JPanel controlButtonsPanel;

    private Timer spinTimer;
    private Timer scrollTimer;

    private JLabel scrollingLabel;
    private double rotationAngle = 0.0;
    private static final double SPIN_SPEED = Math.PI / 60;
    private static final int TIMER_DELAY = 16;
    private static final int SCROLL_DELAY = 16;
    private static final float SCROLL_SPEED = 0.5f;
    private float scrollPosition = 0.0f;

    @Getter
    private ProgressTrackBar progressTrackBar;
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    private JLabel sourceTypeLabel;

    public MiniPlayerPanel() {
        this.playerFacade = App.getBean(MusicPlayerFacade.class);
        this.playerFacade.subscribeToPlayerEvents(this);

        setOpaque(false);
        setPreferredSize(new Dimension(800, 100));
        setLayout(new BorderLayout());
        setBorder(GuiUtil.createTitledBorder("Playing", TitledBorder.LEFT));

        backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        textColor = ThemeManager.getInstance().getTextColor();
        accentColor = ThemeManager.getInstance().getAccentColor();

        scrollTimer = new Timer(SCROLL_DELAY, e -> {
            FontMetrics fm = scrollingLabel.getFontMetrics(scrollingLabel.getFont());
            int textWidth = fm.stringWidth(scrollingLabel.getText()) + 30;

            scrollPosition += SCROLL_SPEED;
            if (scrollPosition > textWidth) {
                scrollPosition = 0;
            }
            scrollingLabel.repaint();
        });

        initComponents();

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void initComponents() {
        JPanel rootPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel leftPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        spinningDisc = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (getIcon() == null) return;

                Graphics2D g2d = (Graphics2D) g.create();
                GuiUtil.configureGraphicsForHighQuality(g2d);

                ImageIcon icon = (ImageIcon) getIcon();
                BufferedImage image = new BufferedImage(
                        icon.getIconWidth(),
                        icon.getIconHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                icon.paintIcon(null, image.getGraphics(), 0, 0);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                AffineTransform transform = new AffineTransform();
                transform.rotate(rotationAngle, centerX, centerY);

                g2d.transform(transform);

                int x = (getWidth() - icon.getIconWidth()) / 2;
                int y = (getHeight() - icon.getIconHeight()) / 2;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.drawImage(image, x, y, null);

                g2d.dispose();
            }
        };

        spinTimer = new Timer(TIMER_DELAY, e -> {
            rotationAngle += SPIN_SPEED;
            if (rotationAngle >= 2 * Math.PI) {
                rotationAngle = 0;
            }
            spinningDisc.repaint();
        });
        spinningDisc.setPreferredSize(new Dimension(60, 60));
        spinningDisc.setMinimumSize(new Dimension(60, 60));
        spinningDisc.setMaximumSize(new Dimension(60, 60));
        spinningDisc.setVisible(false);


        scrollingLabel = new ScrollingLabel();
        scrollingLabel.setPreferredSize(new Dimension(150, 25));
        scrollingLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        scrollingLabel.setForeground(textColor);
        scrollingLabel.setVisible(false);

        leftPanel.add(spinningDisc);
        leftPanel.add(scrollingLabel);

        JPanel centerPanel = GuiUtil.createPanel(new BorderLayout(0, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel upperControlPanel = GuiUtil.createPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        sourceTypeLabel = GuiUtil.createLabel("");
        sourceTypeLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        sourceTypeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        sourceTypeLabel.setVisible(false);

        sourceTypeLabel.setPreferredSize(new Dimension(150, 20));
        sourceTypeLabel.setMinimumSize(new Dimension(10, 20));
        sourceTypeLabel.setMaximumSize(new Dimension(150, 20));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        upperControlPanel.add(sourceTypeLabel, gbc);


        controlButtonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        controlButtonsPanel.setVisible(false);

        JButton prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, 20, 20);
        prevButton.addActionListener(e -> {
            // Once the song in queue is played it cant never go back
            if (playerFacade.getCurrentPlaylist() != null & playerFacade.getCurrentPlaylist().getSourceType() == PlaylistSourceType.QUEUE) {
                GuiUtil.showToast(this, "Cannot go back in Queue!");
                return;
            }
            playerFacade.prevSong();
        });

        // Play button
        JButton playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 20, 20);
        playButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) {
                return;
            }
            playerFacade.playCurrentSong();
        });

        // Pause button
        JButton pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, 20, 20);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) {
                return;
            }
            playerFacade.pauseSong();
        });

        // Next button
        JButton nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, 20, 20);
        nextButton.addActionListener(e -> {
            if (playerFacade.getQueueSongs() == null || playerFacade.getQueueSongs().isEmpty()) {
                GuiUtil.showToast(this, "No more songs in queue!");
            } else {
                playerFacade.nextSong();
            }
        });

        // Add buttons to control panel
        controlButtonsPanel.add(prevButton);
        controlButtonsPanel.add(playButton);
        controlButtonsPanel.add(pauseButton);
        controlButtonsPanel.add(nextButton);


        // Progress bar panel with the time label
        JPanel progressPanel = GuiUtil.createPanel(new BorderLayout(0, 0));

        // Progress bar
        progressTrackBar = new ProgressTrackBar();
        progressTrackBar.setPreferredSize(new Dimension(200, 20));
        progressTrackBar.setBackground(GuiUtil.darkenColor(backgroundColor, 0.2f));
        progressTrackBar.setForeground(accentColor);
        progressTrackBar.setVisible(false);


        progressPanel.add(progressTrackBar, BorderLayout.CENTER);


        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        upperControlPanel.add(controlButtonsPanel, gbc);

        JPanel rightPlaceholder = GuiUtil.createPanel();
        rightPlaceholder.setPreferredSize(new Dimension(150, 20));
        rightPlaceholder.setMinimumSize(new Dimension(10, 20));
        rightPlaceholder.setMaximumSize(new Dimension(150, 20));

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.anchor = GridBagConstraints.EAST;
        upperControlPanel.add(rightPlaceholder, gbc);

        centerPanel.add(upperControlPanel, BorderLayout.NORTH);
        centerPanel.add(progressPanel, BorderLayout.CENTER);

        // Add mouse listeners to progress bar
        progressTrackBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (playerFacade.isHavingAd()) {
                    return;
                }
                playerFacade.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (playerFacade.isHavingAd()) {
                    return;
                }

                float percentage = (float) e.getX() / progressTrackBar.getWidth();
                percentage = Math.max(0, Math.min(1.0f, percentage));

                if (playerFacade.getCurrentSong() != null) {
                    long totalFrames = playerFacade.getCurrentSong().getFrame();
                    int newFrame = (int) (totalFrames * percentage);
                    int newTimeInMilli = (int) (newFrame / playerFacade.getCurrentSong().getFrameRatePerMilliseconds());

                    playerFacade.setCurrentTimeInMilli(newTimeInMilli);
                    playerFacade.setCurrentFrame(newFrame);
                    playerFacade.playCurrentSong();
                }
            }
        });

        progressTrackBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (playerFacade.isHavingAd()) {
                    return;
                }

                float percentage = Math.min(1.0f, Math.max(0, (float) e.getX() / progressTrackBar.getWidth()));
                progressTrackBar.setProgress(percentage);

                if (playerFacade.getCurrentSong() != null) {
                    long totalFrames = playerFacade.getCurrentSong().getFrame();
                    int frame = (int) (percentage * totalFrames);
                    int timeInMillis = (int) (frame / playerFacade.getCurrentSong().getFrameRatePerMilliseconds());

                    updateSongTimeLabel(timeInMillis);
                    updateLabelColors(percentage);

                    playerFacade.notifySliderDragging(frame, timeInMillis);
                }
            }
        });

        // Assemble all panels
        rootPanel.add(leftPanel, BorderLayout.WEST);
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        add(rootPanel, BorderLayout.CENTER);
    }

    public void updatePlaybackProgress(SongDTO song, int frame) {
        if (song != null && progressTrackBar != null) {
            long totalFrames = song.getFrame();
            float progress = (float) frame / totalFrames;

            progressTrackBar.setProgress(progress);
            updateLabelColors(progress);
        }
    }


    public void updateLabelColors(float progress) {
        int midpoint = progressTrackBar.getWidth() / 2;
        int progressPosition = (int) (progress * progressTrackBar.getWidth());

        if (progressPosition >= midpoint) {
            progressTrackBar.setTimeTextColor(backgroundColor);
        } else {
            progressTrackBar.setTimeTextColor(textColor);
        }
    }

    public void updateSongTimeLabel(int currentTimeInMilli) {
        if (playerFacade.getCurrentSong() != null) {
            int minutes = (currentTimeInMilli / 1000) / 60;
            int seconds = (currentTimeInMilli / 1000) % 60;

            String totalDuration = playerFacade.getCurrentSong().getSongLength();
            String formattedTime = String.format("%d:%02d / %s", minutes, seconds, totalDuration);

            progressTrackBar.setTimeText(formattedTime);

            updateLabelColors(progressTrackBar.getProgress());
        }
    }

    public void showPlaybackControls() {
        spinningDisc.setVisible(true);
        progressTrackBar.setVisible(true);
        controlButtonsPanel.setVisible(true);
        sourceTypeLabel.setVisible(true);
        startTextScrolling();
        startDiscSpinning();
    }

    public void hidePlaybackControls() {
        spinningDisc.setVisible(false);
        progressTrackBar.setVisible(false);
        controlButtonsPanel.setVisible(false);
        sourceTypeLabel.setVisible(false);
        stopTextScrolling();
        stopDiscSpinning();
    }

    public void updateScrollingText(SongDTO song) {
        if (song != null) {
            String text = song.getTitle() + " - " + song.getSongArtist();
            scrollingLabel.setText(text);
            scrollingLabel.setVisible(true);

            if (spinTimer.isRunning()) {
                stopTextScrolling();
                startTextScrolling();
            }
        }
    }

    public void updateSpinningDisc(SongDTO song) {
        if (song != null) {
            int discSize = 60;
            if (song.getSongImage() != null) {
                spinningDisc.setIcon(GuiUtil.createDiscImageIcon(song.getSongImage(), discSize, discSize, 7));
            } else {
                spinningDisc.setIcon(GuiUtil.createDiscImageIcon(
                        GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH), discSize, discSize, 7
                ));
            }
        }
    }

    public void updatePlaybackInfo(SongDTO song) {
        if (song != null) {
            updateSpinningDisc(song);
            updateScrollingText(song);
        }
    }

    public void startDiscSpinning() {
        if (!spinTimer.isRunning()) {
            spinTimer.start();
        }
    }

    public void stopDiscSpinning() {
        if (spinTimer.isRunning()) {
            spinTimer.stop();
        }
    }

    public void startTextScrolling() {
        if (!scrollTimer.isRunning() && scrollingLabel.getText() != null) {
            scrollPosition = 0;

            FontMetrics fm = scrollingLabel.getFontMetrics(scrollingLabel.getFont());
            int textWidth = fm.stringWidth(scrollingLabel.getText());

            if (textWidth > scrollingLabel.getWidth()) {
                scrollTimer.start();
            }
        }
    }

    public void stopTextScrolling() {
        if (scrollTimer.isRunning()) {
            scrollTimer.stop();
        }
        scrollPosition = 0;
        scrollingLabel.repaint();
    }

    public void enablePauseButtonDisablePlayButton() {
        JButton playButton = (JButton) controlButtonsPanel.getComponent(1);
        JButton pauseButton = (JButton) controlButtonsPanel.getComponent(2);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);

        startDiscSpinning();
        startTextScrolling();
    }

    public void enablePlayButtonDisablePauseButton() {
        JButton playButton = (JButton) controlButtonsPanel.getComponent(1);
        JButton pauseButton = (JButton) controlButtonsPanel.getComponent(2);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);

        stopDiscSpinning();
        stopTextScrolling();
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.type()) {
                case SONG_LOADED -> {
                    SongDTO song = (SongDTO) event.data();
                    updatePlaybackInfo(song);
                    showPlaybackControls();
                    enablePauseButtonDisablePlayButton();
                }

                case PLAYBACK_STARTED -> enablePauseButtonDisablePlayButton();

                case PLAYBACK_PAUSED -> enablePlayButtonDisablePauseButton();

                case PLAYBACK_PROGRESS -> {
                    int[] data = (int[]) event.data();
                    updatePlaybackProgress(playerFacade.getCurrentSong(), data[0]);
                    updateSongTimeLabel(data[1]);
                }

                case HOME_PAGE_SLIDER_CHANGED -> showPlaybackControls();

                case PLAYLIST_LOADED -> {
                    PlaylistDTO playlist = (PlaylistDTO) event.data();
                    updateSourceTypeLabel(playlist);
                }
            }
        });
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        // Update component colors
        progressTrackBar.setBackground(GuiUtil.darkenColor(backgroundColor, 0.2f));
        progressTrackBar.setForeground(accentColor);

        // Update label colors
        updateLabelColors(progressTrackBar.getProgress());

        // Refresh the panel
        repaint();
    }

    public void cleanup() {
        if (playerFacade != null) {
            playerFacade.unsubscribeFromPlayerEvents(this);
        }

        if (spinTimer != null) {
            spinTimer.stop();
        }

        if (scrollTimer != null) {
            scrollTimer.stop();
        }

        ThemeManager.getInstance().removeThemeChangeListener(this);
    }


    private class ScrollingLabel extends JLabel {
        private static final int PADDING = 30;
        private boolean isScrollingNeeded = false;

        public ScrollingLabel() {
            setOpaque(false);
            setBorder(null);
        }

        @Override
        public void setText(String text) {
            super.setText(text);

            if (text != null && !text.isEmpty()) {
                FontMetrics fm = getFontMetrics(getFont());
                int textWidth = fm.stringWidth(text);
                isScrollingNeeded = textWidth > getWidth();
            } else {
                isScrollingNeeded = false;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            if (getText() == null || getText().isEmpty()) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(getForeground());

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int yPos = (getHeight() / 2) + (fm.getAscent() / 2) - 1;

            if (!isScrollingNeeded || !scrollTimer.isRunning()) {
                int xPos = 0;
                g2d.drawString(getText(), xPos, yPos);
            } else {
                int xPos = (int) -scrollPosition;

                g2d.drawString(getText(), xPos, yPos);

                int fullWidth = textWidth + PADDING;

                int offset = fullWidth;
                while (xPos + offset < getWidth() + textWidth) {
                    g2d.drawString(getText(), xPos + offset, yPos);
                    offset += fullWidth;
                }
            }

            g2d.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            // Ensure the width doesn't grow too large
            return new Dimension(Math.min(200, size.width), size.height);
        }
    }


    public class ProgressTrackBar extends JPanel {
        @Getter
        private float progress = 0.0f;
        private static final int CORNER_RADIUS = 3;
        private String timeText = "0:00 / 0:00";
        private Color textColor = accentColor;

        public ProgressTrackBar() {
            setOpaque(false);
        }

        public void setProgress(float progress) {
            this.progress = Math.max(0, Math.min(1.0f, progress));
            repaint();
        }

        public void setTimeText(String text) {
            this.timeText = text;
            repaint();
        }

        public void setTimeTextColor(Color color) {
            this.textColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            GuiUtil.configureGraphicsForHighQuality(g2d);

            int width = getWidth();
            int height = getHeight();

            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS);

            if (progress > 0) {
                int progressWidth = (int) (width * progress);
                g2d.setColor(getForeground());
                g2d.fillRoundRect(0, 0, progressWidth, height, CORNER_RADIUS, CORNER_RADIUS);
            }

            if (timeText != null && !timeText.isEmpty()) {
                FontMetrics fm = g2d.getFontMetrics(FontUtil.getSpotifyFont(Font.BOLD, 16));
                int textWidth = fm.stringWidth(timeText);
                int textHeight = fm.getHeight();

                int x = (width - textWidth) / 2;
                int y = ((height - textHeight) / 2) + fm.getAscent();

                g2d.setFont(FontUtil.getSpotifyFont(Font.BOLD, 16));

                int progressPosition = (int) (width * progress);

                int textStartX = x;

                for (int i = 0; i < timeText.length(); i++) {
                    String currentChar = String.valueOf(timeText.charAt(i));
                    int charWidth = fm.stringWidth(currentChar);

                    boolean isCovered = (textStartX + charWidth / 2) <= progressPosition;

                    g2d.setColor(isCovered ? backgroundColor : textColor);

                    g2d.drawString(currentChar, textStartX, y);

                    textStartX += charWidth;
                }
            }

            g2d.dispose();
        }
    }

    public void updateSourceTypeLabel(PlaylistDTO playlist) {
        if (playlist == null) {
            sourceTypeLabel.setText("Unknown Source");
            sourceTypeLabel.setVisible(true);
        }
        PlaylistSourceType sourceType = playlist.getSourceType();
        sourceTypeLabel.setVisible(false);

        String labelText = switch (sourceType) {
            case USER_PLAYLIST -> "PLAYLIST: " + playlist.getName();
            case ALBUM -> "ALBUM: " + playlist.getName();
            case LIKED_SONGS -> "LIKED";
            case QUEUE -> "QUEUE: " + playlist.getName();
            case SEARCH_RESULTS -> "SEARCH";
            case POPULAR -> "POPULAR";
            case LOCAL -> "LOCAL";
        };


        if (!labelText.isEmpty()) {
            sourceTypeLabel.setText(labelText);
            sourceTypeLabel.setVisible(true);
        } else {
            sourceTypeLabel.setVisible(false);
        }
    }
}