package com.javaweb.view;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.*;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.NetworkChecker;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerFacade;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerMediator;
import com.javaweb.view.mini_musicplayer.event.PlayerEvent;
import com.javaweb.view.mini_musicplayer.event.PlayerEventListener;
import com.javaweb.view.panel.RecentSearchDropdown;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class HomePage extends JFrame implements PlayerEventListener, ThemeChangeListener {
    private static final Dimension FRAME_SIZE = new Dimension(1024, 768);
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private JLabel avatarLabel;
    private JLabel fullNameLabel;
    private JLabel spinningDisc;
    private JPanel controlButtonsPanel;
    private JSlider playbackSlider;
    private JButton prevButton;
    private JButton playButton;
    private JButton pauseButton;
    private JButton nextButton;
    private JPanel userInfoPanel;
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
    private float scrollPosition = 0.0f; // Change to float for smoother movement
    private JLabel dateLabel;
    private JPanel topPanel;
    private JPanel footerPanel;
    private JPanel combinedCenterPanel;
    private JPanel libraryPanel;
    private final JPanel mainPanel;
    private JLabel welcomeLabel;

    private final MusicPlayerFacade playerFacade;
    private JButton lookupIcon;
    private JButton homeIcon;
    private JTextField searchField;
    private JPanel searchBarWrapper;
    private JLabel helpLabel;
    private JPanel helpPanel;
    private JPanel rightPanel;
    private JPanel datePanel;
    private RecentSearchDropdown recentSearchDropdown;
    private JButton miniplayerButton;
    private JButton goBackButton;
    private JButton goForwardButton;
    private JLabel followedArtistsLabel;

    public HomePage() {
        initializeFrame();
        mainPanel = createMainPanel();
        add(mainPanel);
        spinningDisc.setIcon(
                GuiUtil.createDiscImageIcon(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH), 50, 50, 7));

        setVisible(true);

        playerFacade = MusicPlayerFacade.getInstance();

        MusicPlayerMediator.getInstance().subscribeToPlayerEvents(this);
        ThemeManager.getInstance().addThemeChangeListener(this);

    }


    private void initializeFrame() {
        //Set up for frame size
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(FRAME_SIZE);
        setResizable(true);
        //Doing nothing because I want user to confirm when logging out
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        GuiUtil.styleTitleBar(this, GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.12), ThemeManager.getInstance().getTextColor());

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

    }


    private JPanel createMainPanel() {
        JPanel mainPanel = GuiUtil.createPanel(new BorderLayout(0, 10));

        // Apply a radial gradient using the GuiUtil method instead of the linear gradient
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(AppConstant.BACKGROUND_COLOR, 0.1f),
                GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1f),
                0.5f, 0.5f, 0.8f);

        topPanel = createHeaderPanel();
        topPanel.setOpaque(false);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        combinedCenterPanel = GuiUtil.createPanel(new BorderLayout(10, 0));

        libraryPanel = createLibraryPanel();
        libraryPanel.setOpaque(false);
        combinedCenterPanel.add(libraryPanel, BorderLayout.WEST);

        centerPanel = createCenterPanel();
        centerPanel.setOpaque(false);
        combinedCenterPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(combinedCenterPanel, BorderLayout.CENTER);

        footerPanel = createMiniMusicPlayerPanel();
        footerPanel.setOpaque(false);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
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


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        headerPanel.setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Use GridBagLayout for the header content panel
        JPanel headerContentPanel = new JPanel(new GridBagLayout());
        headerContentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.weighty = 1;

        // ---------- LEFT SECTION (Date) ----------
        datePanel = new JPanel();
        datePanel.setOpaque(false);
        datePanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        dateLabel = GuiUtil.createLabel();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String currentDate = LocalDate.now().format(dateFormatter);
        dateLabel.setText(currentDate);

        datePanel.add(Box.createVerticalGlue());
        datePanel.add(dateLabel);
        datePanel.add(Box.createVerticalGlue());


        // Create a panel for date and navigation icons
        JPanel dateAndNavPanel = new JPanel();
        dateAndNavPanel.setLayout(new BoxLayout(dateAndNavPanel, BoxLayout.X_AXIS));
        dateAndNavPanel.setOpaque(false);
        dateAndNavPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Add date panel
        dateAndNavPanel.add(datePanel);
        dateAndNavPanel.add(Box.createHorizontalStrut(15));

        // Add navigation icons
        goBackButton = GuiUtil.changeButtonIconColor(AppConstant.GO_BACK_ICON_PATH, 20, 20);
        goBackButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        goBackButton.setToolTipText("Go Back");
        goBackButton.addActionListener(e -> {
            // Add navigation logic here
            // For example: navigateBack();
        });

        goForwardButton = GuiUtil.changeButtonIconColor(AppConstant.GO_FORWARD_ICON_PATH, 20, 20);
        goForwardButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        goForwardButton.setToolTipText("Go Forward");
        goForwardButton.addActionListener(e -> {
            // Add navigation logic here
            // For example: navigateForward();
        });

        // Add the buttons to the panel
        dateAndNavPanel.add(goBackButton);
        dateAndNavPanel.add(Box.createHorizontalStrut(5));
        dateAndNavPanel.add(goForwardButton);

        // ---------- CENTER SECTION (Search bar & Home icon) ----------
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        centerPanel.setOpaque(false);


        // Home icon with vertical centering
        homeIcon = GuiUtil.changeButtonIconColor(AppConstant.HOME_ICON_PATH, 20, 20);
        homeIcon.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel homeIconWrapper = new JPanel();
        homeIconWrapper.setLayout(new BoxLayout(homeIconWrapper, BoxLayout.Y_AXIS));
        homeIconWrapper.setOpaque(false);
        homeIconWrapper.add(Box.createVerticalGlue());
        homeIconWrapper.add(homeIcon);
        homeIconWrapper.add(Box.createVerticalGlue());

        centerPanel.add(homeIconWrapper);
        centerPanel.add(Box.createHorizontalStrut(10));

        // Search bar with vertical centering
        searchBarWrapper = GuiUtil.createPanel(new BorderLayout());
        searchBarWrapper.setPreferredSize(new Dimension(getWidth(), 40));
        searchBarWrapper.setMaximumSize(new Dimension(getWidth(), 40));
        searchBarWrapper.setMinimumSize(new Dimension(200, 40));
        searchBarWrapper.setAlignmentY(Component.CENTER_ALIGNMENT);
        searchBarWrapper.setBorder(GuiUtil.createCompoundBorder(2));
        lookupIcon = GuiUtil.changeButtonIconColor(AppConstant.LOOKUP_ICON_PATH, 20, 20);
        lookupIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        lookupIcon.setOpaque(false);
        lookupIcon.setFocusPainted(false);
        lookupIcon.setAlignmentY(Component.CENTER_ALIGNMENT);
        searchBarWrapper.add(lookupIcon, BorderLayout.WEST);

        searchField = GuiUtil.createLineInputField("What do you want to muse?...", 20);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        searchField.setOpaque(false);
        searchField.setForeground(GuiUtil.darkenColor(ThemeManager.getInstance().getTextColor(), 0.3f));
        searchField.setCaretColor(ThemeManager.getInstance().getTextColor());
        searchField.setAlignmentY(Component.CENTER_ALIGNMENT);

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("What do you want to muse?...")) {
                    searchField.setText("");
                    searchField.setForeground(ThemeManager.getInstance().getTextColor());
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
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

        lookupIcon.addActionListener(e -> {
            if (!searchField.getText().isEmpty() &&
                    !searchField.getText().equals("What do you want to muse?...")) {
                performSearch(searchField.getText());
            }
        });

        searchBarWrapper.add(searchField, BorderLayout.CENTER);
        centerPanel.add(searchBarWrapper);

        centerPanel.add(Box.createHorizontalGlue());
        // ---------- RIGHT SECTION (User info) ----------
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        miniplayerButton = GuiUtil.changeButtonIconColor(AppConstant.MINIPLAYER_ICON_PATH, 24, 24);
        miniplayerButton.setOpaque(false);
        miniplayerButton.setFocusPainted(false);
        miniplayerButton.setToolTipText("Open Music Player");
        miniplayerButton.addActionListener(e -> openMiniplayer());

        helpPanel = new JPanel();
        helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
        helpPanel.setBorder(GuiUtil.createTitledBorder("Help", TitledBorder.CENTER));
        helpPanel.setOpaque(false);

        helpPanel.setPreferredSize(new Dimension(80, 50));
        helpPanel.setMaximumSize(new Dimension(80, 50));

        helpLabel = GuiUtil.createLabel("Type ?", Font.BOLD, 12);
        helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpPanel.add(Box.createVerticalGlue());
        helpPanel.add(helpLabel);
        helpPanel.add(Box.createVerticalGlue());

        String userRole = determineUserRole(getCurrentUser().getRoles());
        fullNameLabel = GuiUtil.createLabel(getCurrentUser().getFullName() != null ?
                getCurrentUser().getFullName() + " - " + userRole : "??? - " + userRole);
        fullNameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        avatarLabel = createUserProfileAvatar();
        avatarLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        rightPanel.add(miniplayerButton);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(helpPanel);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(fullNameLabel);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(avatarLabel);

        // Add components to GridBagLayout
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        headerContentPanel.add(dateAndNavPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        headerContentPanel.add(centerPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        headerContentPanel.add(rightPanel, gbc);

        // Add the content panel to the header panel
        headerPanel.add(headerContentPanel, BorderLayout.CENTER);
        headerPanel.setBorder(GuiUtil.createTitledBorder("Search", TitledBorder.LEFT));

        return headerPanel;
    }

    private JPanel createMiniMusicPlayerPanel() {
        JPanel miniMusicPlayerPanel = new JPanel(new BorderLayout());
        miniMusicPlayerPanel.setBorder(GuiUtil.createTitledBorder("Playing", TitledBorder.LEFT));
        miniMusicPlayerPanel.setOpaque(true);

        // Create a more organized layout with FlowLayout center alignment
        JPanel controlsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        controlsWrapper.setOpaque(false);

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
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setOpaque(false);
        sliderPanel.setPreferredSize(new Dimension(300, 60));
        sliderPanel.setMaximumSize(new Dimension(300, 60));

        playbackSlider = new JSlider();
        playbackSlider.setPreferredSize(new Dimension(300, 40));
        playbackSlider.setMaximumSize(new Dimension(300, 40));
        playbackSlider.setBackground(AppConstant.HEADER_BACKGROUND_COLOR);
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

        controlButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        controlButtonsPanel.setBackground(AppConstant.HEADER_BACKGROUND_COLOR);
        controlButtonsPanel.setVisible(false);
        controlButtonsPanel.setOpaque(false);

        // Previous button
        prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, 20, 20);
        prevButton.addActionListener(e -> {
            playerFacade.prevSong();
        });

        // Play button
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 20, 20);
        playButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) {
                return;
            }
            playerFacade.playCurrentSong();
        });

        // Pause button
        pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, 20, 20);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) return;
            playerFacade.pauseSong();
        });

        // Next button
        nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, 20, 20);
        nextButton.addActionListener(e -> {
            playerFacade.nextSong();
        });

        // Add buttons to control panel
        controlButtonsPanel.add(prevButton);
        controlButtonsPanel.add(playButton);
        controlButtonsPanel.add(pauseButton);
        controlButtonsPanel.add(nextButton);


        // Create layout for player elements
        JPanel spinAndTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        spinAndTextPanel.setOpaque(false);
        spinAndTextPanel.add(spinningDisc);
        spinAndTextPanel.add(scrollingLabel);

        // Create panel for controls
        JPanel playerControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        playerControlsPanel.setOpaque(false);
        playerControlsPanel.add(controlButtonsPanel);

        // Add all components with better organization
        controlsWrapper.add(spinAndTextPanel);
        controlsWrapper.add(sliderPanel);
        controlsWrapper.add(playerControlsPanel);

        // Add padding to the panel
        miniMusicPlayerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
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
        JPanel labelsPanel = new JPanel(new BorderLayout());
        labelsPanel.setPreferredSize(new Dimension(getWidth(), 18));
        labelsPanel.setOpaque(false);

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
        libraryNav.setPreferredSize(new Dimension(200, getHeight()));

        // Create tabbed pane for different library sections
        JTabbedPane libraryTabs = new JTabbedPane(JTabbedPane.TOP);
        libraryTabs.setOpaque(true); // Make opaque to show background color
        libraryTabs.setBackground(ThemeManager.getInstance().getBackgroundColor());
        libraryTabs.setForeground(ThemeManager.getInstance().getTextColor());

        // Fix the content area background color
        UIManager.put("TabbedPane.contentAreaColor", ThemeManager.getInstance().getBackgroundColor());
        UIManager.put("TabbedPane.selected", ThemeManager.getInstance().getBackgroundColor());
        UIManager.put("TabbedPane.background", ThemeManager.getInstance().getBackgroundColor());
        UIManager.put("TabbedPane.tabAreaBackground", ThemeManager.getInstance().getBackgroundColor());
        UIManager.put("TabbedPane.unselectedBackground", GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.2f));

        // Customize the tab appearance
        libraryTabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.05f);
                lightHighlight = GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f);
                shadow = GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.2f);
                darkShadow = GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.3f);
                focus = ThemeManager.getInstance().getAccentColor();
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Override to paint content area with custom color
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(ThemeManager.getInstance().getBackgroundColor());

                Insets insets = tabPane.getInsets();
                int x = insets.left;
                int y = insets.top + calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                int w = tabPane.getWidth() - insets.right - insets.left;
                int h = tabPane.getHeight() - insets.bottom - y;

                g2d.fillRect(x, y, w, h);

                // Call the parent method to draw the borders if needed
                super.paintContentBorder(g, tabPlacement, selectedIndex);
            }

            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                // Override to paint tab area with custom color
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(ThemeManager.getInstance().getBackgroundColor());

                Insets insets = tabPane.getInsets();
                int x = insets.left;
                int y = insets.top;
                int w = tabPane.getWidth() - insets.right - insets.left;
                int h = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

                g2d.fillRect(x, y, w, h);

                // Call the parent method to draw the tabs
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }
        });

        // Tab 1: Artists
        JPanel artistsPanel = createArtistsPanel();

        // Tab 2: Playlists
        JPanel playlistsPanel = createPlaylistsPanel();

        // Tab 3: Liked Songs
        JPanel likedSongsPanel = createLikedSongsPanel();

        // Add tabs with custom icons
        libraryTabs.addTab("Artists", GuiUtil.createColoredIcon(AppConstant.ARTIST_ICON_PATH, 16), artistsPanel);
        libraryTabs.addTab("Playlists", GuiUtil.createColoredIcon(AppConstant.PLAYLIST_ICON_PATH, 16), playlistsPanel);
        libraryTabs.addTab("Liked", GuiUtil.createColoredIcon(AppConstant.HEART_ICON_PATH, 16), likedSongsPanel);

        libraryNav.add(libraryTabs, BorderLayout.CENTER);

        return libraryNav;
    }

    private JPanel createArtistsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        JPanel artistsListPanel = new JPanel();
        artistsListPanel.setLayout(new BoxLayout(artistsListPanel, BoxLayout.Y_AXIS));
        artistsListPanel.setOpaque(false);

        loadFollowedArtists(artistsListPanel);

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(artistsListPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPlaylistsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        JPanel playlistsListPanel = new JPanel();
        playlistsListPanel.setLayout(new BoxLayout(playlistsListPanel, BoxLayout.Y_AXIS));
        playlistsListPanel.setOpaque(false);

        loadUserPlaylists(playlistsListPanel);

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(playlistsListPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLikedSongsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        JPanel likedSongsListPanel = new JPanel();
        likedSongsListPanel.setLayout(new BoxLayout(likedSongsListPanel, BoxLayout.Y_AXIS));
        likedSongsListPanel.setOpaque(false);

        loadLikedSongs(likedSongsListPanel);

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(likedSongsListPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadUserPlaylists(JPanel container) {
        try {
            // Check for network connectivity
            if (!NetworkChecker.isNetworkAvailable()) {
                addNetworkErrorLabel(container);
                return;
            }

            // Fetch user playlists
            java.util.List<PlaylistDTO> userPlaylists = CommonApiUtil.fetchPlaylistByUserId();

            if (userPlaylists.isEmpty()) {
                addEmptyContentLabel(container, "No playlists found");
                return;
            }

            // Add each playlist to the panel
            for (PlaylistDTO playlist : userPlaylists) {
                container.add(createPlaylistPanel(playlist));
                container.add(Box.createVerticalStrut(5));
            }

        } catch (Exception e) {
            log.error("Failed to load user playlists", e);
            addErrorLabel(container, "Failed to load playlists");
        }
    }


    private void loadFollowedArtists(JPanel container) {
        try {
            // Check for network connectivity
            if (!NetworkChecker.isNetworkAvailable()) {
                JLabel errorLabel = new JLabel("Network unavailable");
                errorLabel.setForeground(ThemeManager.getInstance().getTextColor());
                errorLabel.setFont(FontUtil.getSpotifyFont(Font.ITALIC, 12));
                errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                errorLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
                container.add(errorLabel);
                return;
            }

            java.util.List<ArtistDTO> followedArtists = CommonApiUtil.fetchFollowedArtists();

            if (followedArtists.isEmpty()) {
                JLabel emptyLabel = new JLabel("No followed artists");
                emptyLabel.setForeground(ThemeManager.getInstance().getTextColor());
                emptyLabel.setFont(FontUtil.getSpotifyFont(Font.ITALIC, 12));
                emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
                container.add(emptyLabel);
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
                addNetworkErrorLabel(container);
                return;
            }

            // Fetch liked songs
            java.util.List<SongDTO> likedSongs = CommonApiUtil.findAllSongLikes()
                    .stream()
                    .map(SongLikesDTO::getSongDTO)
                    .toList();

            if (likedSongs.isEmpty()) {
                addEmptyContentLabel(container, "No liked songs");
                return;
            }

            // Create a virtual playlist for liked songs
            PlaylistDTO likedSongsPlaylist = new PlaylistDTO();
            likedSongsPlaylist.setName("Liked Songs");
            likedSongsPlaylist.setSongs(likedSongs);

            // Add the liked songs panel
            container.add(createLikedSongsCollectionPanel(likedSongsPlaylist));

            // Add some recently liked songs as individual entries
            JLabel recentLabel = GuiUtil.createLabel("Recently Liked", Font.BOLD, 12);
            recentLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
            recentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(recentLabel);

            // Add up to 5 most recent liked songs
            for (int i = 0; i < Math.min(5, likedSongs.size()); i++) {
                container.add(createSongPanel(likedSongs.get(i)));
                container.add(Box.createVerticalStrut(5));
            }

        } catch (Exception e) {
            log.error("Failed to load liked songs", e);
            addErrorLabel(container, "Failed to load liked songs");
        }
    }

    private JPanel createLikedSongsCollectionPanel(PlaylistDTO likedSongsPlaylist) {
        JPanel likedSongsPanel = new JPanel();
        likedSongsPanel.setLayout(new BorderLayout(10, 0));
        likedSongsPanel.setOpaque(false);
        likedSongsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f))
        ));
        likedSongsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Create liked songs icon (heart icon with gradient background)
        JPanel heartIconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0xE8128A),
                        getWidth(), getHeight(), new Color(0x26C6DA)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Draw heart icon
                Icon heartIcon = GuiUtil.createColoredIcon(AppConstant.HEART_ICON_PATH, Color.WHITE, 24, 24);
                heartIcon.paintIcon(this, g2d, (getWidth() - 24) / 2, (getHeight() - 24) / 2);

                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }
        };
        heartIconPanel.setOpaque(false);

        // Create info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

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
        addHoverEffect(likedSongsPanel);

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
        JPanel playlistPanel = new JPanel();
        playlistPanel.setLayout(new BorderLayout(10, 0));
        playlistPanel.setOpaque(false);
        playlistPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        playlistPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Create playlist cover (based on first song's image)
        JLabel coverLabel = createPlaylistCover(playlist, 40);

        // Create playlist info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

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

        // Add hover effect and click handler
        addHoverEffect(playlistPanel);

        playlistPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Playlist clicked: {}", playlist.getName());
                playerFacade.setCurrentPlaylist(playlist);
                // You might want to show a song selection dialog or just play the first song
                if (!playlist.getSongs().isEmpty()) {
                    playerFacade.loadSong(playlist.getSongs().getFirst());
                }
            }
        });

        return playlistPanel;
    }

    private JLabel createPlaylistCover(PlaylistDTO playlist, int size) {
        BufferedImage coverImage;

        try {
            // Try to use the first song's image as the cover
            if (!playlist.getSongs().isEmpty() && playlist.getSongs().getFirst().getSongImage() != null) {
                BufferedImage originalImage = playlist.getSongs().getFirst().getSongImage();
                coverImage = GuiUtil.createSmoothCircularAvatar(originalImage, 6);
            } else {
                // Create default cover with playlist icon
                coverImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = coverImage.createGraphics();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill background
                g2d.setColor(ThemeManager.getInstance().getAccentColor());
                g2d.fillRoundRect(0, 0, size, size, 6, 6);

                // Draw playlist icon
                g2d.setColor(ThemeManager.getInstance().getTextColor());
                int iconSize = (int) (size * 0.6);
                int iconX = (size - iconSize) / 2;
                int iconY = (size - iconSize) / 2;

                // Draw lines to suggest a playlist
                int lineWidth = (int) (iconSize * 0.7);
                int lineHeight = iconSize / 8;
                int lineX = iconX + (iconSize - lineWidth) / 2;
                int lineStartY = iconY + iconSize / 4;

                for (int i = 0; i < 3; i++) {
                    g2d.fillRoundRect(lineX, lineStartY + i * (lineHeight + 2),
                            lineWidth, lineHeight, 2, 2);
                }

                g2d.dispose();
            }
        } catch (Exception e) {
            // Create simple colored square as fallback
            coverImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = coverImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(ThemeManager.getInstance().getAccentColor());
            g2d.fillRoundRect(0, 0, size, size, 6, 6);
            g2d.dispose();
        }

        return new JLabel(new ImageIcon(coverImage));
    }

    private JLabel createSongCover(SongDTO song) {
        BufferedImage coverImage;

        try {
            if (song.getSongImage() != null) {
                coverImage = GuiUtil.createSmoothCircularAvatar(song.getSongImage(), 40);
            } else {
                // Create default cover with note icon
                coverImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = coverImage.createGraphics();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill background
                g2d.setColor(ThemeManager.getInstance().getAccentColor());
                g2d.fillRoundRect(0, 0, 40, 40, 6, 6);

                // Draw music note icon
                g2d.setColor(ThemeManager.getInstance().getTextColor());
                // Simple music note shape
                int centerX = 40 / 2;
                int centerY = 40 / 2;
                int noteWidth = 40 / 4;

                // Draw note head
                g2d.fillOval(centerX - noteWidth / 4, centerY + noteWidth / 2,
                        noteWidth / 2, noteWidth / 3);

                // Draw note stem
                g2d.fillRect(centerX + noteWidth / 4 - 1, centerY - noteWidth / 2,
                        2, noteWidth);

                g2d.dispose();
            }
        } catch (Exception e) {
            // Create simple colored square as fallback
            coverImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = coverImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(ThemeManager.getInstance().getTextColor());
            g2d.fillRoundRect(0, 0, 40, 40, 6, 6);
            g2d.dispose();
        }

        return new JLabel(new ImageIcon(coverImage));
    }

    private JPanel createSongPanel(SongDTO song) {
        JPanel songPanel = new JPanel();
        songPanel.setLayout(new BorderLayout(10, 0));
        songPanel.setOpaque(false);
        songPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        songPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Create song cover
        JLabel coverLabel = createSongCover(song);

        // Create song info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLabel = GuiUtil.createLabel(song.getSongTitle(), Font.BOLD, 12);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(
                song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist",
                Font.PLAIN, 10
        );
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(artistLabel);

        songPanel.add(coverLabel, BorderLayout.WEST);
        songPanel.add(infoPanel, BorderLayout.CENTER);

        // Add hover effect
        addHoverEffect(songPanel);

        // Add click handler
        songPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Song clicked: {}", song.getSongTitle());
                playerFacade.loadSong(song);
            }
        });

        return songPanel;
    }

    private void addHoverEffect(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f));
                panel.setOpaque(true);
                panel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setOpaque(false);
                panel.repaint();
            }
        });
    }

    private void addNetworkErrorLabel(JPanel container) {
        JLabel errorLabel = GuiUtil.createLabel("Network unavailable", Font.ITALIC, 12);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        container.add(errorLabel);
    }

    private void addEmptyContentLabel(JPanel container, String message) {
        JLabel emptyLabel = GuiUtil.createLabel(message, Font.ITALIC, 12);
        emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        container.add(emptyLabel);
    }

    private void addErrorLabel(JPanel container, String message) {
        JLabel errorLabel = GuiUtil.createLabel(message, Font.ITALIC, 12);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        container.add(errorLabel);
    }

    private JPanel createArtistPanel(ArtistDTO artist) {
        JPanel artistPanel = new JPanel();
        artistPanel.setLayout(new BorderLayout(10, 0));
        artistPanel.setOpaque(false);
        artistPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        artistPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Create artist avatar
        JLabel avatarLabel = GuiUtil.createArtistAvatar(artist, 40, ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor());

        // Create artist info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        infoPanel.add(Box.createVerticalGlue());


        JLabel stageNameLabel = GuiUtil.createLabel(artist.getStageName(), Font.BOLD, 12);
        stageNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(stageNameLabel);

        infoPanel.add(Box.createVerticalGlue());


        artistPanel.add(avatarLabel, BorderLayout.WEST);
        artistPanel.add(infoPanel, BorderLayout.CENTER);

        // Add hover effect
        artistPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                artistPanel.setBackground(GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f));
                artistPanel.setOpaque(true);
                artistPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                artistPanel.setOpaque(false);
                artistPanel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Artist clicked: {}", artist.getStageName());
            }
        });

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
        }
    }

    private JPanel createCenterPanel() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(GuiUtil.createTitledBorder("Main", TitledBorder.LEFT));
        centerPanel.setOpaque(false);

        JPanel homePanel = createHomePanel();
        homePanel.setOpaque(false);
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
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);  // Make this panel transparent

        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setOpaque(false);

        welcomeLabel = new JLabel("Welcome to Muse Moe", SwingConstants.CENTER);
        welcomeLabel.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 36));
        welcomeLabel.setForeground(AppConstant.TEXT_COLOR);

        backgroundPanel.add(welcomeLabel);
        mainContent.add(backgroundPanel, BorderLayout.CENTER);

        return mainContent;
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {

        GuiUtil.styleTitleBar(this, GuiUtil.lightenColor(backgroundColor, 0.12), textColor);

        // Update all control colors
        GuiUtil.changeButtonIconColor(nextButton);
        GuiUtil.changeButtonIconColor(prevButton);
        GuiUtil.changeButtonIconColor(playButton);
        GuiUtil.changeButtonIconColor(pauseButton);
        GuiUtil.changeButtonIconColor(miniplayerButton);
        GuiUtil.changeButtonIconColor(goBackButton);
        GuiUtil.changeButtonIconColor(goForwardButton);


        playbackSlider.setBackground(GuiUtil.lightenColor(backgroundColor, 0.2f));
        playbackSlider.setForeground(accentColor);

        // Force repaint of main panel to update the gradient
        Container contentPane = getContentPane();
        contentPane.repaint();

        // Update text colors
        dateLabel.setForeground(textColor);
        fullNameLabel.setForeground(textColor);

        scrollingLabel.setForeground(textColor);
        labelBeginning.setForeground(textColor);
        labelEnd.setForeground(textColor);

        // Update avatar if needed
        if (getCurrentUser().getAvatar() == null) {
            userInfoPanel.remove(avatarLabel);
            avatarLabel = createUserProfileAvatar();
            userInfoPanel.add(avatarLabel);
        }

        GuiUtil.changeButtonIconColor(homeIcon);
        GuiUtil.changeButtonIconColor(lookupIcon);

        searchField.setForeground(textColor);

        helpPanel.setBorder(GuiUtil.createTitledBorder("Help", TitledBorder.CENTER));

        helpLabel.setForeground(textColor);

        topPanel.setBorder(GuiUtil.createTitledBorder("Search", TitledBorder.LEFT));
        searchBarWrapper.setBorder(GuiUtil.createCompoundBorder(2));


        libraryPanel.setBorder(GuiUtil.createTitledBorder("Library", TitledBorder.LEFT));

        combinedCenterPanel.remove(libraryPanel);
        libraryPanel = createLibraryPanel();
        libraryPanel.setOpaque(false);
        combinedCenterPanel.add(libraryPanel, BorderLayout.WEST);
        combinedCenterPanel.revalidate();
        combinedCenterPanel.repaint();
        if (libraryPanel != null) {
            Component[] components = libraryPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JTabbedPane tabbedPane) {
                    tabbedPane.setBackground(backgroundColor);
                    tabbedPane.setForeground(textColor);

                    // Update UI Manager properties
                    UIManager.put("TabbedPane.contentAreaColor", backgroundColor);
                    UIManager.put("TabbedPane.selected", backgroundColor);
                    UIManager.put("TabbedPane.background", backgroundColor);
                    UIManager.put("TabbedPane.tabAreaBackground", backgroundColor);
                    UIManager.put("TabbedPane.unselectedBackground", GuiUtil.darkenColor(backgroundColor, 0.2f));

                    // Force UI manager to update
                    SwingUtilities.updateComponentTreeUI(tabbedPane);

                    // Update tab icons
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        String title = tabbedPane.getTitleAt(i);
                        Icon icon = null;
                        switch (title) {
                            case "Artists" ->
                                    icon = GuiUtil.createColoredIcon(AppConstant.ARTIST_ICON_PATH, textColor, 16, 16);
                            case "Playlists" ->
                                    icon = GuiUtil.createColoredIcon(AppConstant.PLAYLIST_ICON_PATH, textColor, 16, 16);
                            case "Liked" ->
                                    icon = GuiUtil.createColoredIcon(AppConstant.HEART_ICON_PATH, textColor, 16, 16);
                        }
                        if (icon != null) {
                            tabbedPane.setIconAt(i, icon);
                        }
                    }
                }
            }
        }

        centerPanel.setBorder(GuiUtil.createTitledBorder("Main", TitledBorder.LEFT));

        footerPanel.setBorder(GuiUtil.createTitledBorder("Playing", TitledBorder.LEFT));


        //Apply again the mainPanel
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(backgroundColor, 0.1f),
                GuiUtil.darkenColor(backgroundColor, 0.1f),
                0.5f, 0.5f, 0.8f);

        welcomeLabel.setForeground(textColor);
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