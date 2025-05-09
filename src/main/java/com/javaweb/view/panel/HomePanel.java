package com.javaweb.view.panel;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.*;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HomePanel extends JPanel implements ThemeChangeListener, PlayerEventListener {
    private final MusicPlayerFacade playerFacade;
    private JPanel recentlyPlayedPanel;
    private JPanel recommendationsPanel;
    private JScrollPane scrollPane;
    private final Map<SongDTO, JButton> playPauseButtonMap = new HashMap<>();

    public HomePanel(MusicPlayerFacade playerFacade) {

        playerFacade.subscribeToPlayerEvents(this);


        setOpaque(false);
        this.playerFacade = playerFacade;
        setLayout(new BorderLayout());

        JPanel mainPanel = GuiUtil.createPanel();
        mainPanel.setLayout(new MigLayout("fill, insets 10", "[grow]", "[top][grow]"));

        initComponents(mainPanel);

        // Add scrollpane to allow scrolling through content
        scrollPane = GuiUtil.createStyledScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        ThemeManager.getInstance().addThemeChangeListener(this);
        onThemeChanged(
                ThemeManager.getInstance().getBackgroundColor(),
                ThemeManager.getInstance().getTextColor(),
                ThemeManager.getInstance().getAccentColor()
        );
    }

    private void initComponents(JPanel mainPanel) {
        JPanel asciiArtPanel = createWelcomePanel();
        mainPanel.add(asciiArtPanel, "growx, wrap");

        createRecentlyPlayedSection();
        createRecommendationsSection();


        mainPanel.add(recentlyPlayedPanel, "grow, wrap");

        mainPanel.add(recommendationsPanel, "grow");
    }

    private JPanel createWelcomePanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout("fill, insets 0"));

        String[] welcomeMessages = AppConstant.WELCOME_MESSAGE;
        int randomIndex = (int) (Math.random() * welcomeMessages.length);
        String selectedMessage = welcomeMessages[randomIndex];
        String asciiArt = generateFigletArt(selectedMessage);

        JTextArea asciiArtTextArea = GuiUtil.createTextArea(asciiArt, Font.BOLD, 15);
        panel.add(asciiArtTextArea, "left, top, growx");

        return panel;
    }

    private void createRecentlyPlayedSection() {
        recentlyPlayedPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Recently played", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton showAllButton = GuiUtil.createButton("Show all");
        showAllButton.addActionListener(e -> {
            log.info("Show all recently played songs clicked");
        });
        headerPanel.add(showAllButton, BorderLayout.EAST);

        recentlyPlayedPanel.add(headerPanel, "growx, wrap");

        JPanel songGridPanel = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                ""));

        loadRecentlyPlayedSongs(songGridPanel);
        recentlyPlayedPanel.add(songGridPanel, "grow");
    }

    private void createRecommendationsSection() {
        recommendationsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Recommended for you!", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton showAllButton = GuiUtil.createButton("Show all");
        showAllButton.addActionListener(e -> {
            log.info("Show all recommendations clicked");
        });
        headerPanel.add(showAllButton, BorderLayout.EAST);

        recommendationsPanel.add(headerPanel, "growx, wrap");

        // Grid panel for song items
        JPanel songGridPanel = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                ""));

        loadRecommendedSongs(songGridPanel);
        recommendationsPanel.add(songGridPanel, "grow");

    }

    private void loadRecommendedSongs(JPanel container) {
        if (!NetworkChecker.isNetworkAvailable()) {
            container.add(GuiUtil.createErrorLabel("Network unavailable!"), "span");
            return;
        }

        try {
            List<SongDTO> recommendedSongs = CommonApiUtil.fetchRecommendedSongs(20);

            if (recommendedSongs.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No recommendations available"), "span");
                return;
            }

            for (SongDTO song : recommendedSongs) {
                container.add(createSongCard(song));
            }
        } catch (Exception e) {
            log.error("Failed to load recommended songs", e);
            container.add(GuiUtil.createErrorLabel("Failed to load recommendations"), "span");
        }
    }


    private void loadRecentlyPlayedSongs(JPanel container) {
        if (!NetworkChecker.isNetworkAvailable()) {
            container.add(GuiUtil.createErrorLabel("Network unavailable!"), "span");
            return;
        }

        try {
            List<SongDTO> recentlyPlayed = CommonApiUtil.fetchRecentPlayHistory(20);

            if (recentlyPlayed.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No recently played songs"), "span");
                return;
            }

            for (SongDTO song : recentlyPlayed) {
                container.add(createSongCard(song));
            }
        } catch (Exception e) {
            log.error("Failed to load recently played songs", e);
            container.add(GuiUtil.createErrorLabel("Failed to load recently played songs"), "span");
        }
    }

    private JPanel createSongCard(SongDTO song) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        JPanel imagePanel = GuiUtil.createPanel();
        imagePanel.setPreferredSize(new Dimension(150, 150));
        imagePanel.setMaximumSize(new Dimension(150, 150));
        imagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        AsyncImageLabel coverLabel = GuiUtil.createAsyncImageLabel(150, 150, 15);
        playerFacade.populateSongImage(song, coverLabel::setLoadedImage);

        imagePanel.add(coverLabel);
        card.add(imagePanel);

        JPanel infoBarPanel = GuiUtil.createPanel(new BorderLayout(5, 0));
        infoBarPanel.setPreferredSize(new Dimension(150, 30));
        infoBarPanel.setMaximumSize(new Dimension(150, 30));
        infoBarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textInfoPanel = GuiUtil.createPanel();
        textInfoPanel.setLayout(new BoxLayout(textInfoPanel, BoxLayout.Y_AXIS));

        // Title with limited width
        JLabel titleLabel = GuiUtil.createLabel(
                StringUtils.getTruncatedTextByWidth(song.getTitle(), FontUtil.getSpotifyFont(Font.BOLD, 12), 100),
                Font.BOLD, 12
        );
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Artist name with limited width
        String artistName = song.getSongArtist() != null ?
                StringUtils.getTruncatedTextByWidth(song.getSongArtist(), FontUtil.getSpotifyFont(Font.PLAIN, 10), 100) :
                "Unknown Artist";
        JLabel artistLabel = GuiUtil.createLabel(artistName, Font.PLAIN, 10);
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add tooltips for full text
        titleLabel.setToolTipText(song.getTitle());
        artistLabel.setToolTipText(song.getSongArtist());

        textInfoPanel.add(titleLabel);
        textInfoPanel.add(artistLabel);

        JPanel controlPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controlPanel.setPreferredSize(new Dimension(24, 24));

        JButton playPauseButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 24, 24);
        playPauseButton.setPreferredSize(new Dimension(24, 24));
        playPauseButton.setMargin(new Insets(0, 0, 0, 0));

        playPauseButtonMap.put(song, playPauseButton);

        playPauseButton.addActionListener(e -> {
            if (playerFacade.getCurrentSong() != null &&
                    playerFacade.getCurrentSong().getId().equals(song.getId())) {
                if (playerFacade.isPaused()) {
                    playerFacade.playCurrentSong();
                } else {
                    playerFacade.pauseSong();
                }
            } else {
                playerFacade.loadSong(song);
            }
        });

        controlPanel.add(playPauseButton);

        // Add text and controls to the info bar
        infoBarPanel.add(textInfoPanel, BorderLayout.CENTER);
        infoBarPanel.add(controlPanel, BorderLayout.EAST);


        card.add(Box.createVerticalStrut(20));
        // Add info bar panel to card
        card.add(infoBarPanel);

        GuiUtil.addHoverEffect(card);

        return card;
    }

    private String generateFigletArt(String text) {
        try {
            Process process = getProcess(text);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            process.waitFor();
            return builder.toString();
        } catch (Exception e) {
            log.error("Error generating Figlet art", e);
            return "Welcome to MuseMoe!";
        }
    }

    private Process getProcess(String text) throws IOException {
        File folder = new File("D:\\figlet\\usr\\share\\figlet");
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            log.error(".flf files not found!.");
        }

        File randomFile = files[(int) (Math.random() * files.length)];
        String fontName = randomFile.getName();
        ProcessBuilder processBuilder = new ProcessBuilder(
                "figlet",
                "-f",
                fontName,
                "-W",
                "-w 500",
                "-s",
                text
        );

        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        setBackground(backgroundColor);
        setForeground(textColor);
        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);

    }

    public void cleanup() {
        playerFacade.unsubscribeFromPlayerEvents(this);
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        switch (event.type()) {
            case PLAYBACK_STARTED -> {
                SongDTO currentSong = (SongDTO) event.data();
                updatePlayPauseButtons(currentSong, true);
            }
            case PLAYBACK_PAUSED -> {
                SongDTO currentSong = (SongDTO) event.data();
                updatePlayPauseButtons(currentSong, false);
            }
        }
    }

    private void updatePlayPauseButtons(SongDTO currentSong, boolean isPlaying) {
        // Update all play/pause buttons based on current playing song
        SwingUtilities.invokeLater(() -> {
            playPauseButtonMap.forEach((song, button) -> {
                if (currentSong != null && song.getId().equals(currentSong.getId())) {
                    if (isPlaying) {
                        button.setIcon(GuiUtil.createColoredIcon(
                                AppConstant.PAUSE_ICON_PATH,
                                ThemeManager.getInstance().getTextColor(),
                                24, 24)
                        );
                    } else {
                        button.setIcon(GuiUtil.createColoredIcon(
                                AppConstant.PLAY_ICON_PATH,
                                ThemeManager.getInstance().getTextColor(),
                                24, 24)
                        );
                    }
                } else {
                    button.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH,
                            ThemeManager.getInstance().getTextColor(),
                            24, 24)
                    );
                }
            });
        });
    }

}