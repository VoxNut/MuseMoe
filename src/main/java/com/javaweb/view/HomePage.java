package com.javaweb.view;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.*;
import com.javaweb.utils.*;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.panel.*;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class HomePage extends JFrame implements PlayerEventListener, ThemeChangeListener {


    private static final Dimension FRAME_SIZE = new Dimension(1024, 768);

    private final MusicPlayerFacade playerFacade;

    private JLabel spinningDisc;
    private JPanel controlButtonsPanel;
    private JSlider playbackSlider;
    private Timer spinTimer;
    private JLabel labelBeginning;
    private JLabel labelEnd;

    private JLabel timerLabel;
    private JLabel statusLabel;
    private Timer loadingTimer;
    private long startTime;

    private double rotationAngle = 0.0;
    private static final double SPIN_SPEED = Math.PI / 60;
    private static final int TIMER_DELAY = 16; //
    private Timer scrollTimer;
    private JLabel scrollingLabel;
    private static final int SCROLL_DELAY = 16; // ~60 FPS (1000ms/60)
    private static final float SCROLL_SPEED = 0.5f;
    private float scrollPosition = 0.0f;

    private JPanel mainPanel;
    private JTextField searchField;
    private RecentSearchDropdown recentSearchDropdown;
    private final ImageIcon miniMuseMoeIcon;
    private JPanel loadingOverlay;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    private JPanel headerPanel;
    private JPanel combinedPanel;
    private JPanel miniMusicPlayerPanel;

    private JPanel libraryPanel;
    private JPanel centerCardPanel;

    private EnhancedSpectrumVisualizer visualizerPanel;
    private boolean visualizerActive = false;
    private boolean commitPanelActive = false;
    private boolean instructionPanelActive = false;


    public HomePage() {

        playerFacade = App.getBean(MusicPlayerFacade.class);
        ;

        initializeFrame();

        showLoadingOverlay("Loading MuseMoe...");

        playerFacade.subscribeToPlayerEvents(this);
        ThemeManager.getInstance().addThemeChangeListener(this);

        miniMuseMoeIcon = GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 30, 30);
        GuiUtil.changeIconColor(miniMuseMoeIcon, ThemeManager.getInstance().getTextColor());
        setIconImage(miniMuseMoeIcon.getImage());

        JPanel initialPanel = createInitialPanel();
        add(initialPanel);

        SwingUtilities.invokeLater(this::startProgressiveLoading);

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

    private JPanel createInitialPanel() {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());

        // Apply gradient background
        GuiUtil.setGradientBackground(panel,
                GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f),
                GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.4f),
                0.5f, 0.5f, 0.8f);

        // Create a simple welcome message
        JPanel centerPanel = GuiUtil.createPanel(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("Welcome to Muse Moe");
        welcomeLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 32));
        welcomeLabel.setForeground(ThemeManager.getInstance().getTextColor());

        JLabel loadingLabel = new JLabel("Loading your music experience...");
        loadingLabel.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 16));
        loadingLabel.setForeground(ThemeManager.getInstance().getTextColor());

        // Add components to panel
        JPanel textPanel = GuiUtil.createPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(welcomeLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(loadingLabel);

        centerPanel.add(textPanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void showLoadingOverlay(String message) {
        if (loadingOverlay == null) {
            loadingOverlay = GuiUtil.createPanel((new GridBagLayout()));
            loadingOverlay.setOpaque(true);
            loadingOverlay.setBackground(ThemeManager.getInstance().getBackgroundColor());

            JPanel contentPanel = GuiUtil.createPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            // Create a logo or app icon at the top
            ImageIcon logoIcon = GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 400, 400);
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            progressLabel = GuiUtil.createLabel(message, Font.BOLD, 40);
            progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Create a modern looking progress bar
            Color accentColor = ThemeManager.getInstance().getAccentColor();

            progressBar = GuiUtil.createStyledProgressBar(
                    ThemeManager.getInstance().getBackgroundColor(),
                    accentColor != null ? accentColor : new Color(52, 152, 219)
            );
            progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
            progressBar.setIndeterminate(false);
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);

            // Add timer label
            timerLabel = GuiUtil.createLabel("Time elapsed: 0.0s", Font.PLAIN, 16);
            timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Create and start the timer
            startTime = System.currentTimeMillis();
            loadingTimer = new Timer(100, e -> {
                long elapsed = System.currentTimeMillis() - startTime;
                double seconds = elapsed / 1000.0;
                timerLabel.setText(String.format("Time elapsed: %.1fs", seconds));
            });
            loadingTimer.start();

            // Add additional status text below progress bar
            statusLabel = GuiUtil.createLabel("Initializing application...", Font.ITALIC, 30);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add components to the content panel with proper spacing
            contentPanel.add(logoLabel);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(progressLabel);
            contentPanel.add(Box.createVerticalStrut(15));
            contentPanel.add(progressBar);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(timerLabel);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(statusLabel);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            loadingOverlay.add(contentPanel, gbc);

            JLayeredPane layeredPane = getLayeredPane();
            loadingOverlay.setBounds(0, 0, getWidth(), getHeight());
            layeredPane.add(loadingOverlay, JLayeredPane.POPUP_LAYER);

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    loadingOverlay.setBounds(0, 0, getWidth(), getHeight());
                }
            });
        } else {
            progressLabel.setText(message);
        }

        loadingOverlay.setVisible(true);
    }

    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            // Stop the timer when hiding the overlay
            if (loadingTimer != null && loadingTimer.isRunning()) {
                loadingTimer.stop();
            }
            loadingOverlay.setVisible(false);
        }
    }


    private void startProgressiveLoading() {
        // Create a worker thread for background loading
        new SwingWorker<Void, ProgressUpdate>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Step 1: Create the main structural panels
                    publish(new ProgressUpdate("Setting up the interface...", 10));
                    Thread.sleep(100);

                    mainPanel = createMainPanel();

                    // Step 2: Load header components
                    publish(new ProgressUpdate("Loading user profile...", 30));
                    Thread.sleep(100);
                    headerPanel = createHeaderPanel();

                    // Step 3: Load library components
                    publish(new ProgressUpdate("Loading your library...", 60));
                    Thread.sleep(100);
                    combinedPanel = createCombinedPanel();

                    // Step 4: Load player components
                    publish(new ProgressUpdate("Setting up music player...", 85));
                    Thread.sleep(100);
                    miniMusicPlayerPanel = createMiniMusicPlayerPanel();

                    // Complete loading
                    publish(new ProgressUpdate("Loading complete!", 100));
                    Thread.sleep(500);

                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void process(java.util.List<ProgressUpdate> updates) {
                if (!updates.isEmpty()) {
                    ProgressUpdate latestUpdate = updates.getLast();
                    statusLabel.setText(latestUpdate.message);
                    progressBar.setValue(latestUpdate.progressPercentage);
                    progressBar.setIndeterminate(latestUpdate.progressPercentage < 0);
                }
            }

            @Override
            protected void done() {
                try {
                    getContentPane().removeAll();
                    getContentPane().add(mainPanel);

                    // Final UI updates
                    mainPanel.add(headerPanel, BorderLayout.NORTH);
                    mainPanel.add(combinedPanel, BorderLayout.CENTER);
                    mainPanel.add(miniMusicPlayerPanel, BorderLayout.SOUTH);

                    // Make UI elements interactive
                    mainPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (searchField.hasFocus() && !searchField.contains(e.getPoint())) {
                                searchField.transferFocus();
                            }
                        }
                    });

                    // Hide loading overlay and refresh the UI
                    hideLoadingOverlay();
                    revalidate();
                    repaint();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private static class ProgressUpdate {
        String message;
        int progressPercentage;

        public ProgressUpdate(String message, int progressPercentage) {
            this.message = message;
            this.progressPercentage = progressPercentage;
        }
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = GuiUtil.createPanel(new BorderLayout(0, 10));

        // Apply a radial gradient using the GuiUtil method instead of the linear gradient
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f),
                GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.4f),
                0.5f, 0.5f, 0.8f);


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
        libraryPanel = createLibraryPanel();
        combinedPanel.add(libraryPanel, BorderLayout.WEST);
        combinedPanel.add(createCenterPanel(), BorderLayout.CENTER);
        return combinedPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        headerPanel.setBorder(GuiUtil.createTitledBorder("Search", TitledBorder.LEFT));

        JPanel headerContent = GuiUtil.createPanel(new MigLayout(
                "insets 5, fillx",
                "[left]20[grow, fill]20[right]", // Three columns: left, center (growing), right
                "[center]" // One row, centered vertically
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
        homeIcon.addActionListener(e -> toggleHome());
        homeIcon.setToolTipText("Home");
        centerPanel.add(homeIcon, "");

        // Search bar with wrapper for styling
        JPanel searchBarWrapper = GuiUtil.createPanel(new BorderLayout());
        searchBarWrapper.setBorder(GuiUtil.createCompoundBorder(2));
        searchBarWrapper.setPreferredSize(new Dimension(0, 40));

        // Search icon
        JButton lookupIcon = GuiUtil.changeButtonIconColor(AppConstant.LOOKUP_ICON_PATH, 20, 20);
        lookupIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        lookupIcon.addActionListener(e -> {
            if (!searchField.getText().isEmpty()
                    && !searchField.getText().equals("What do you want to muse?...")) {
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
                    if (!searchField.getText().isEmpty()
                            && !searchField.getText().equals("What do you want to muse?...")) {
                        performSearch(searchField.getText());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (recentSearchDropdown != null
                        && !searchField.getText().isEmpty()
                        && !searchField.getText().equals("What do you want to muse?...")) {
                    recentSearchDropdown.hidePopup();
                }
            }
        });

        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.getText().equals("What do you want to muse?...")
                        || searchField.getText().isEmpty()) {
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
        JLabel fullNameLabel = GuiUtil.createLabel(getCurrentUser().getFullName() != null
                ? getCurrentUser().getFullName() + " - " + userRole : "??? - " + userRole);
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

                if (playerFacade.isHavingAd()) {
                    return;
                }

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

                    playerFacade.notifySliderDragging(value, timeInMillis);
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
            if (playerFacade.isHavingAd()) {
                return;
            }
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
        SwingUtilities.invokeLater(
                () -> {
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
        );

    }

    private void loadRecentSearches() {
        try {
            // Fetch the 10 most recent played songs
            java.util.List<SongDTO> recentSongs = CommonApiUtil.fetchRecentSearchHistory((AppConstant.RECENT_SEARCHED_SONG_LIMIT));
            recentSongs.forEach(playerFacade::populateSongImage);
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
        java.util.List<PlaylistDTO> playlists = CommonApiUtil.fetchPlaylistByUserId();
        Optional<PlaylistDTO> playlistWithSong = playlists
                .stream()
                .filter(playlist -> playlist.getSongs()
                        .stream()
                        .anyMatch(playlistSong -> playlistSong.getId().equals(song.getId())))
                .findFirst();
        playerFacade.setCurrentPlaylist(playlistWithSong.orElse(null));
        playerFacade.loadSong(song);
        searchField.setText(song.getTitle() + " - " + (song.getSongArtist() != null ? song.getSongArtist() : "Unknown"));

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
                java.util.List<PlaylistDTO> playlists = CommonApiUtil.fetchPlaylistByUserId();
                Optional<PlaylistDTO> playlistWithSong = playlists.stream()
                        .filter(playlist -> playlist.getSongs()
                                .stream()
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
        long totalFrames = song.getFrame();
        playbackSlider.setMaximum((int) totalFrames);

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
        String text = song.getTitle() + " - " + song.getSongArtist() + " ";
        scrollingLabel.setText(text);
        scrollingLabel.setVisible(true);
    }

    public void setPlaybackSliderValue(long frame) {
        playbackSlider.setValue((int) frame);
    }

    public void updateSpinningDisc(SongDTO song) {
        if (song.getSongImage() != null) {
            spinningDisc.setIcon(GuiUtil.createDiscImageIcon(song.getSongImage(), 50, 50, 7));
        } else {
            spinningDisc.setIcon(
                    GuiUtil.createDiscImageIcon(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH), 50, 50, 7));
        }
        long totalFrames = song.getFrame();
        playbackSlider.setMaximum((int) totalFrames);
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

    private void refreshLikedSongsPanel() {
        ExpandableCardPanel likedSongsCard = null;

        for (Component component : GuiUtil.findComponentsByType(libraryPanel, ExpandableCardPanel.class)) {
            if (component instanceof ExpandableCardPanel card
                    && card.getTitle().equals("Liked")) {
                likedSongsCard = card;
                break;
            }
        }

        if (likedSongsCard != null) {
            JPanel updatedLikedSongsPanel = createLikedSongsPanel();

            likedSongsCard.setContent(updatedLikedSongsPanel);

            boolean wasExpanded = likedSongsCard.isExpanded();
            if (wasExpanded) {
                likedSongsCard.expandPanel();
            }
        }
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
            List<SongLikesDTO> songLikesDTOList = CommonApiUtil.findAllSongLikes();
            List<SongDTO> likedSongs = songLikesDTOList
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
            if (downloadedSongs == null || downloadedSongs.isEmpty()) {
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
                likedSongsPlaylist.getSongs().size() + " song"
                        + (likedSongsPlaylist.getSongs().size() != 1 ? "s" : ""),
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
        if (!playlist.getSongs().isEmpty()) {
            SongDTO songDTO = playlist.getSongs().getFirst();
            playerFacade.populateSongImage(songDTO);
            coverLabel = GuiUtil.createRoundedCornerImageLabel(songDTO.getSongImage(), 15, 40, 40);
        } else {
            coverLabel = GuiUtil.createRoundedCornerImageLabel(AppConstant.DEFAULT_COVER_PATH, 15, 40, 40);
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
        playerFacade.populateSongImage(song);
        JLabel coverLabel = GuiUtil.createRoundedCornerImageLabel(song.getSongImage(), 15, 40, 40);


        // Create song info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        Font titleFont = FontUtil.getSpotifyFont(Font.BOLD, 12);
        Font artistFont = FontUtil.getSpotifyFont(Font.PLAIN, 10);

        JLabel titleLabel = GuiUtil.createLabel(StringUtils.getTruncatedTextByWidth(song.getTitle(), titleFont), Font.BOLD, 12);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(
                song.getSongArtist() != null ? StringUtils.getTruncatedTextByWidth(song.getSongArtist(), artistFont) : "Unknown Artist",
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

                    GuiUtil.changeButtonIconColor(heartButton);

                    GuiUtil.showSuccessMessageDialog(HomePage.this, "Removed from liked songs");
                    refreshLikedSongsPanel();
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
                    refreshLikedSongsPanel();
                } else {
                    GuiUtil.showErrorMessageDialog(HomePage.this, "Failed to like song");
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
                    log.info("Song clicked: {}", song.getTitle());
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
        playerFacade.populateArtistProfile(artist);
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
        profileItem.addActionListener(e -> {/* navigate to profile */
        });
        playerFacade.populateUserProfile(getCurrentUser());
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
        centerCardPanel = GuiUtil.createPanel(cardLayout);
        centerCardPanel.setBorder(GuiUtil.createTitledBorder("Main", TitledBorder.LEFT));

        // Create the home panel
        JPanel homePanel = createHomePanel();
        homePanel.setName("home");
        centerCardPanel.add(homePanel, "home");

        // Create the visualizer panel
        visualizerPanel = new EnhancedSpectrumVisualizer(32);
        visualizerPanel.setName("visualizer");
        centerCardPanel.add(visualizerPanel, "visualizer");

        // Create the commit panel
        CommitPanel commitPanel = new CommitPanel();
        commitPanel.setName("commits");
        centerCardPanel.add(commitPanel, "commits");

        InstructionPanel instructionPanel = new InstructionPanel();
        instructionPanel.setName("instructions");
        centerCardPanel.add(instructionPanel, "instructions");

        // Add global keyboard listener for toggling between views
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                // Shift+V to toggle visualizer
                if (e.getKeyCode() == KeyEvent.VK_V && e.isShiftDown()) {
                    toggleVisualizer();
                    return true;
                }

                // Shift+C to toggle commit view
                if (e.getKeyCode() == KeyEvent.VK_C && e.isShiftDown()) {
                    toggleCommitPanel();
                    return true;
                }

                if (e.getKeyCode() == KeyEvent.VK_B && visualizerActive) {
                    toggleVisualizerBands();
                    return true;
                }

                if (e.getKeyCode() == KeyEvent.VK_SLASH && e.isShiftDown()) {
                    toggleInstructionPanel();
                    return true;
                }
            }
            return false;
        });

        return centerCardPanel;
    }

    private void toggleCommitPanel() {
        commitPanelActive = !commitPanelActive;

        if (centerCardPanel == null) {
            return;
        }

        CardLayout cardLayout = (CardLayout) centerCardPanel.getLayout();

        if (commitPanelActive) {
            if (visualizerActive) {
                visualizerActive = false;
                playerFacade.notifyToggleCava(false);
            }
            if (instructionPanelActive) {
                instructionPanelActive = false;
            }
            cardLayout.show(centerCardPanel, "commits");
            log.info("Commit panel activated");
            GuiUtil.showToast(this, "Commit panel activated");
        } else {
            cardLayout.show(centerCardPanel, "home");
            log.info("Commit panel deactivated");
            GuiUtil.showToast(this, "Commit panel deactivated");
        }
    }

    private void toggleInstructionPanel() {
        instructionPanelActive = !instructionPanelActive;

        if (centerCardPanel == null) {
            return;
        }

        CardLayout cardLayout = (CardLayout) centerCardPanel.getLayout();

        if (instructionPanelActive) {
            if (visualizerActive) {
                visualizerActive = false;
                playerFacade.notifyToggleCava(false);
            }
            if (commitPanelActive) {
                commitPanelActive = false;
            }

            cardLayout.show(centerCardPanel, "instructions");
            log.info("Instruction panel activated");
            GuiUtil.showToast(this, "Help panel activated");
        } else {
            cardLayout.show(centerCardPanel, "home");
            log.info("Instruction panel deactivated");
            GuiUtil.showToast(this, "Help panel deactivated");
        }
    }

    private void toggleHome() {
        visualizerActive = false;
        commitPanelActive = false;
        instructionPanelActive = false;
        CardLayout cardLayout = (CardLayout) centerCardPanel.getLayout();
        cardLayout.show(centerCardPanel, "home");
        GuiUtil.showToast(this, "Home activated");
    }

    private void toggleVisualizer() {
        visualizerActive = !visualizerActive;

        if (centerCardPanel == null) {
            log.error("Center card panel is null");
            return;
        }

        CardLayout cardLayout = (CardLayout) centerCardPanel.getLayout();

        if (visualizerActive) {
            // Turn off commit panel if it's active
            if (commitPanelActive) {
                commitPanelActive = false;
            }
            if (instructionPanelActive) {
                instructionPanelActive = false;
            }

            playerFacade.notifyToggleCava(true);
            cardLayout.show(centerCardPanel, "visualizer");
            log.info("Visualizer activated");
            GuiUtil.showToast(this, "Visualizer activated");
        } else {
            cardLayout.show(centerCardPanel, "home");
            GuiUtil.showToast(this, "Visualizer deactivated");
            log.info("Visualizer deactivated");
        }
    }

    private void toggleVisualizerBands() {
        if (visualizerPanel == null) {
            return;
        }

        int[] bandOptions = {10, 16, 24, 32, 48, 64, 98, 128, 256, 512};
        int currentBands = visualizerPanel.getNumberOfBands();

        // Find next band option
        int nextBandIndex = 0;
        for (int i = 0; i < bandOptions.length; i++) {
            if (currentBands < bandOptions[i]) {
                nextBandIndex = i;
                break;
            }
        }

        // Cycle through options
        int newBands = bandOptions[nextBandIndex];
        visualizerPanel.setNumberOfBands(newBands);

        // Show feedback
        GuiUtil.showToast(this, "Visualizer: " + newBands + " bands");
    }

    private JPanel createHomePanel() {
        JPanel mainContent = GuiUtil.createPanel(new MigLayout(
                "fill, insets 10",
                "[grow]",
                "[top][grow]"
        ));

        String[] welcomeMessages = AppConstant.WELCOME_MESSAGE;
        int randomIndex = (int) (Math.random() * welcomeMessages.length);
        String selectedMessage = welcomeMessages[randomIndex];
        String asciiArt = generateFigletArt(selectedMessage);

        JTextArea asciiArtTextArea = GuiUtil.createTextArea(asciiArt, Font.BOLD, 12);

        JPanel asciiArtPanel = GuiUtil.createPanel(new MigLayout("fill, insets 0"));
        asciiArtPanel.add(asciiArtTextArea, "left, top, growx");

        mainContent.add(asciiArtPanel, "growx, wrap");

        return mainContent;
    }

    private String generateFigletArt(String text) {
        try {
            Process process = getProcess(text);

            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            return output.toString();
        } catch (Exception e) {
            log.error("Error generating ASCII art", e);
            return "";
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

        Process process = processBuilder.start();
        return process;
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

                case SONG_LIKED_CHANGED -> refreshLikedSongsPanel();

                case TOGGLE_CAVA -> {
                    if (visualizerActive && visualizerPanel != null) {
                        boolean isToggle = (boolean) event.data();
                        visualizerPanel.toggleCAVA(isToggle);
                    }
                }

            }
        });
    }

    @Override
    public void dispose() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
        playerFacade.unsubscribeFromPlayerEvents(this);
        super.dispose();
    }

    private UserDTO getCurrentUser() {
        return UserSessionManager.getInstance().getCurrentUser();
    }

}
