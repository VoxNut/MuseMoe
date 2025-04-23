package com.javaweb.view;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.*;
import com.javaweb.utils.*;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerFacade;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerMediator;
import com.javaweb.view.mini_musicplayer.event.PlayerEvent;
import com.javaweb.view.mini_musicplayer.event.PlayerEventListener;
import com.javaweb.view.panel.ExpandableCardPanel;
import com.javaweb.view.panel.RecentSearchDropdown;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class HomePage extends JFrame implements PlayerEventListener, ThemeChangeListener {
    private static final Dimension FRAME_SIZE = new Dimension(1024, 768);

    private JLabel spinningDisc;
    private JPanel controlButtonsPanel;
    private JSlider playbackSlider;
    private Timer spinTimer;
    private JLabel labelBeginning;
    private JLabel labelEnd;

    private double rotationAngle = 0.0;
    private static final double SPIN_SPEED = Math.PI / 60;
    private static final int TIMER_DELAY = 16; //
    private Timer scrollTimer;
    private JLabel scrollingLabel;
    private static final int SCROLL_DELAY = 16; // ~60 FPS (1000ms/60)
    private static final float SCROLL_SPEED = 0.5f; // Smaller increment
    private float scrollPosition = 0.0f;

    private final JPanel mainPanel;
    private final MusicPlayerFacade playerFacade;
    private JTextField searchField;
    private RecentSearchDropdown recentSearchDropdown;
    private final ImageIcon miniMuseMoeIcon;

    public HomePage() {
        initializeFrame();

        playerFacade = MusicPlayerFacade.getInstance();
        MusicPlayerMediator.getInstance().subscribeToPlayerEvents(this);
        ThemeManager.getInstance().addThemeChangeListener(this);


        miniMuseMoeIcon = GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 30, 30);
        GuiUtil.changeIconColor(miniMuseMoeIcon, ThemeManager.getInstance().getTextColor());
        setIconImage(miniMuseMoeIcon.getImage());


        mainPanel = createMainPanel();
        add(mainPanel);

    }


    private void initializeFrame() {
        //Set up for frame size
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(FRAME_SIZE);
        setResizable(true);
        //Doing nothing because I want user to confirm when logging out
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        GuiUtil.styleTitleBar(this, GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1), ThemeManager.getInstance().getTextColor());

        //Show option pane when user log out
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = GuiUtil.showConfirmMessageDialog(
                        HomePage.this,
                        "Do you really want to logout MuseMoe? We'll miss you :(",
                        "Exit"
                );
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        setVisible(true);

    }


    private JPanel createMainPanel() {
        JPanel mainPanel = GuiUtil.createPanel(new BorderLayout(0, 10));

        // Apply a radial gradient using the GuiUtil method instead of the linear gradient
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f),
                GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.4f),
                0.5f, 0.5f, 0.8f);


        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createCombinedPanel(), BorderLayout.CENTER);
        mainPanel.add(createMiniMusicPlayerPanel(), BorderLayout.SOUTH);

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus() && !searchField.contains(e.getPoint())) {
                    searchField.transferFocus();
                }
            }
        });
        return mainPanel;
    }

    private JPanel createCombinedPanel() {
        JPanel combinedPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        combinedPanel.add(createLibraryPanel(), BorderLayout.WEST);
        combinedPanel.add(createCenterPanel(), BorderLayout.CENTER);
        return combinedPanel;
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        headerPanel.setBorder(GuiUtil.createTitledBorder("Search", TitledBorder.LEFT));


        JPanel headerContent = GuiUtil.createPanel(new MigLayout(
                "insets 5, fillx",
                "[left]20[grow, fill]20[right]", // Three columns: left, center (growing), right
                "[center]"  // One row, centered vertically
        ));

        // ---------- LEFT SECTION (Date) ----------
        JPanel leftPanel = GuiUtil.createPanel(new MigLayout("insets 0", "[]5[]5[]", "[center]"));

        // Date display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        JLabel dateLabel = GuiUtil.createLabel(LocalDate.now().format(dateFormatter), Font.PLAIN, 14);
        leftPanel.add(dateLabel, "");

        // Navigation buttons
        JButton goBackButton = GuiUtil.changeButtonIconColor(AppConstant.GO_BACK_ICON_PATH, 20, 20);
        goBackButton.setToolTipText("Go Back");
        goBackButton.addActionListener(e -> {
            // Add navigation logic here
        });

        JButton goForwardButton = GuiUtil.changeButtonIconColor(AppConstant.GO_FORWARD_ICON_PATH, 20, 20);
        goForwardButton.setToolTipText("Go Forward");
        goForwardButton.addActionListener(e -> {
            // Add navigation logic here
        });

        leftPanel.add(goBackButton, "");
        leftPanel.add(goForwardButton, "");

        // ---------- CENTER SECTION (Search bar & Home icon) ----------
        JPanel centerPanel = GuiUtil.createPanel(new MigLayout("insets 0, fillx", "[]10[grow, fill]", "[center]"));

        // Home button
        JButton homeIcon = GuiUtil.changeButtonIconColor(AppConstant.HOME_ICON_PATH, 20, 20);
        homeIcon.setToolTipText("Home");
        centerPanel.add(homeIcon, "");

        // Search bar with wrapper for styling
        JPanel searchBarWrapper = GuiUtil.createPanel(new BorderLayout());
        searchBarWrapper.setBorder(GuiUtil.createCompoundBorder(2));
        searchBarWrapper.setPreferredSize(new Dimension(0, 40)); // Height only, width will be determined by layout

        // Search icon
        JButton lookupIcon = GuiUtil.changeButtonIconColor(AppConstant.LOOKUP_ICON_PATH, 20, 20);
        lookupIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        lookupIcon.addActionListener(e -> {
            if (!searchField.getText().isEmpty() &&
                    !searchField.getText().equals("What do you want to muse?...")) {
                performSearch(searchField.getText());
            }
        });
        searchBarWrapper.add(lookupIcon, BorderLayout.WEST);

        // Search field
        searchField = GuiUtil.createInputField("What do you want to muse?...", 20);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Add the same focus and key listeners as in your original code
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                if (searchField.getText().equals("What do you want to muse?...")) {
                    searchField.setText("");
                    searchField.setForeground(ThemeManager.getInstance().getTextColor());
                }
            }

            @Override
            public void focusLost(FocusEvent evt) {
                if (searchField.getText().isEmpty() || searchField.getText().trim().isEmpty()) {
                    searchField.setText("What do you want to muse?...");
                    searchField.setForeground(GuiUtil.darkenColor(ThemeManager.getInstance().getTextColor(), 0.3f));
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && recentSearchDropdown != null) {
                    recentSearchDropdown.hidePopup();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!searchField.getText().isEmpty() &&
                            !searchField.getText().equals("What do you want to muse?...")) {
                        performSearch(searchField.getText());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (recentSearchDropdown != null &&
                        !searchField.getText().isEmpty() &&
                        !searchField.getText().equals("What do you want to muse?...")) {
                    recentSearchDropdown.hidePopup();
                }
            }
        });

        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.getText().equals("What do you want to muse?...") ||
                        searchField.getText().isEmpty()) {
                    if (NetworkChecker.isNetworkAvailable()) {
                        loadRecentSearches();
                    } else {
                        GuiUtil.showNetworkErrorDialog(HomePage.this, "Internet connection is unavailable");
                    }
                }
            }
        });

        searchBarWrapper.add(searchField, BorderLayout.CENTER);
        centerPanel.add(searchBarWrapper, "growx");

        // ---------- RIGHT SECTION (User info) ----------
        JPanel rightPanel = GuiUtil.createPanel(new MigLayout("insets 0", "[]10[]10[]10[]", "[center]"));

        // Miniplayer button
        JButton miniplayerButton = GuiUtil.changeButtonIconColor(AppConstant.MINIPLAYER_ICON_PATH, 24, 24);
        miniplayerButton.setToolTipText("Open Music Player");
        miniplayerButton.addActionListener(e -> openMiniplayer());
        rightPanel.add(miniplayerButton, "");

        // Help panel
        JPanel helpPanel = GuiUtil.createPanel(new MigLayout("insets 5, wrap", "[center]", "[center]"));
        helpPanel.setBorder(GuiUtil.createTitledBorder("Help", TitledBorder.CENTER));
        JLabel helpLabel = GuiUtil.createLabel("Type ?", Font.BOLD, 14);
        helpPanel.add(helpLabel, "");
        rightPanel.add(helpPanel, "w 80!");

        // User info display
        String userRole = determineUserRole(getCurrentUser().getRoles());
        JLabel fullNameLabel = GuiUtil.createLabel(getCurrentUser().getFullName() != null ?
                getCurrentUser().getFullName() + " - " + userRole : "??? - " + userRole);
        rightPanel.add(fullNameLabel, "");

        // User avatar with menu
        JLabel avatarLabel = createUserProfileAvatar();
        rightPanel.add(avatarLabel, "");

        // Add the three main sections to the header content
        headerContent.add(leftPanel, "");
        headerContent.add(centerPanel, "growx");
        headerContent.add(rightPanel, "");

        // Add the content to the header panel
        headerPanel.add(headerContent, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createMiniMusicPlayerPanel() {
        JPanel miniMusicPlayerPanel = GuiUtil.createPanel(new BorderLayout());
        miniMusicPlayerPanel.setBorder(
                GuiUtil.createTitledBorder("Playing", TitledBorder.LEFT)
        );

        // Create a more organized layout with FlowLayout center alignment
        JPanel controlsWrapper = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        // Create scrolling text label
        scrollingLabel = new ScrollingLabel();
        scrollingLabel.setPreferredSize(new Dimension(200, 30));
        scrollingLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        scrollingLabel.setForeground(AppConstant.TEXT_COLOR);
        scrollingLabel.setVisible(false);

        // Create scroll timer
        scrollTimer = new Timer(SCROLL_DELAY, e -> {
            scrollPosition += SCROLL_SPEED;
            FontMetrics fm = scrollingLabel.getFontMetrics(scrollingLabel.getFont());
            int textWidth = fm.stringWidth(scrollingLabel.getText());
            if (scrollPosition >= textWidth) {
                scrollPosition = 0;
            }
            scrollingLabel.repaint();
        });

        // Spinning disc with song image
        spinningDisc = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();

                // Enable smoother rendering
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                g2d.rotate(rotationAngle, centerX, centerY);

                Icon icon = getIcon();
                if (icon != null) {
                    icon.paintIcon(this, g2d, 0, 0);
                }

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
        spinningDisc.setPreferredSize(new Dimension(50, 50));
        spinningDisc.setVisible(false);

        // Playback slider
        JPanel sliderPanel = GuiUtil.createPanel(new BorderLayout());
        sliderPanel.setPreferredSize(new Dimension(300, 60));
        sliderPanel.setMaximumSize(new Dimension(300, 60));

        playbackSlider = new JSlider();
        playbackSlider.setPreferredSize(new Dimension(300, 40));
        playbackSlider.setMaximumSize(new Dimension(300, 40));
        playbackSlider.setBackground(AppConstant.BACKGROUND_COLOR);
        playbackSlider.setForeground(AppConstant.TEXT_COLOR);
        playbackSlider.setFocusable(false);
        playbackSlider.setVisible(false);
        sliderPanel.add(playbackSlider, BorderLayout.CENTER);
        sliderPanel.add(createLabelsPanel(), BorderLayout.SOUTH);

        playbackSlider.addMouseListener(new MouseAdapter() {

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
                int sliderValue = playbackSlider.getValue();

                int newTimeInMilli = (int) (sliderValue
                        / playerFacade.getCurrentSong().getFrameRatePerMilliseconds());

                playerFacade.setCurrentTimeInMilli(newTimeInMilli);
                playerFacade.setCurrentFrame(sliderValue);
                playerFacade.playCurrentSong();

            }
        });

        playbackSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                playbackSlider.setValueIsAdjusting(true);

                if (playerFacade.isHavingAd()) return;

                // Calculate the value based on drag position
                int width = playbackSlider.getWidth();
                int position = e.getX();
                double percentOfWidth = (double) position / width;
                int value = (int) (percentOfWidth * playbackSlider.getMaximum());

                // Ensure value is within valid range
                value = Math.max(0, Math.min(value, playbackSlider.getMaximum()));

                // Set slider value
                playbackSlider.setValue(value);

                // Update the time label in real-time during dragging
                if (playerFacade.getCurrentSong() != null) {
                    double frameRate = playerFacade.getCurrentSong().getFrameRatePerMilliseconds();
                    int timeInMillis = (int) (value / frameRate);
                    updateSongTimeLabel(timeInMillis);

                    MusicPlayerMediator.getInstance().notifySliderDragging(value, timeInMillis);
                }
                SwingUtilities.invokeLater(() -> playbackSlider.setValueIsAdjusting(false));
            }
        });

        controlButtonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        controlButtonsPanel.setBackground(AppConstant.BACKGROUND_COLOR);
        controlButtonsPanel.setVisible(false);

        // Previous button
        JButton prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, 20, 20);
        prevButton.addActionListener(e -> playerFacade.prevSong());

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
            if (playerFacade.isHavingAd()) return;
            playerFacade.pauseSong();
        });

        // Next button
        JButton nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, 20, 20);
        nextButton.addActionListener(e -> playerFacade.nextSong());

        // Add buttons to control panel
        controlButtonsPanel.add(prevButton);
        controlButtonsPanel.add(playButton);
        controlButtonsPanel.add(pauseButton);
        controlButtonsPanel.add(nextButton);


        // Create layout for player elements
        JPanel spinAndTextPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        spinAndTextPanel.add(spinningDisc);
        spinAndTextPanel.add(scrollingLabel);

        // Create panel for controls
        JPanel playerControlsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        playerControlsPanel.add(controlButtonsPanel);

        // Add all components with better organization
        controlsWrapper.add(spinAndTextPanel);
        controlsWrapper.add(sliderPanel);
        controlsWrapper.add(playerControlsPanel);


        miniMusicPlayerPanel.add(controlsWrapper, BorderLayout.CENTER);

        return miniMusicPlayerPanel;
    }

    private void openMiniplayer() {

        for (WindowListener listener : MiniMusicPlayerGUI.getInstance().getWindowListeners()) {
            MiniMusicPlayerGUI.getInstance().removeWindowListener(listener);
        }

        MiniMusicPlayerGUI.getInstance().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Hide the window instead of disposing it
                MiniMusicPlayerGUI.getInstance().setVisible(false);
            }
        });

        // Update UI if there's a current song
        if (playerFacade.getCurrentSong() != null) {
            showPlaybackSlider();
            ThemeManager.getInstance().setThemeColors(ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor(), ThemeManager.getInstance().getAccentColor());
            // Check if player is paused or playing
            if (playerFacade.isPaused()) {
                enablePlayButtonDisablePauseButton();
            } else {
                enablePauseButtonDisablePlayButton();
            }

            updatePlaybackSlider(playerFacade.getCurrentSong());
            updateSpinningDisc(playerFacade.getCurrentSong());
            updateScrollingText(playerFacade.getCurrentSong());
            setPlaybackSliderValue(playerFacade.getCalculatedFrame());
        }

        // Show the player if it's not already visible
        if (!MiniMusicPlayerGUI.getInstance().isVisible()) {
            MiniMusicPlayerGUI.getInstance().setVisible(true);
        }

        // Bring to front and give focus
        MiniMusicPlayerGUI.getInstance().toFront();
        MiniMusicPlayerGUI.getInstance().requestFocus();
    }

    private void loadRecentSearches() {
        try {
            // Fetch the 10 most recent played songs
            java.util.List<SongDTO> recentSongs = CommonApiUtil.fetchRecentSearchHistory((AppConstant.RECENT_SEARCHED_SONG_LIMIT));

            if (!recentSongs.isEmpty()) {
                if (recentSearchDropdown == null) {
                    // Create the dropdown if it doesn't exist
                    recentSearchDropdown = new RecentSearchDropdown(
                            searchField,
                            recentSongs,
                            this::handleRecentSongSelected
                    );
                } else {
                    // Update the songs if the dropdown exists
                    recentSearchDropdown.updateSongs(recentSongs);
                }

                // Show the dropdown
                recentSearchDropdown.showPopup(searchField);
            }
        } catch (Exception e) {
            // Log the error but don't crash the application
            log.error("Error loading recent searches", e);

            // If there's an existing dropdown, ensure it's hidden
            if (recentSearchDropdown != null) {
                recentSearchDropdown.hidePopup();
            }
        }


    }

    private void handleRecentSongSelected(SongDTO song) {
        java.util.List<PlaylistDTO> playlists = CommonApiUtil.fetchAllPlaylists();

        Optional<PlaylistDTO> playlistWithSong = playlists.stream()
                .filter(playlist -> playlist.getSongs().stream()
                        .anyMatch(playlistSong -> playlistSong.getId().equals(song.getId())))
                .findFirst();

        playerFacade.setCurrentPlaylist(playlistWithSong.orElse(null));
        playerFacade.loadSong(song);
        searchField.setText(song.getSongTitle() + " - " + (song.getSongArtist() != null ? song.getSongArtist() : "Unknown"));


    }

    private void performSearch(String query) {
        if (NetworkChecker.isNetworkAvailable()) {
            // You can implement search functionality here
            // For example, search for songs matching the query and display them
            java.util.List<SongDTO> searchResults = CommonApiUtil.searchSongs(query);

            if (searchResults != null && !searchResults.isEmpty()) {
                // Show search results in a popup or another part of the UI
                SongDTO songDTO = searchResults.getFirst();
                //For MiniMusicPlayerGUI.
                java.util.List<PlaylistDTO> playlists = CommonApiUtil.fetchAllPlaylists();
                Optional<PlaylistDTO> playlistWithSong = playlists.stream()
                        .filter(playlist -> playlist.getSongs().stream()
                                .anyMatch(playlistSong -> playlistSong.getId().equals(songDTO.getId())))
                        .findFirst();

                playerFacade.setCurrentPlaylist(playlistWithSong.orElse(null));

                CommonApiUtil.logSearchHistory(songDTO.getId(), query);

                showSearchResults(searchResults);
            } else {
                GuiUtil.showInfoMessageDialog(this, "No songs found matching your search.");
            }
        } else {
            GuiUtil.showNetworkErrorDialog(this, "Internet connection is unavailable");
        }

    }

    private void showSearchResults(java.util.List<SongDTO> searchResults) {
        // Implement this to show search results
        // Could be a new panel in the main area, or a popup similar to recent searches
        // For now, just play the first result as an example
        playerFacade.loadSong(searchResults.getFirst());
    }

    private JPanel createLabelsPanel() {
        // Create labels panel
        JPanel labelsPanel = GuiUtil.createPanel(new BorderLayout());
        labelsPanel.setPreferredSize(new Dimension(getWidth(), 18));

        labelBeginning = GuiUtil.createLabel("00:00", Font.PLAIN, 18);
        labelBeginning.setVisible(false);

        labelEnd = GuiUtil.createLabel("00:00", Font.PLAIN, 18);
        labelEnd.setVisible(false);

        // Add labels to labelsPanel
        labelsPanel.add(labelBeginning, BorderLayout.WEST);
        labelsPanel.add(labelEnd, BorderLayout.EAST);
        labelsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, labelsPanel.getPreferredSize().height));

        return labelsPanel;
    }


    public void updatePlaybackSlider(SongDTO song) {
        // Set slider range based on total frames instead of milliseconds
        int totalFrames = song.getMp3File().getFrameCount();
        playbackSlider.setMaximum(totalFrames);

        labelEnd.setText(song.getSongLength());
        // Turn on or off this for octagon/ball thumb
        playbackSlider.setPaintLabels(false);
        // Improve slider performance
        playbackSlider.repaint();

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
        if (!scrollTimer.isRunning()) {
            scrollTimer.start();
        }
    }

    public void stopTextScrolling() {
        if (scrollTimer.isRunning()) {
            scrollTimer.stop();
        }
        scrollPosition = 0;
        scrollingLabel.repaint();
    }

    public void updateSongTimeLabel(int currentTimeInMilli) {
        int minutes = (currentTimeInMilli / 1000) / 60;
        int seconds = (currentTimeInMilli / 1000) % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        labelBeginning.setText(formattedTime);
    }

    public void updateScrollingText(SongDTO song) {
        String text = song.getSongTitle() + " - " + song.getSongArtist() + " ";
        scrollingLabel.setText(text);
        scrollingLabel.setVisible(true);
    }

    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
    }

    public void updateSpinningDisc(SongDTO song) {
        if (song.getSongImage() != null) {
            spinningDisc.setIcon(GuiUtil.createDiscImageIcon(song.getSongImage(), 50, 50, 7));
        } else {
            spinningDisc.setIcon(
                    GuiUtil.createDiscImageIcon(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH), 50, 50, 7));
        }
        int totalFrames = song.getMp3File().getFrameCount();
        playbackSlider.setMaximum(totalFrames);
    }

    // Methods to toggle play and pause buttons
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


    public String determineUserRole(Set<String> roles) {
        if (roles.contains(AppConstant.ADMIN_ROLE)) {
            return "Admin";
        } else if (roles.contains(AppConstant.PREMIUM_ROLE)) {
            return "Premium User";
        } else if (roles.contains(AppConstant.ARTIST_ROLE)) {
            return "Artist";
        } else {
            return "Free User";
        }
    }

    public void showPlaybackSlider() {
        spinningDisc.setVisible(true);
        playbackSlider.setVisible(true);
        controlButtonsPanel.setVisible(true);
        labelBeginning.setVisible(true);
        labelEnd.setVisible(true);
        startTextScrolling();
        startDiscSpinning();
    }

    public void hidePlaybackSlider() {
        spinningDisc.setVisible(false);
        playbackSlider.setVisible(false);
        controlButtonsPanel.setVisible(false);
        labelBeginning.setVisible(false);
        labelEnd.setVisible(false);
        stopTextScrolling();
        stopDiscSpinning();
    }

    private JPanel createLibraryPanel() {
        JPanel libraryNav = GuiUtil.createPanel(new BorderLayout());
        libraryNav.setBorder(GuiUtil.createTitledBorder("Library", TitledBorder.LEFT));
        libraryNav.setPreferredSize(new Dimension(230, getHeight()));
        libraryNav.setMaximumSize(new Dimension(230, getHeight()));


        // Content panel with MigLayout
        JPanel contentPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1, insets 0", "[fill]", "[]0[]"));
        contentPanel.setOpaque(false);

        // Create expandable section for Artists
        ExpandableCardPanel artistsCard = createExpandableCard("Artists", AppConstant.ARTIST_ICON_PATH, createArtistsPanel());

        // Create expandable section for Playlists
        ExpandableCardPanel playlistsCard = createExpandableCard("Playlists", AppConstant.PLAYLIST_ICON_PATH, createPlaylistsPanel());

        // Create expandable section for Liked Songs
        ExpandableCardPanel likedSongsCard = createExpandableCard("Liked", AppConstant.HEART_ICON_PATH, createLikedSongsPanel());

        // Create expandable section for Downloaded Songs
        ExpandableCardPanel downloadedSongsCard = createExpandableCard("Downloaded", AppConstant.DOWNLOAD_ICON_PATH, createDownloadedSongsPanel());

        // Add cards to main panel
        contentPanel.add(artistsCard, "growx");
        contentPanel.add(playlistsCard, "growx");
        contentPanel.add(likedSongsCard, "growx");
        contentPanel.add(downloadedSongsCard, "growx");

        // Add scroll support
        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(contentPanel);
        libraryNav.add(scrollPane, BorderLayout.CENTER);

        return libraryNav;
    }

    // Helper method to create expandable card
    private ExpandableCardPanel createExpandableCard(String title, String iconPath, JPanel contentPanel) {
        return new ExpandableCardPanel(title, iconPath, contentPanel);
    }

    private JPanel createArtistsPanel() {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());

        JPanel artistsListPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1, insets 0", "[fill]", "[]0[]"));

        loadFollowedArtists(artistsListPanel);

        panel.add(artistsListPanel, BorderLayout.CENTER);
        return panel;
    }


    private JPanel createPlaylistsPanel() {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());

        JPanel playlistsListPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1, insets 0", "[fill]", "[]0[]"));

        loadUserPlaylists(playlistsListPanel);
        panel.add(playlistsListPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLikedSongsPanel() {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());


        JPanel likedSongsListPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1, insets 0", "[fill]", "[]0[]"));

        loadLikedSongs(likedSongsListPanel);
        panel.add(likedSongsListPanel, BorderLayout.CENTER);


        return panel;
    }

    private JPanel createDownloadedSongsPanel() {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());


        JPanel downloadedSongsListPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1, insets 0", "[fill]", "[]0[]"));

        loadDownloadedSongs(downloadedSongsListPanel);

        panel.add(downloadedSongsListPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadUserPlaylists(JPanel container) {
        try {
            // Check for network connectivity
            if (!NetworkChecker.isNetworkAvailable()) {
                container.add(GuiUtil.createErrorLabel("Network is unavailable!"));
                return;
            }

            // Fetch user playlists
            java.util.List<PlaylistDTO> userPlaylists = CommonApiUtil.fetchPlaylistByUserId();

            if (userPlaylists.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No playlists found!"));
                return;
            }

            // Add each playlist to the panel
            for (PlaylistDTO playlist : userPlaylists) {
                container.add(createPlaylistPanel(playlist));
                container.add(Box.createVerticalStrut(5));
            }

        } catch (Exception e) {
            log.error("Failed to load user playlists", e);
            GuiUtil.createErrorLabel("Failed to load playlists!");
        }
    }


    private void loadFollowedArtists(JPanel container) {
        try {
            // Check for network connectivity
            if (!NetworkChecker.isNetworkAvailable()) {
                container.add(GuiUtil.createErrorLabel("Network unavailable!"));
                return;
            }

            java.util.List<ArtistDTO> followedArtists = CommonApiUtil.fetchFollowedArtists();

            if (followedArtists.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No Followed Artists!"));
                return;
            }

            // Add each artist to the panel
            for (ArtistDTO artist : followedArtists) {
                container.add(createArtistPanel(artist));
                // Add a small gap between artists
                container.add(Box.createVerticalStrut(5));
            }

        } catch (Exception e) {
            log.error("Failed to load followed artists", e);
            JLabel errorLabel = new JLabel("Failed to load artists");
            errorLabel.setForeground(ThemeManager.getInstance().getTextColor());
            errorLabel.setFont(FontUtil.getSpotifyFont(Font.ITALIC, 12));
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            errorLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
            container.add(errorLabel);
        }
    }

    private void loadLikedSongs(JPanel container) {
        try {
            // Check for network connectivity
            if (!NetworkChecker.isNetworkAvailable()) {
                container.add(GuiUtil.createErrorLabel("Network is unavailable!"));
                return;
            }

            // Fetch liked songs
            java.util.List<SongDTO> likedSongs = CommonApiUtil.findAllSongLikes()
                    .stream()
                    .map(SongLikesDTO::getSongDTO)
                    .toList();

            if (likedSongs.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No liked songs"));
                return;
            }

            // Create a virtual playlist for liked songs
            PlaylistDTO likedSongsPlaylist = new PlaylistDTO();
            likedSongsPlaylist.setName("Liked Songs");
            likedSongsPlaylist.setSongs(likedSongs);

            // Add the liked songs panel
            container.add(createLikedSongsCollectionPanel(likedSongsPlaylist));

            JPanel recentLabelPanel = GuiUtil.createPanel(new BorderLayout());
            recentLabelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            recentLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel recentLabel = GuiUtil.createLabel("Recently Liked", Font.BOLD, 14);
            recentLabelPanel.add(recentLabel, BorderLayout.WEST);

            container.add(recentLabelPanel);

            container.add(Box.createVerticalStrut(5));

            // Add up to 5 most recent liked songs
            for (int i = 0; i < Math.min(5, likedSongs.size()); i++) {
                container.add(createSongPanel(likedSongs.get(i)));
                container.add(Box.createVerticalStrut(5));
            }

        } catch (Exception e) {
            log.error("Failed to load liked songs", e);
            container.add(GuiUtil.createErrorLabel("Failed to load liked songs"));
        }
    }

    private void loadDownloadedSongs(JPanel container) {
        try {
            // Check for network connectivity
            if (!NetworkChecker.isNetworkAvailable()) {
                container.add(GuiUtil.createErrorLabel("Network is unavailable!"));
                return;
            }

            // Fetch liked songs
            java.util.List<SongDTO> downloadedSongs = CommonApiUtil.fetchUserDownloadedSongs();
            if (downloadedSongs.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No downloaded songs"));
                return;
            }


            JPanel recentLabelPanel = GuiUtil.createPanel(new BorderLayout());
            recentLabelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            recentLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel recentLabel = GuiUtil.createLabel("Recently Downloaded", Font.BOLD, 14);
            recentLabelPanel.add(recentLabel, BorderLayout.WEST);

            container.add(recentLabelPanel);

            container.add(Box.createVerticalStrut(5));

            for (SongDTO downloadedSong : downloadedSongs) {
                container.add(createSongPanel(downloadedSong));
                container.add(Box.createVerticalStrut(5));
            }

        } catch (Exception e) {
            log.error("Failed to load downloaded songs", e);
            container.add(GuiUtil.createErrorLabel("Failed to load downloaded songs"));
        }
    }

    private JPanel createLikedSongsCollectionPanel(PlaylistDTO likedSongsPlaylist) {
        JPanel likedSongsPanel = GuiUtil.createPanel(new BorderLayout(10, 0));

        likedSongsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f))
        ));
        likedSongsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel heartIconPanel = GuiUtil.createGradientHeartPanel(60, 60, 15, 30);

        // Create info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = GuiUtil.createLabel("Liked Songs", Font.BOLD, 14);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel countLabel = GuiUtil.createLabel(
                likedSongsPlaylist.getSongs().size() + " song" +
                        (likedSongsPlaylist.getSongs().size() != 1 ? "s" : ""),
                Font.PLAIN, 12
        );
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(titleLabel);
        infoPanel.add(countLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        likedSongsPanel.add(heartIconPanel, BorderLayout.WEST);
        likedSongsPanel.add(infoPanel, BorderLayout.CENTER);

        // Add hover effect
        GuiUtil.addHoverEffect(likedSongsPanel);

        // Add click handler
        likedSongsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Liked Songs collection clicked");
                playerFacade.setCurrentPlaylist(likedSongsPlaylist);
                if (!likedSongsPlaylist.getSongs().isEmpty()) {
                    playerFacade.loadSong(likedSongsPlaylist.getSongs().getFirst());
                }
            }
        });

        return likedSongsPanel;
    }

    private JPanel createPlaylistPanel(PlaylistDTO playlist) {
        JPanel playlistPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        playlistPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        playlistPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel coverLabel;
        if (playlist.getSongs() != null && !playlist.getSongs().isEmpty() &&
                playlist.getSongs().getFirst().getSongImage() != null) {
            coverLabel = GuiUtil.createRoundedCornerImageLabel(
                    playlist.getSongs().getFirst().getSongImage(), 15, 40, 40);
        } else {

            //Replace with actual default icon
            coverLabel = GuiUtil.createRoundedCornerImageLabel(
                    AppConstant.DEFAULT_COVER_PATH, 15, 40, 40);
        }
        // Create playlist info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = GuiUtil.createLabel(playlist.getName(), Font.BOLD, 12);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel countLabel = GuiUtil.createLabel(
                playlist.getSongs().size() + " song" + (playlist.getSongs().size() != 1 ? "s" : ""),
                Font.PLAIN, 10
        );
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(countLabel);

        playlistPanel.add(coverLabel, BorderLayout.WEST);
        playlistPanel.add(infoPanel, BorderLayout.CENTER);

        GuiUtil.addHoverEffect(playlistPanel);

        playlistPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Playlist clicked: {}", playlist.getName());
                playerFacade.setCurrentPlaylist(playlist);
                if (!playlist.getSongs().isEmpty()) {
                    playerFacade.loadSong(playlist.getSongs().getFirst());
                }
            }
        });

        return playlistPanel;
    }


    private JPanel createSongPanel(SongDTO song) {
        JPanel songPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        songPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Create song cover
        JLabel coverLabel = GuiUtil.createRoundedCornerImageLabel(song.getSongImage(), 15, 40, 40);

        // Create song info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = GuiUtil.createLabel(StringUtils.getTruncatedText(song.getSongTitle()), Font.BOLD, 12);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(
                song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist",
                Font.PLAIN, 10
        );
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(artistLabel);

        // Create action panel for heart icon
        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT));

        // Check if song is liked
        boolean isLiked = CommonApiUtil.checkSongLiked(song.getId());

        // Create heart button based on liked status
        JButton heartButton = GuiUtil.changeButtonIconColor(
                isLiked ? AppConstant.HEART_ICON_PATH : AppConstant.HEART_OUTLINE_ICON_PATH,
                20, 20
        );

        heartButton.addActionListener(e -> {
            boolean currentlyLiked = CommonApiUtil.checkSongLiked(song.getId());

            if (currentlyLiked) {
                // Unlike the song
                if (CommonApiUtil.deleteSongLikes(song.getId())) {
                    heartButton.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.HEART_OUTLINE_ICON_PATH,
                            ThemeManager.getInstance().getTextColor(),
                            20, 20
                    ));
                    GuiUtil.showSuccessMessageDialog(HomePage.this, "Removed from liked songs");
                } else {
                    GuiUtil.showErrorMessageDialog(HomePage.this, "Failed to unlike song");
                }
            } else {
                // Like the song
                if (CommonApiUtil.createSongLikes(song.getId())) {
                    heartButton.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.HEART_ICON_PATH,
                            ThemeManager.getInstance().getTextColor(),
                            20, 20
                    ));
                    GuiUtil.showSuccessMessageDialog(HomePage.this, "Added to liked songs");
                } else {
                    GuiUtil.showSuccessMessageDialog(HomePage.this, "Failed to like song");
                }
            }
        });

        actionPanel.add(heartButton);

        songPanel.add(coverLabel, BorderLayout.WEST);
        songPanel.add(infoPanel, BorderLayout.CENTER);
        songPanel.add(actionPanel, BorderLayout.EAST);

        // Add hover effect
        GuiUtil.addHoverEffect(songPanel);

        // Add click handler for playing the song
        songPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Only handle click if it's not on the heart button
                Component clickedComponent = SwingUtilities.getDeepestComponentAt(songPanel, e.getX(), e.getY());
                if (clickedComponent != heartButton) {
                    log.info("Song clicked: {}", song.getSongTitle());
                    playerFacade.loadSong(song);
                }
            }
        });

        return songPanel;
    }


    private JPanel createArtistPanel(ArtistDTO artist) {
        JPanel artistPanel = GuiUtil.createPanel(new BorderLayout(10, 0));

        artistPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        artistPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Create artist avatar
        JLabel avatarLabel = GuiUtil.createArtistAvatar(artist, 40);

        // Create artist info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.add(Box.createVerticalGlue());


        JLabel stageNameLabel = GuiUtil.createLabel(artist.getStageName(), Font.BOLD, 12);
        stageNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(stageNameLabel);

        infoPanel.add(Box.createVerticalGlue());


        artistPanel.add(avatarLabel, BorderLayout.WEST);
        artistPanel.add(infoPanel, BorderLayout.CENTER);

        // Add hover effect
        GuiUtil.addHoverEffect(artistPanel);

        return artistPanel;
    }


    private JLabel createUserProfileAvatar() {
        JMenuItem profileItem = GuiUtil.createMenuItem("Account");
        JMenuItem logoutItem = GuiUtil.createMenuItem("Log out");

        logoutItem.addActionListener(e -> logout());
        profileItem.addActionListener(e -> {/* navigate to profile */});

        return GuiUtil.createInteractiveUserAvatar(
                getCurrentUser(),
                40,
                ThemeManager.getInstance().getBackgroundColor(),
                ThemeManager.getInstance().getTextColor(),
                profileItem,
                logoutItem
        );
    }

    private void logout() {
        int option = GuiUtil.showConfirmMessageDialog(this, "Do you really want to log out MuseMoe? We'll miss you :(", "Logout confirm");
        if (option == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                hidePlaybackSlider();
                LoginPage loginPage = new LoginPage();
                UIManager.put("TitlePane.iconSize", new Dimension(24, 24));
                loginPage.getUsernameField().setText(getCurrentUser().getUsername());
                loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 512, 512).getImage());
                loginPage.setVisible(true);
            });

            if (MiniMusicPlayerGUI.getInstance() != null) {
                playerFacade.pauseSong();
                MiniMusicPlayerGUI.getInstance().setVisible(false);
            }
            ThemeManager.getInstance().setThemeColors(AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        }
    }

    private JPanel createCenterPanel() {
        CardLayout cardLayout = new CardLayout();
        JPanel centerPanel = GuiUtil.createPanel(cardLayout);
        centerPanel.setBorder(GuiUtil.createTitledBorder("Main", TitledBorder.LEFT));
        JPanel homePanel = createHomePanel();
        homePanel.setName("home");
        centerPanel.add(homePanel, "home");
        return centerPanel;
    }


    private JLabel createLogoLabel() {
        ImageIcon logoIcon = new ImageIcon(AppConstant.MUSE_MOE_LOGO_PATH);
        Image logoImage = logoIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return logoLabel;
    }


    private JPanel createHomePanel() {
        JPanel mainContent = GuiUtil.createPanel(new BorderLayout());

        JPanel backgroundPanel = GuiUtil.createPanel(new GridBagLayout());

        JLabel welcomeLabel = new JLabel("Welcome to Muse Moe", SwingConstants.CENTER);
        welcomeLabel.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 36));
        welcomeLabel.setForeground(AppConstant.TEXT_COLOR);

        backgroundPanel.add(welcomeLabel);
        mainContent.add(backgroundPanel, BorderLayout.CENTER);

        return mainContent;
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        // Title Bar
        GuiUtil.styleTitleBar(this, GuiUtil.darkenColor(backgroundColor, 0.1), textColor);

        // Gradient
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(backgroundColor, 0.1f),
                GuiUtil.darkenColor(backgroundColor, 0.4f),
                0.5f, 0.5f, 0.8f);

        //Update components colors
        GuiUtil.updatePanelColors(mainPanel, backgroundColor, textColor, accentColor);

        //Icons
        GuiUtil.changeIconColor(miniMuseMoeIcon, textColor);
        setIconImage(miniMuseMoeIcon.getImage());

        // Force repaint
        SwingUtilities.invokeLater(this::repaint);
    }


    private class ScrollingLabel extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Calculate dimensions relative to component size
            int height = getHeight();
            float centerY = height / 2.0f;

            FontMetrics fm = g2d.getFontMetrics();
            String text = getText();
            int textWidth = fm.stringWidth(text);

            // Draw text with responsive positioning
            g2d.setColor(getForeground());
            float textY = centerY + (float) fm.getAscent() / 2;
            g2d.drawString(text, -scrollPosition, textY);
            g2d.drawString(text, textWidth - scrollPosition, textY);

            g2d.dispose();
        }
    }


    @Override
    public void onPlayerEvent(PlayerEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.type()) {
                case SONG_LOADED -> {
                    SongDTO song = (SongDTO) event.data();
                    updatePlaybackSlider(song);
                    setPlaybackSliderValue(0);
                    updateSpinningDisc(song);
                    updateScrollingText(song);
                    showPlaybackSlider();
                    enablePauseButtonDisablePlayButton();
                }


                case PLAYBACK_STARTED -> enablePauseButtonDisablePlayButton();


                case PLAYBACK_PAUSED -> enablePlayButtonDisablePauseButton();


                case PLAYBACK_PROGRESS -> {
                    int[] data = (int[]) event.data();
                    if (!playbackSlider.getValueIsAdjusting()) {
                        setPlaybackSliderValue(data[0]);
                        updateSongTimeLabel(data[1]);
                    }
                }
                // Starting to show the playback slider in the Home Page.
                case HOME_PAGE_SLIDER_CHANGED -> showPlaybackSlider();

                case SLIDER_CHANGED -> setPlaybackSliderValue((int) event.data());

                case SLIDER_DRAGGING -> {
                    int[] data = (int[]) event.data();
                    if (playbackSlider.getValueIsAdjusting()) {
                        return;
                    }
                    setPlaybackSliderValue(data[0]);
                    updateSongTimeLabel(data[1]);
                }
            }
        });
    }

    @Override
    public void dispose() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
        MusicPlayerMediator.getInstance().unsubscribeFromPlayerEvents(this);
        super.dispose();
    }

    private UserDTO getCurrentUser() {
        return UserSessionManager.getInstance().getCurrentUser();
    }


}