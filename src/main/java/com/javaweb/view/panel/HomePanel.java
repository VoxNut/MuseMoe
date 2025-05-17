package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.*;
import com.javaweb.view.HomePage;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JPanel recommendationAlbumsPanel;
    private JScrollPane scrollPane;
    private Map<SongDTO, JButton> playPauseButtonMap = new HashMap<>();

    public HomePanel() {
        playerFacade = App.getBean(MusicPlayerFacade.class);
        playerFacade.subscribeToPlayerEvents(this);
        setOpaque(false);
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
        createRecommendationAlbumsSection();

        mainPanel.add(recentlyPlayedPanel, "grow, wrap");

        mainPanel.add(recommendationsPanel, "grow, wrap");

        mainPanel.add(recommendationAlbumsPanel, "grow");
    }

    private JPanel createWelcomePanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout("fill, insets 0"));

        String[] welcomeMessages = AppConstant.WELCOME_MESSAGE;
        int randomIndex = (int) (Math.random() * welcomeMessages.length);
        String selectedMessage = welcomeMessages[randomIndex];
        String asciiArt = generateFigletArt(selectedMessage);

        JTextArea asciiArtTextArea = GuiUtil.createTextArea(asciiArt);
        panel.add(asciiArtTextArea, "left, top, growx");

        return panel;
    }

    private void createRecentlyPlayedSection() {
        recentlyPlayedPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Recently played", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);


        recentlyPlayedPanel.add(headerPanel, "growx, wrap");

        JPanel songGridPanel = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                ""));

        loadRecentlyPlayedSongs(songGridPanel);
        recentlyPlayedPanel.add(songGridPanel, "grow");
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

    private void createRecommendationsSection() {
        recommendationsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Recommended for you!", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);


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

    private void createRecommendationAlbumsSection() {
        recommendationAlbumsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Albums you might like!", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);


        recommendationAlbumsPanel.add(headerPanel, "growx, wrap");

        // Grid panel for song items
        JPanel albumGridPanel = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                ""));

        loadRecommendedAlbums(albumGridPanel);
        recommendationAlbumsPanel.add(albumGridPanel, "grow");

    }

    private void loadRecommendedAlbums(JPanel container) {
        if (!NetworkChecker.isNetworkAvailable()) {
            container.add(GuiUtil.createErrorLabel("Network unavailable!"), "span");
            return;
        }

        try {
            // Use the new method from CommonApiUtil
            List<AlbumDTO> recommendedAlbums = CommonApiUtil.fetchRecommendedAlbums(20);

            if (recommendedAlbums.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No album recommendations available"), "span");
                return;
            }

            for (AlbumDTO album : recommendedAlbums) {
                container.add(createAlbumCard(album));
            }
        } catch (Exception e) {
            log.error("Failed to load recommended albums", e);
            container.add(GuiUtil.createErrorLabel("Failed to load album recommendations"), "span");
        }
    }

    // Create album card method
    private JPanel createAlbumCard(AlbumDTO album) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create square album cover with rounded corners
        AsyncImageLabel coverLabel = GuiUtil.createAsyncImageLabel(150, 150, 15);
        coverLabel.startLoading();
        playerFacade.populateAlbumImage(album, coverLabel::setLoadedImage);
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create info panel for text
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.setOpaque(false);

        Font titleFont = FontUtil.getSpotifyFont(Font.BOLD, 12);

        // Album title with truncation and tooltip
        JLabel titleLabel = GuiUtil.createLabel(
                StringUtils.getTruncatedTextByWidth(album.getTitle(), titleFont, 140),
                Font.BOLD, 12);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setToolTipText(album.getTitle());

        // Artist name with tooltip
        JLabel artistLabel = GuiUtil.createLabel(album.getArtistName(), Font.PLAIN, 11);
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        artistLabel.setToolTipText(album.getArtistName());

        // Add components
        card.add(coverLabel);
        card.add(Box.createVerticalStrut(8));
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(artistLabel);
        card.add(infoPanel);

        // Add hover effect
        GuiUtil.addHoverEffect(card);

//        card.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (SwingUtilities.isLeftMouseButton(e)) {
//                    log.info("Album clicked: {}", album.getTitle());
//                    PlaylistDTO playlistDTO = playerFacade.convertSongListToPlaylist(album.getSongDTOS(), album.getTitle());
//                    if (album.getSongDTOS() != null && !album.getSongDTOS().isEmpty()) {
//                        playerFacade.loadSongWithContext(album.getSongDTOS().iterator().next(), playlistDTO, MusicPlayerFacade.PlaylistSourceType.ALBUM);
//                    }
//                }
//            }
//        });

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    log.info("Album clicked: {}", album.getTitle());

                    HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(HomePanel.this);
                    homePage.navigateToAlbumView(album);

                    PlaylistDTO playlistDTO = playerFacade.convertSongListToPlaylist(album.getSongDTOS(), album.getTitle());
                    playerFacade.setCurrentPlaylist(playlistDTO);
                }
            }
        });

        return card;
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

        JPanel controlButtonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controlButtonsPanel.setPreferredSize(new Dimension(24, 24));

        // Create play button
        JButton playPauseButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 24, 24);
        playPauseButton.setPreferredSize(new Dimension(24, 24));
        playPauseButton.setMargin(new Insets(0, 0, 0, 0));


        playPauseButton.addActionListener(e -> {
            SongDTO currentSong = playerFacade.getCurrentSong();
            if (isSameSong(currentSong, song)) {
                if (playerFacade.isPaused()) {
                    playerFacade.playCurrentSong();
                } else {
                    playerFacade.pauseSong();
                }
            } else {
                if (song.getIsLocalFile()) {
                    playerFacade.loadLocalSong(song);
                } else {
                    playerFacade.loadSong(song);
                }
            }
        });

        playPauseButtonMap.put(song, playPauseButton);
        controlButtonsPanel.add(playPauseButton);

        // Add text and controls to the info bar
        infoBarPanel.add(textInfoPanel, BorderLayout.CENTER);
        infoBarPanel.add(controlButtonsPanel, BorderLayout.EAST);


        card.add(Box.createVerticalStrut(20));
        // Add info bar panel to card
        card.add(infoBarPanel);

        GuiUtil.addHoverEffect(card);

        GuiUtil.addSongContextMenu(card, song);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Component clickedComponent = SwingUtilities.getDeepestComponentAt(card, e.getX(), e.getY());
                    if (!(clickedComponent instanceof JButton)) {
                        log.info("Song clicked: {}", song.getTitle());
                        HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(HomePanel.this);
                        homePage.navigateToSongDetailsView(song);
                    }
                }
            }
        });

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

        assert files != null;
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

    private boolean isSameSong(SongDTO song1, SongDTO song2) {
        if (song1 == null || song2 == null) return false;
        if (!song1.getIsLocalFile() && !song2.getIsLocalFile()) {
            return song1.getId() != null &&
                    song2.getId() != null &&
                    song1.getId().equals(song2.getId());
        }
        return false;
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
        SwingUtilities.invokeLater(() -> {
            playPauseButtonMap.forEach((song, button) -> {
                if (isSameSong(currentSong, song)) {
                    if (isPlaying) {
                        button.setIcon(GuiUtil.createColoredIcon(
                                AppConstant.PAUSE_ICON_PATH,
                                ThemeManager.getInstance().getTextColor(),
                                24, 24)
                        );
                        button.setRolloverIcon(GuiUtil.createColoredIcon(
                                AppConstant.PAUSE_ICON_PATH,
                                GuiUtil.lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f),
                                24, 24)
                        );
                    } else {
                        button.setIcon(GuiUtil.createColoredIcon(
                                AppConstant.PLAY_ICON_PATH,
                                ThemeManager.getInstance().getTextColor(),
                                24, 24)
                        );
                        button.setRolloverIcon(GuiUtil.createColoredIcon(
                                AppConstant.PLAY_ICON_PATH,
                                GuiUtil.lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f),
                                24, 24)
                        );
                    }
                } else {
                    button.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH,
                            ThemeManager.getInstance().getTextColor(),
                            24, 24)
                    );
                    button.setRolloverIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH,
                            GuiUtil.lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f),
                            24, 24)
                    );
                }
            });
        });
    }


}