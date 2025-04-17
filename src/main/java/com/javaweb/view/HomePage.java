package com.javaweb.view;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerFacade;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerMediator;
import com.javaweb.view.mini_musicplayer.event.PlayerEvent;
import com.javaweb.view.mini_musicplayer.event.PlayerEventListener;
import com.javaweb.view.panel.RecentSearchDropdown;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private Color backgroundColor = AppConstant.BACKGROUND_COLOR;
    private Color textColor = AppConstant.TEXT_COLOR;
    private Color accentColor = GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1);
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

    public HomePage() throws IOException {
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
        GuiUtil.styleTitleBar(this, GuiUtil.lightenColor(backgroundColor, 0.12), textColor);

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
        JPanel mainPanel = GuiUtil.createPanel(new BorderLayout());

        // Apply a radial gradient using the GuiUtil method instead of the linear gradient
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(AppConstant.BACKGROUND_COLOR, 0.1f),
                GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1f),
                0.5f, 0.5f, 0.8f);

        topPanel = createHeaderPanel();
        topPanel.setOpaque(false);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        combinedCenterPanel = GuiUtil.createPanel(new BorderLayout());

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
        headerPanel.setBackground(backgroundColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Use GridBagLayout for the header content panel
        JPanel headerContentPanel = new JPanel(new GridBagLayout());
        headerContentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.weighty = 1.0; // Căn giữa theo chiều dọc

        // ---------- LEFT SECTION (Date) ----------
        datePanel = new JPanel();
        datePanel.setOpaque(false);
        datePanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        datePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        dateLabel = new JLabel();
        dateLabel.setForeground(textColor);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String currentDate = LocalDate.now().format(dateFormatter);
        dateLabel.setText(currentDate);

        datePanel.add(Box.createVerticalGlue());
        datePanel.add(dateLabel);
        datePanel.add(Box.createVerticalGlue());

        // ---------- CENTER SECTION (Search bar & Home icon) ----------
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setAlignmentY(Component.CENTER_ALIGNMENT);


        // Home icon with vertical centering
        homeIcon = GuiUtil.changeButtonIconColor(AppConstant.HOME_ICON_PATH, textColor, 20, 20);
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
        searchBarWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        searchBarWrapper.setPreferredSize(new Dimension(600, 40));
        searchBarWrapper.setMaximumSize(new Dimension(600, 40));
        searchBarWrapper.setMinimumSize(new Dimension(200, 40));
        searchBarWrapper.setAlignmentY(Component.CENTER_ALIGNMENT);

        lookupIcon = GuiUtil.changeButtonIconColor(AppConstant.LOOKUP_ICON_PATH, textColor, 20, 20);
        lookupIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        lookupIcon.setOpaque(false);
        lookupIcon.setFocusPainted(false);
        lookupIcon.setAlignmentY(Component.CENTER_ALIGNMENT);
        searchBarWrapper.add(lookupIcon, BorderLayout.WEST);

        searchField = GuiUtil.createLineInputField("What do you want to muse?...", 20);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        searchField.setOpaque(false);
        searchField.setForeground(GuiUtil.darkenColor(textColor, 0.3f));
        searchField.setCaretColor(textColor);
        searchField.setAlignmentY(Component.CENTER_ALIGNMENT);

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("What do you want to muse?...")) {
                    searchField.setText("");
                    searchField.setForeground(textColor);
                    loadRecentSearches();
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty() || searchField.getText().trim().isEmpty()) {
                    searchField.setText("What do you want to muse?...");
                    searchField.setForeground(GuiUtil.darkenColor(textColor, 0.3f));
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
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        miniplayerButton = GuiUtil.changeButtonIconColor(AppConstant.MINIPLAYER_ICON_PATH, textColor, 24, 24);
        miniplayerButton.setOpaque(false);
        miniplayerButton.setFocusPainted(false);
        miniplayerButton.setToolTipText("Open Music Player");
        miniplayerButton.addActionListener(e -> openMiniplayer());

        helpPanel = new JPanel();
        helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
        helpPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                "Help",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                FontUtil.getSpotifyFont(Font.BOLD, 12),
                textColor
        ));
        helpPanel.setOpaque(false);
        helpPanel.setPreferredSize(new Dimension(80, 43));
        helpPanel.setMaximumSize(new Dimension(80, 43));

        helpLabel = GuiUtil.createLabel("Type ?", Font.BOLD, 12);
        helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpPanel.add(Box.createVerticalGlue());
        helpPanel.add(helpLabel);
        helpPanel.add(Box.createVerticalGlue());

        String userRole = determineUserRole(getCurrentUser().getRoles());
        fullNameLabel = GuiUtil.createLabel(getCurrentUser().getFullName() != null ?
                getCurrentUser().getFullName() + " - " + userRole : "??? - " + userRole);
        fullNameLabel.setForeground(textColor);
        fullNameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        avatarLabel = createUserAvatar();
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
        headerContentPanel.add(datePanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        headerContentPanel.add(centerPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0; // Không co giãn cho rightPanel
        headerContentPanel.add(rightPanel, gbc);

        // Add the content panel to the header panel
        headerPanel.add(headerContentPanel, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        return headerPanel;
    }

    private JPanel createMiniMusicPlayerPanel() {
        JPanel miniMusicPlayerPanel = new JPanel(new BorderLayout());
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
        prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        prevButton.addActionListener(e -> {
            playerFacade.prevSong();
        });

        // Play button
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        playButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) {
                return;
            }
            playerFacade.playCurrentSong();
        });

        // Pause button
        pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) return;
            playerFacade.pauseSong();
        });

        // Next button
        nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
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
            java.util.List<SongDTO> recentSongs = CommonApiUtil.fetchRecentPlayHistory(AppConstant.RECENT_SEARCHED_SONG_LIMIT);

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
        // Play the selected song
        playerFacade.loadSong(song);
        /*
         * getBufferedImage from the chosen song and extract colors from it.
         */
        searchField.setText(song.getSongTitle() + " - " + song.getSongArtist());

        Color[] themeColors = GuiUtil.extractThemeColors(song.getSongImage());

        // Apply the extracted colors to the UI
        ThemeManager.getInstance().setThemeColors(
                themeColors[0], // backgroundColor
                themeColors[1], // textColor
                themeColors[2]  // accentColor
        );
    }

    private void performSearch(String query) {
        // You can implement search functionality here
        // For example, search for songs matching the query and display them
        java.util.List<SongDTO> searchResults = CommonApiUtil.searchSongs(query);

        if (searchResults != null && !searchResults.isEmpty()) {
            // Show search results in a popup or another part of the UI
            showSearchResults(searchResults);
        } else {
            GuiUtil.showInfoMessageDialog(this, "No songs found matching your search.");
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

        labelBeginning = GuiUtil.createSpotifyFontLabel("00:00", Font.PLAIN, 18);
        labelBeginning.setForeground(textColor);
        labelBeginning.setVisible(false);

        labelEnd = GuiUtil.createSpotifyFontLabel("00:00", Font.PLAIN, 18);
        labelEnd.setForeground(textColor);
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
        JPanel libraryNav = new JPanel();
        libraryNav.setLayout(new BoxLayout(libraryNav, BoxLayout.Y_AXIS));
        libraryNav.setBackground(AppConstant.BACKGROUND_COLOR);
        libraryNav.setPreferredSize(new Dimension(100, getHeight()));
        return libraryNav;
    }


    private JLabel createUserAvatar() {
        BufferedImage originalImage = null;
        boolean useDefaultAvatar = false;

        try {
            if (getCurrentUser().getAvatar() != null) {
                originalImage = ImageIO.read(new File(getCurrentUser().getAvatar().getFileUrl()));
            } else {
                useDefaultAvatar = true;
            }
        } catch (IOException e) {
            useDefaultAvatar = true;
        }

        int size = 40;

        BufferedImage avatarImage;
        if (useDefaultAvatar) {
            avatarImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = avatarImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color bgColor = this.backgroundColor != null ? this.backgroundColor : AppConstant.NAVBAR_BACKGROUND_COLOR;
            Color textColor = this.textColor != null ? this.textColor : AppConstant.TEXT_COLOR;

            g2d.setColor(bgColor);
            g2d.fillOval(0, 0, size, size);

            String initial = getCurrentUser().getUsername() != null && !getCurrentUser().getUsername().isEmpty() ?
                    getCurrentUser().getUsername().substring(0, 1).toUpperCase() : "U";

            float fontSize = (float) size * 0.4f;
            g2d.setColor(textColor);
            g2d.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, fontSize));

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(initial);
            int textHeight = fm.getAscent();

            int x = (size - textWidth) / 2;
            int y = (size - textHeight) / 2 + fm.getAscent();

            g2d.drawString(initial, x, y);
            g2d.dispose();
        } else {
            // Use the actual user image and make it circular
            avatarImage = GuiUtil.createSmoothCircularAvatar(originalImage, size);
        }

        avatarLabel = new JLabel(new ImageIcon(avatarImage));
        avatarLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Ensure the label size matches the image size
        avatarLabel.setPreferredSize(new Dimension(size, size));

        JPopupMenu popupMenu = GuiUtil.createPopupMenu(backgroundColor, textColor);
        JMenuItem profileItem = GuiUtil.createMenuItem("Account");
        JMenuItem logoutItem = GuiUtil.createMenuItem("Log out");


        popupMenu.add(profileItem);
        popupMenu.add(logoutItem);

        logoutItem.addActionListener(e -> {
            logout();
        });
        profileItem.addActionListener(e -> {
//            navigateTo("profile");
        });

        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        return avatarLabel;
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
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;
        GuiUtil.styleTitleBar(this, GuiUtil.lightenColor(backgroundColor, 0.12), textColor);

        // Update all control colors
        GuiUtil.changeButtonIconColor(nextButton, textColor);
        GuiUtil.changeButtonIconColor(prevButton, textColor);
        GuiUtil.changeButtonIconColor(playButton, textColor);
        GuiUtil.changeButtonIconColor(pauseButton, textColor);
        GuiUtil.changeButtonIconColor(miniplayerButton, textColor);


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
            avatarLabel = createUserAvatar();
            userInfoPanel.add(avatarLabel);
            userInfoPanel.revalidate();
            userInfoPanel.repaint();
        }
        GuiUtil.changeButtonIconColor(homeIcon, textColor);
        GuiUtil.changeButtonIconColor(lookupIcon, textColor);
        searchBarWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        searchField.setForeground(textColor);
        helpPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                "Help",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                FontUtil.getSpotifyFont(Font.BOLD, 12),
                textColor
        ));
        helpLabel.setForeground(textColor);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        datePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

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