package com.javaweb.view;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.enums.ActivePanel;
import com.javaweb.enums.PlaylistSourceType;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.*;
import com.javaweb.utils.*;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.navigation.NavigationDestination;
import com.javaweb.view.navigation.NavigationListener;
import com.javaweb.view.navigation.NavigationManager;
import com.javaweb.view.navigation.NavigationManager.NavigationItem;
import com.javaweb.view.panel.*;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class HomePage extends JFrame implements PlayerEventListener, ThemeChangeListener, NavigationListener {


    private static final Dimension FRAME_SIZE = new Dimension(1024, 768);

    private MusicPlayerFacade playerFacade;

    private JLabel timerLabel;
    private JLabel statusLabel;
    private Timer loadingTimer;
    private long startTime;

    private JPanel mainPanel;
    private JTextField searchField;
    private RecentSearchDropdown recentSearchDropdown;
    private final ImageIcon miniMuseMoeIcon;
    private JPanel loadingOverlay;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JLabel fullNameLabel;

    private JPanel headerPanel;
    private JPanel combinedPanel;
    private JPanel miniMusicPlayerPanel;

    private JPanel libraryPanel;
    private JPanel centerCardPanel;

    private EnhancedSpectrumVisualizer visualizerPanel;

    private ActivePanel activePanel = ActivePanel.HOME;
    private boolean miniplayerActive = false;


    private JButton goBackButton;
    private JButton goForwardButton;
    private final NavigationManager navigationManager;


    // Keyboard event dispatch
    private KeyEventDispatcher keyEventDispatcher;
    private AWTEventListener globalClickListener;
    private JButton upgradeButton;

    // Search event
    private Timer searchDelayTimer;
    private final int SEARCH_DELAY = 500; // milliseconds delay after typing

    private String userRole;

    private ArtistProfilePanel artistProfilePanel;
    private AlbumViewPanel albumViewPanel;
    private SongDetailsPanel songDetailsPanel;
    private QueuePanel queuePanel;
    private CommitPanel commitPanel;
    private InstructionPanel instructionPanel;
    private SearchResultsPanel searchResultsPanel;
    private ArtistUploadPanel artistUploadPanel;
    private AdminStatisticsPanel adminStatisticsPanel;
    private AccountSettingsPanel accountSettingsPanel;
    private HomePanel homePanel;

    private UserDTO currentUser;


    private AsyncImageLabel avatarLabel;

    public HomePage() {

        navigationManager = NavigationManager.getInstance();
        navigationManager.addNavigationListener(this);

        navigationManager.clearHistory();

        navigationManager.navigateTo(NavigationDestination.HOME, null);

        playerFacade = App.getBean(MusicPlayerFacade.class);

        currentUser = UserSessionManager.getInstance().getCurrentUser();

        MiniMusicPlayerGUI.getInstance();

        initializeFrame();

        showLoadingOverlay("Loading MuseMoe...");

        playerFacade.subscribeToPlayerEvents(this);
        ThemeManager.getInstance().addThemeChangeListener(this);

        miniMuseMoeIcon = GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 30, 30);
        GuiUtil.changeIconColor(miniMuseMoeIcon, ThemeManager.getInstance().getTextColor());
        setIconImage(miniMuseMoeIcon.getImage());

        JPanel initialPanel = createInitialPanel();
        add(initialPanel);

        SwingUtilities.invokeLater(() -> {
            startProgressiveLoading();
            setupGlobalFocusManagement();
        });

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

            progressBar = GuiUtil.createStyledProgressBar();
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


                    hideLoadingOverlay();
                    revalidate();
                    repaint();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void setupGlobalFocusManagement() {
        AWTEventListener globalClickListener = event -> {
            if (event instanceof MouseEvent mouseEvent) {
                if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
                    Component clickedComponent = SwingUtilities.getDeepestComponentAt(
                            mouseEvent.getComponent(),
                            mouseEvent.getX(),
                            mouseEvent.getY()
                    );

                    if (searchField != null &&
                            searchField.hasFocus() &&
                            !(clickedComponent instanceof JTextField) &&
                            !(clickedComponent instanceof JTextArea) &&
                            !(clickedComponent instanceof JEditorPane)) {

                        SwingUtilities.invokeLater(() -> mainPanel.requestFocusInWindow());
                    }
                }
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(
                globalClickListener,
                AWTEvent.MOUSE_EVENT_MASK
        );

        this.globalClickListener = globalClickListener;
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
            public void mousePressed(MouseEvent e) {
                if (searchField.hasFocus()) {
                    SwingUtilities.invokeLater(mainPanel::requestFocusInWindow);
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
                "[left]20[grow, fill]20[right]",
                "[center]"
        ));

        // ---------- LEFT SECTION (Date) ----------
        JPanel leftPanel = GuiUtil.createPanel(new MigLayout("insets 0", "[]5[]5[]", "[center]"));

        // Date display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        JLabel dateLabel = GuiUtil.createLabel(LocalDate.now().format(dateFormatter), Font.PLAIN, 16);
        leftPanel.add(dateLabel, "");

        // Navigation buttons
        goBackButton = GuiUtil.changeButtonIconColor(AppConstant.GO_BACK_ICON_PATH, 20, 20);
        goBackButton.setEnabled(navigationManager.canGoBack());
        goBackButton.addActionListener(e -> handleNavigationBack());
        GuiUtil.setSmartTooltip(goBackButton, "Go back");


        goForwardButton = GuiUtil.changeButtonIconColor(AppConstant.GO_FORWARD_ICON_PATH, 20, 20);
        goForwardButton.setEnabled(navigationManager.canGoForward());
        goForwardButton.addActionListener(e -> handleNavigationForward());
        GuiUtil.setSmartTooltip(goForwardButton, "Go forward");

        leftPanel.add(goBackButton, "");
        leftPanel.add(goForwardButton, "");

        // ---------- CENTER SECTION (Search bar & Home icon) ----------
        JPanel centerPanel = GuiUtil.createPanel(new MigLayout("insets 0, fillx", "[]10[grow, fill]", "[center]"));

        // Home button
        JButton homeIcon = GuiUtil.changeButtonIconColor(AppConstant.HOME_ICON_PATH, 20, 20);
        homeIcon.addActionListener(e -> toggleHome());
        homeIcon.setToolTipText("Home");
        GuiUtil.setSmartTooltip(homeIcon, "Go to Home");

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
        GuiUtil.setSmartTooltip(lookupIcon, "Look up songs, artists, albums and more!");

        searchBarWrapper.add(lookupIcon, BorderLayout.WEST);

        // Search field
        searchField = GuiUtil.createInputField("What do you want to muse?...", 20);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        searchDelayTimer = new Timer(SEARCH_DELAY, e -> {
            String query = searchField.getText();
            if (!query.isEmpty() && !query.equals("What do you want to muse?...")) {
                performSearch(query);
            }
        });
        searchDelayTimer.setRepeats(false);

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
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (recentSearchDropdown != null && recentSearchDropdown.isVisible()) {
                        recentSearchDropdown.hidePopup();
                    } else {
                        mainPanel.requestFocusInWindow();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!searchField.getText().isEmpty()
                            && !searchField.getText().equals("What do you want to muse?...")) {
                        performSearch(searchField.getText());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (searchDelayTimer.isRunning()) {
                    searchDelayTimer.restart();
                } else {
                    searchDelayTimer.start();
                }

                String query = searchField.getText();

                if ((query.isEmpty() || query.equals("What do you want to muse?..."))) {
                    toggleHome();
                }

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

        // ---------- UPGRADE SECTION (New, between Center and Right) ----------
        JPanel upgradePanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        // Add upgrade button based on user role
        userRole = currentUser.determineUserRole();
        upgradeButton = null;

        if (userRole.equals("Free User")) {
            upgradeButton = GuiUtil.createButton("Upgrade to Premium");
            upgradeButton.addActionListener(e -> handlePremiumUpgrade());
            GuiUtil.setSmartTooltip(upgradeButton, "Upgrade to Premium user");


        } else if (userRole.equals("Premium User")) {
            upgradeButton = GuiUtil.createButton("Become an Artist");
            upgradeButton.addActionListener(e -> handleArtistUpgrade());
            GuiUtil.setSmartTooltip(upgradeButton, "Upgrade to Artist user");

        }

        if (upgradeButton != null) {
            GuiUtil.styleButton(upgradeButton, ThemeManager.getInstance().getTextColor(), GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.2), ThemeManager.getInstance().getAccentColor());
            upgradePanel.add(upgradeButton);
        }

        // ---------- RIGHT SECTION (User info) ----------
        JPanel rightPanel = GuiUtil.createPanel(new MigLayout("insets 0", "[]10[]10[]10[]", "[center]"));

        // Help panel
        JPanel helpPanel = GuiUtil.createPanel(new MigLayout("insets 0, wrap", "[center]", "[center]"));
        helpPanel.setBorder(GuiUtil.createTitledBorder("Help", TitledBorder.CENTER));
        JLabel helpLabel = GuiUtil.createLabel("Type ?", Font.BOLD, 14);
        helpPanel.add(helpLabel, "");
        GuiUtil.setSmartTooltip(helpPanel, "Find it hard to use MuseMoe? Type \"?\" for more information");

        rightPanel.add(helpPanel, "w 80!");

        // User info display

        fullNameLabel = GuiUtil.createLabel(currentUser.getFullName() != null
                ? currentUser.getFullName() + " - " + userRole : "??? - " + userRole);

        GuiUtil.setSmartTooltip(fullNameLabel, "Your full name and role");

        rightPanel.add(fullNameLabel, "");

        // User avatar with menu
        JLabel avatarLabel = createUserProfileAvatar();
        GuiUtil.setSmartTooltip(avatarLabel, "User avatar");

        rightPanel.add(avatarLabel, "");

        // Add the three main sections to the header content
        headerContent.add(leftPanel, "");
        headerContent.add(centerPanel, "growx");
        headerContent.add(upgradePanel, "");
        headerContent.add(rightPanel, "");

        // Add the content to the header panel
        headerPanel.add(headerContent, BorderLayout.CENTER);

        return headerPanel;
    }

    public void setFullNameLabel(String fullName) {
        fullNameLabel.setText(fullName != null
                ? fullName + " - " + userRole : "??? - " + userRole);
    }

    private void handlePremiumUpgrade() {
        int option = GuiUtil.showConfirmMessageDialog(
                this,
                "Upgrade to Premium for $9.99/month?\nEnjoy ad-free music, unlimited downloads, and higher quality audio.",
                "Upgrade to Premium"
        );

        if (option == JOptionPane.YES_OPTION) {
            try {
                boolean success = CommonApiUtil.upgradeUser(RoleType.PREMIUM);
                if (success) {
                    GuiUtil.showSuccessMessageDialog(this, "Congratulations! You are now a Premium user.\nPlease restart the application to apply changes.");
                    // Reload the UI
                    SwingUtilities.invokeLater(() -> {
                        logout();
                    });
                } else {
                    GuiUtil.showErrorMessageDialog(this, "Failed to upgrade your account. Please try again later.");
                }
            } catch (Exception ex) {
                log.error("Error during premium upgrade", ex);
                GuiUtil.showErrorMessageDialog(this, "An unexpected error occurred. Please try again later.");
            }
        }
    }

    private void handleArtistUpgrade() {
        int option = GuiUtil.showConfirmMessageDialog(
                this,
                "Become an artist and share your music with the world?\nThis will give you access to upload tracks, create albums, and build your fanbase.",
                "Become an Artist"
        );

        if (option == JOptionPane.YES_OPTION) {

            // Stage name field
            JTextField stageNameField = GuiUtil.createTextField(20);

            // Bio field
            JTextArea bioArea = GuiUtil.createTextArea(5, 20);

            JButton selectImageButton = new JButton("Select Profile Picture");
            final JLabel imageNameLabel = new JLabel("No image selected");
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));
            fileChooser.setCurrentDirectory(AppConstant.ARTIST_PROFILE_DIRECTORY);
            fileChooser.setPreferredSize(AppConstant.FILE_CHOOSER_SIZE);
            final File[] selectedFile = {null};

            selectImageButton.addActionListener(e -> {
                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile[0] = fileChooser.getSelectedFile();
                    imageNameLabel.setText(selectedFile[0].getName());
                }
            });

            JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            imagePanel.add(selectImageButton);
            imagePanel.add(imageNameLabel);

            JOptionPane optionPane = GuiUtil.showArtistUpgradeDialog(
                    this,
                    stageNameField,
                    bioArea,
                    imageNameLabel,
                    selectImageButton
            );
            Object result = optionPane.getValue();

            if (result != null && result instanceof Integer && (Integer) result == JOptionPane.OK_OPTION
                    && !stageNameField.getText().trim().isEmpty()) {
                try {
                    MultipartFile profilePicture = null;

                    if (selectedFile[0] != null) {
                        profilePicture = new MockMultipartFile(
                                selectedFile[0].getName(),
                                selectedFile[0].getName(),
                                Files.probeContentType(selectedFile[0].toPath()),
                                Files.readAllBytes(selectedFile[0].toPath())
                        ) {
                        };
                    }
                    boolean success = CommonApiUtil.upgradeUserToArtist(
                            stageNameField.getText().trim(),
                            bioArea.getText().trim(),
                            profilePicture
                    );

                    if (success) {
                        GuiUtil.showSuccessMessageDialog(this, "Congratulations! You are now an Artist.\nPlease restart the application to apply changes.");
                        // Reload the UI
                        SwingUtilities.invokeLater(() -> {
                            logout();
                        });
                    } else {
                        GuiUtil.showErrorMessageDialog(this, "Failed to upgrade your account. Please try again later.");
                    }
                } catch (Exception ex) {
                    log.error("Error during artist upgrade", ex);
                    GuiUtil.showErrorMessageDialog(this, "An unexpected error occurred. Please try again later.");
                }
            } else if (result != null && result instanceof Integer && (Integer) result == JOptionPane.OK_OPTION) {
                GuiUtil.showWarningMessageDialog(this, "Stage name is required.");
                handleArtistUpgrade();
            }
        }
    }

    private JPanel createMiniMusicPlayerPanel() {
        return new MiniPlayerPanel();
    }

    public void toggleMiniplayer() {
        if (miniplayerActive && MiniMusicPlayerGUI.getInstance().isVisible()) {
            MiniMusicPlayerGUI.getInstance().setVisible(false);
            miniplayerActive = false;
            GuiUtil.showToast(this, "Miniplayer deactivated");
        } else {
            openMiniplayer();
            GuiUtil.showToast(this, "Miniplayer activated");
        }
    }

    public void openMiniplayer() {
        SwingUtilities.invokeLater(
                () -> {
                    for (WindowListener listener : MiniMusicPlayerGUI.getInstance().getWindowListeners()) {
                        MiniMusicPlayerGUI.getInstance().removeWindowListener(listener);
                    }

                    MiniMusicPlayerGUI.getInstance().addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            MiniMusicPlayerGUI.getInstance().setVisible(false);
                            miniplayerActive = false;
                        }
                    });

                    // Update UI if there's a current song
                    if (playerFacade.getCurrentSong() != null) {
                        ThemeManager.getInstance().setThemeColors(ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor(), ThemeManager.getInstance().getAccentColor());
                    }

                    // Show the player if it's not already visible
                    if (!MiniMusicPlayerGUI.getInstance().isVisible()) {
                        MiniMusicPlayerGUI.getInstance().setVisible(true);
                        miniplayerActive = true;
                    }

                    // Bring to front and give focus
                    MiniMusicPlayerGUI.getInstance().toFront();
                    MiniMusicPlayerGUI.getInstance().requestFocus();
                }
        );
    }


    private void loadRecentSearches() {
        try {
            java.util.List<SongDTO> recentSongs = CommonApiUtil.fetchRecentSearchHistory((AppConstant.RECENT_SEARCHED_SONG_LIMIT));
            recentSongs.forEach(song -> playerFacade.populateSongImage(song, null));
            if (!recentSongs.isEmpty()) {
                if (recentSearchDropdown == null) {
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
            log.error("Error loading recent searches", e);

            if (recentSearchDropdown != null) {
                recentSearchDropdown.hidePopup();
            }
        }

    }

    private void handleRecentSongSelected(SongDTO song) {
        AlbumDTO albumDTO = CommonApiUtil.fetchAlbumContainsThisSong(song.getId());
        playerFacade.loadSongWithContext(
                song,
                playerFacade.convertSongListToPlaylist(albumDTO.getSongDTOS(), albumDTO.getTitle()),
                PlaylistSourceType.ALBUM
        );
        searchField.setText(song.getTitle() + " - " + (song.getSongArtist() != null ? song.getSongArtist() : "Unknown"));
    }

    private void performSearch(String query) {
        if (NetworkChecker.isNetworkAvailable()) {
            if (recentSearchDropdown != null) {
                recentSearchDropdown.hidePopup();
            }

            SwingWorker<SearchResults, Void> searchWorker = new SwingWorker<>() {
                @Override
                protected SearchResults doInBackground() {
                    SearchResults results = new SearchResults();
                    results.songs = CommonApiUtil.searchSongs(query);
                    results.playlists = CommonApiUtil.searchPlaylists(query);
                    results.albums = CommonApiUtil.searchAlbums(query);
                    results.artists = CommonApiUtil.searchArtists(query);
                    return results;
                }

                @Override
                protected void done() {
                    try {
                        SearchResults results = get();

                        if ((results.songs != null && !results.songs.isEmpty()) ||
                                (results.artists != null && !results.artists.isEmpty()) ||
                                (results.albums != null && !results.albums.isEmpty()) ||
                                (results.playlists != null && !results.playlists.isEmpty())) {

                            if (results.songs != null && !results.songs.isEmpty()) {
                                SongDTO firstSong = results.songs.getFirst();
                                CommonApiUtil.logSearchHistory(firstSong.getId(), query);
                            }

                            showSearchResults(query, results);
                        } else {
                            GuiUtil.showToast(HomePage.this, "No results found matching your search.");
                        }
                    } catch (Exception e) {
                        log.error("Error performing search", e);
                        GuiUtil.showToast(HomePage.this, "Some errors happen when you're searching");
                    }
                }
            };

            searchWorker.execute();
        } else {
            GuiUtil.showNetworkErrorDialog(this, "Internet connection is unavailable");
        }
    }

    private static class SearchResults {
        List<SongDTO> songs = List.of();
        List<PlaylistDTO> playlists = List.of();
        List<AlbumDTO> albums = List.of();
        List<ArtistDTO> artists = List.of();
    }

    private void showSearchResults(String query, SearchResults results) {
        SearchResultsPanel searchResultsPanel = GuiUtil.findFirstComponentByType(centerCardPanel, SearchResultsPanel.class);

        if (searchResultsPanel != null) {
            searchResultsPanel.updateSearchResults(
                    query,
                    results.songs,
                    results.playlists,
                    results.albums,
                    results.artists
            );

            CardLayout cardLayout = (CardLayout) centerCardPanel.getLayout();
            cardLayout.show(centerCardPanel, "searchResults");

            navigationManager.navigateTo(NavigationDestination.SEARCH_RESULTS, query);

            // Log
            log.info("Showing search results for: {}", query);
        }
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
        ExpandableCardPanel likedSongsCard = GuiUtil.findFirstComponentByType(
                libraryPanel,
                ExpandableCardPanel.class,
                card -> card.getTitle().equals("Liked")
        );

        if (likedSongsCard != null) {
            boolean wasExpanded = likedSongsCard.isExpanded();
            JPanel contentPanel = likedSongsCard.getContentPanel();
            JPanel likedSongsListPanel = GuiUtil.findFirstComponentByType(
                    contentPanel,
                    JPanel.class,
                    panel -> panel.getLayout() instanceof MigLayout
            );

            if (likedSongsListPanel != null) {
                loadLikedSongsTemplate(likedSongsListPanel, false, () -> {
                    if (wasExpanded) {
                        likedSongsCard.expandPanel();
                    }
                });
            } else {
                JPanel updatedLikedSongsPanel = createLikedSongsPanel();
                likedSongsCard.setContent(updatedLikedSongsPanel);
                if (wasExpanded) {
                    likedSongsCard.expandPanel();
                }
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
        loadLikedSongsTemplate(container, true, null);
    }

    private void loadDownloadedSongs(JPanel container) {
        try {
            java.util.List<SongDTO> downloadedSongs = LocalSongManager.getDownloadedSongs();

            if (downloadedSongs.isEmpty()) {
                container.add(GuiUtil.createErrorLabel("No downloaded songs found"));
                return;
            }

            JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
            headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel headerLabel = GuiUtil.createLabel("Downloaded Songs", Font.BOLD, 14);
            headerPanel.add(headerLabel, BorderLayout.WEST);

            container.add(headerPanel);
            container.add(Box.createVerticalStrut(5));

            // Add each downloaded song to the panel
            for (SongDTO downloadedSong : downloadedSongs) {
                container.add(createLocalSongPanel(downloadedSong));
                container.add(Box.createVerticalStrut(5));
            }

        } catch (Exception e) {
            log.error("Failed to load downloaded songs: {}", e.getMessage(), e);
            container.add(GuiUtil.createErrorLabel("Failed to load downloaded songs"));
        }
    }


    private JPanel createLocalSongPanel(SongDTO song) {
        JPanel songPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        songPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Create song cover
        AsyncImageLabel coverLabel = new AsyncImageLabel(40, 40, 15);
        coverLabel.startLoading();

        if (song.getSongImage() != null) {
            coverLabel.setLoadedImage(song.getSongImage());
        } else {
            coverLabel.setLoadedImage(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH));
        }

        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));


        JLabel titleLabel = GuiUtil.createLabel(StringUtils.getTruncatedText(song.getTitle()), Font.BOLD, 12);
        GuiUtil.setSmartTooltip(titleLabel, song.getTitle());

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(
                song.getSongArtist() != null ? StringUtils.getTruncatedText(song.getSongArtist()) : "Unknown Artist",
                Font.PLAIN, 10
        );
        GuiUtil.setSmartTooltip(artistLabel, song.getSongArtist());


        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel localLabel = GuiUtil.createLabel("Local", Font.ITALIC, 9);
        localLabel.setForeground(ThemeManager.getInstance().getAccentColor());
        localLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(artistLabel);
        infoPanel.add(localLabel);

        songPanel.add(coverLabel, BorderLayout.WEST);
        songPanel.add(infoPanel, BorderLayout.CENTER);

        // Add hover effect
        GuiUtil.addHoverEffect(songPanel);

        // Add special context menu for local songs
        addLocalSongContextMenu(songPanel, song);

        songPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    log.info("Local song clicked: {}", song.getTitle());
                    playerFacade.loadLocalSong(song);
                }
            }
        });


        return songPanel;
    }

    private void addLocalSongContextMenu(JComponent component, SongDTO song) {
        JPopupMenu contextMenu = GuiUtil.createPopupMenu(ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor());

        // Play option
        JMenuItem playItem = GuiUtil.createMenuItem("Play");
        playItem.addActionListener(e -> {
            playerFacade.loadLocalSong(song);
        });
        contextMenu.add(playItem);

        JMenuItem deleteItem = GuiUtil.createMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int result = GuiUtil.showConfirmMessageDialog(SwingUtilities.getWindowAncestor(component),
                    "Are you sure you want to delete this song?\n" + song.getTitle(),
                    "Confirm Delete");

            if (result == JOptionPane.YES_OPTION) {
                try {
                    File file = new File(song.getLocalFilePath());
                    if (file.delete()) {
                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() {
                                GuiUtil.showToast(SwingUtilities.getWindowAncestor(component),
                                        "Song deleted successfully!");

                                LocalSongManager.getDownloadedSongs();
                                return null;
                            }

                            @Override
                            protected void done() {
                                SwingUtilities.invokeLater(() -> refreshDownloadedSongsPanel());
                            }
                        };
                        worker.execute();
                    } else {
                        GuiUtil.showErrorMessageDialog(SwingUtilities.getWindowAncestor(component),
                                "Could not delete song file");
                    }
                } catch (Exception ex) {
                    GuiUtil.showErrorMessageDialog(SwingUtilities.getWindowAncestor(component),
                            "Error deleting song");
                }
            }
        });

        contextMenu.add(deleteItem);

        // Open folder option
        JMenuItem openFolderItem = GuiUtil.createMenuItem("Show in folder");
        openFolderItem.addActionListener(e -> {
            try {
                File file = new File(song.getLocalFilePath());
                Desktop.getDesktop().open(file.getParentFile());
            } catch (Exception ex) {
                GuiUtil.showErrorMessageDialog(SwingUtilities.getWindowAncestor(component), "Could not open folder");
            }
        });
        contextMenu.add(openFolderItem);

        GuiUtil.registerPopupMenuForThemeUpdates(contextMenu);

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            private void showContextMenu(MouseEvent e) {
                contextMenu.show(component, e.getX(), e.getY());
            }
        });
    }


    public void refreshDownloadedSongsPanel() {
        ExpandableCardPanel downloadedSongsCard = GuiUtil.findFirstComponentByType(
                libraryPanel,
                ExpandableCardPanel.class,
                card -> card.getTitle().equals("Downloaded")
        );

        if (downloadedSongsCard != null) {
            boolean wasExpanded = downloadedSongsCard.isExpanded();
            JPanel updatedDownloadedSongsPanel = createDownloadedSongsPanel();
            downloadedSongsCard.setContent(updatedDownloadedSongsPanel);
            if (wasExpanded) {
                downloadedSongsCard.expandPanel();
            }
        }
    }

    private JPanel createLikedSongsCollectionPanel(PlaylistDTO likedSongsPlaylist) {
        JPanel likedSongsPanel = GuiUtil.createPanel(new BorderLayout(10, 0));

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
                if (!likedSongsPlaylist.getSongs().isEmpty()) {
                    playerFacade.loadSongWithContext(likedSongsPlaylist.getSongs().getFirst(), likedSongsPlaylist, PlaylistSourceType.LIKED_SONGS);
                }

            }
        });

        return likedSongsPanel;
    }

    private JPanel createPlaylistPanel(PlaylistDTO playlist) {
        JPanel playlistPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        playlistPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        playlistPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        AsyncImageLabel coverLabel = new AsyncImageLabel(40, 40, 15);
        coverLabel.startLoading();
        if (!playlist.getSongs().isEmpty()) {
            playerFacade.populateSongImage(playlist.getFirstSong(), coverLabel::setLoadedImage);
        } else {
            coverLabel.setLoadedImage(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH));
        }

        // Create playlist info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = GuiUtil.createLabel(StringUtils.getTruncatedText(playlist.getName()), Font.BOLD, 12);
        GuiUtil.setSmartTooltip(nameLabel, playlist.getName());
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
                if (!playlist.getSongs().isEmpty()) {
                    playerFacade.loadSongWithContext(playlist.getSongs().getFirst(), playlist, PlaylistSourceType.USER_PLAYLIST);
                }
            }
        });

        coverLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    log.info("playlist clicked: {}", playlist.getName());
                    navigateToPlaylistView(playlist, PlaylistSourceType.USER_PLAYLIST);
                }
            }
        });

        return playlistPanel;
    }

    private JPanel createSongPanel(SongDTO song, PlaylistDTO likedSongs) {
        JPanel songPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        songPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Create song cover
        AsyncImageLabel coverLabel = new AsyncImageLabel(40, 40, 15);
        coverLabel.startLoading();
        playerFacade.populateSongImage(song, coverLabel::setLoadedImage);


        // Create song info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));


        JLabel titleLabel = GuiUtil.createLabel(StringUtils.getTruncatedText(song.getTitle()), Font.BOLD, 12);
        GuiUtil.setSmartTooltip(titleLabel, song.getTitle());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(
                song.getSongArtist() != null ? StringUtils.getTruncatedText(song.getSongArtist()) : "Unknown Artist",
                Font.PLAIN, 10
        );

        GuiUtil.setSmartTooltip(artistLabel, song.getSongArtist());

        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(artistLabel);

        // Create action panel for heart icon
        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT));

        // Check if song is liked
        boolean isLiked = CommonApiUtil.checkSongLiked(song.getId());

        // Only show heartbutton if user has network available
        JButton heartButton;
        if (NetworkChecker.isNetworkAvailable()) {
            heartButton = GuiUtil.changeButtonIconColor(
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

            songPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        Component clickedComponent = SwingUtilities.getDeepestComponentAt(songPanel, e.getX(), e.getY());
                        if (clickedComponent != heartButton) {
                            log.info("Song clicked: {}", song.getTitle());
                            playerFacade.loadSongWithContext(song, likedSongs, PlaylistSourceType.LIKED_SONGS);
                        }
                    }
                }
            });

            coverLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        Component clickedComponent = SwingUtilities.getDeepestComponentAt(songPanel, e.getX(), e.getY());
                        if (!(clickedComponent instanceof JButton)) {
                            log.info("Song clicked: {}", song.getTitle());
                            navigateToSongDetailsView(song);
                        }
                    }
                }
            });

        }

        songPanel.add(coverLabel, BorderLayout.WEST);
        songPanel.add(infoPanel, BorderLayout.CENTER);
        songPanel.add(actionPanel, BorderLayout.EAST);

        // Add hover effect
        GuiUtil.addHoverEffect(songPanel);
        GuiUtil.addSongContextMenu(songPanel, song);

        return songPanel;
    }

    private JPanel createArtistPanel(ArtistDTO artist) {
        JPanel artistPanel = GuiUtil.createPanel(new BorderLayout(10, 0));

        artistPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        artistPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Create artist profile
        AsyncImageLabel artistProfile = new AsyncImageLabel(40, 40, 15, true);
        artistProfile.startLoading();
        playerFacade.populateArtistProfile(artist, artistProfile::setLoadedImage);

        // Create artist info panel
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.add(Box.createVerticalGlue());

        JLabel stageNameLabel = GuiUtil.createLabel(StringUtils.getTruncatedText(artist.getStageName()), Font.BOLD, 12);
        GuiUtil.setSmartTooltip(stageNameLabel, artist.getStageName());
        stageNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(stageNameLabel);

        infoPanel.add(Box.createVerticalGlue());

        artistPanel.add(artistProfile, BorderLayout.WEST);
        artistPanel.add(infoPanel, BorderLayout.CENTER);

        // Add hover effect
        GuiUtil.addHoverEffect(artistPanel);

        artistProfile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Artist clicked: {}", artist.getStageName());
                navigateToArtistView(artist);
            }
        });

        return artistPanel;
    }

    private JLabel createUserProfileAvatar() {
        JMenuItem profileItem = GuiUtil.createMenuItem("Account");
        JMenuItem logoutItem = GuiUtil.createMenuItem("Log out");
        JMenuItem defaultColorScheme = GuiUtil.createMenuItem("Default theme");

        logoutItem.addActionListener(e -> logout());
        profileItem.addActionListener(e -> {
            if (NetworkChecker.isNetworkAvailable()) {
                showAccountSettingsPanel();
            } else {
                GuiUtil.showToast(this, "No internet connection available");
            }
        });

        defaultColorScheme.addActionListener(e -> {
            int opt = GuiUtil.showConfirmMessageDialog(this, "Are you sure you want to change to default color scheme?", "Confirm");
            if (opt == JOptionPane.YES_OPTION) {
                if (ThemeManager.getInstance().getBackgroundColor().equals(AppConstant.BACKGROUND_COLOR)
                        && ThemeManager.getInstance().getTextColor().equals(AppConstant.TEXT_COLOR)
                        && ThemeManager.getInstance().getAccentColor().equals(AppConstant.TEXT_FIELD_SIZE)) {
                    GuiUtil.showInfoMessageDialog(this, "Already using default color scheme");

                } else {
                    ThemeManager.getInstance().setThemeColors(
                            AppConstant.BACKGROUND_COLOR,
                            AppConstant.TEXT_COLOR,
                            AppConstant.TEXTFIELD_BACKGROUND_COLOR
                    );
                    GuiUtil.showSuccessMessageDialog(this, "Color scheme changed to default");
                }
            }
        });

        avatarLabel = GuiUtil.createInteractiveUserAvatar(
                currentUser,
                40,
                ThemeManager.getInstance().getBackgroundColor(),
                ThemeManager.getInstance().getTextColor(),
                profileItem,
                defaultColorScheme,
                logoutItem
        );

        // Start loading and populate the image
        avatarLabel.startLoading();
        playerFacade.populateUserProfile(currentUser, avatarLabel::setLoadedImage);
        return avatarLabel;
    }

    private void logout() {
        int option = GuiUtil.showConfirmMessageDialog(this, "Do you really want to log out MuseMoe? We'll miss you :(", "Logout confirm");
        if (option == JOptionPane.YES_OPTION) {
            TokenStorage.clearToken();

            SwingUtilities.invokeLater(() -> {
                LoginPage loginPage = new LoginPage();
                UIManager.put("TitlePane.iconSize", new Dimension(24, 24));
                loginPage.getUsernameField().setText(currentUser.getUsername());
                loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 512, 512).getImage());
                loginPage.setVisible(true);
            });

            if (MiniMusicPlayerGUI.getInstance() != null) {
                playerFacade.stopSong();
                MiniMusicPlayerGUI.getInstance().setVisible(false);
            }
            ThemeManager.getInstance().setThemeColors(AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, AppConstant.TEXTFIELD_BACKGROUND_COLOR);
            this.dispose();
        }
    }

    private JPanel createCenterPanel() {
        CardLayout cardLayout = new CardLayout();
        centerCardPanel = GuiUtil.createPanel(cardLayout);
        centerCardPanel.setBorder(GuiUtil.createTitledBorder("Main", TitledBorder.LEFT));

        // Create the home panel
        homePanel = new HomePanel();
        homePanel.setName("home");
        centerCardPanel.add(homePanel, "home");

        // Create the visualizer panel
        visualizerPanel = new EnhancedSpectrumVisualizer(32);
        visualizerPanel.setName("visualizer");
        centerCardPanel.add(visualizerPanel, "visualizer");

        // Create the commit panel
        commitPanel = new CommitPanel();
        commitPanel.setName("commits");
        centerCardPanel.add(commitPanel, "commits");

        instructionPanel = new InstructionPanel();
        instructionPanel.setName("instructions");
        centerCardPanel.add(instructionPanel, "instructions");

        searchResultsPanel = new SearchResultsPanel();
        searchResultsPanel.setName("searchResults");
        centerCardPanel.add(searchResultsPanel, "searchResults");

        albumViewPanel = new AlbumViewPanel();
        albumViewPanel.setName("albumView");
        centerCardPanel.add(albumViewPanel, "albumView");

        songDetailsPanel = new SongDetailsPanel();
        songDetailsPanel.setName("songDetails");
        centerCardPanel.add(songDetailsPanel, "songDetails");

        artistProfilePanel = new ArtistProfilePanel();
        artistProfilePanel.setName("artistProfile");
        centerCardPanel.add(artistProfilePanel, "artistProfile");

        queuePanel = new QueuePanel();
        queuePanel.setName("queue");
        centerCardPanel.add(queuePanel, "queue");

        artistUploadPanel = new ArtistUploadPanel();
        artistUploadPanel.setName("artistUpload");
        centerCardPanel.add(artistUploadPanel, "artistUpload");

        adminStatisticsPanel = new AdminStatisticsPanel();
        adminStatisticsPanel.setName("adminStatistics");
        centerCardPanel.add(adminStatisticsPanel, "adminStatistics");

        accountSettingsPanel = new AccountSettingsPanel();
        accountSettingsPanel.setName("accountSettings");
        centerCardPanel.add(accountSettingsPanel, "accountSettings");

        keyEventDispatcher = e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner instanceof JTextField ||
                        focusOwner instanceof JTextArea ||
                        focusOwner instanceof JTextComponent) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        focusOwner.transferFocus();
                        return true;
                    }
                    return false;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_ESCAPE -> {
                        if (navigationManager.canGoBack()) {
                            handleNavigationBack();
                        }
                        return true;
                    }
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_F1 -> {
                        if (navigationManager.canGoForward()) {
                            handleNavigationForward();
                        }
                        return true;
                    }

                    case KeyEvent.VK_V -> {
                        if (e.isShiftDown()) {
                            toggleVisualizer();
                            return true;
                        }
                    }
                    case KeyEvent.VK_H -> {
                        toggleHome();
                        return true;
                    }
                    case KeyEvent.VK_E -> {
                        toggleMiniplayer();
                        return true;
                    }


                    case KeyEvent.VK_B -> {
                        if (activePanel == ActivePanel.VISUALIZER) {
                            toggleVisualizerBands();
                            return true;
                        } else {
                            GuiUtil.showToast(this, "Visualizer needs to be active first!");
                        }
                    }
                    case KeyEvent.VK_C -> {
                        if (e.isShiftDown()) {
                            if (NetworkChecker.isNetworkAvailable()) {
                                toggleCommitPanel();
                            } else {
                                GuiUtil.showToast(this, "No internet connection available!");
                            }
                            return true;
                        }
                    }

                    case KeyEvent.VK_SLASH -> {
                        if (e.isShiftDown()) {
                            toggleInstructionPanel();
                            return true;
                        }
                    }
                    case KeyEvent.VK_K -> {
                        SwingUtilities.invokeLater(() -> {
                            searchField.requestFocusInWindow();
                            if (searchField.getText().equals("What do you want to muse?...")) {
                                searchField.selectAll();
                            }
                        });
                        return true;
                    }
                    case KeyEvent.VK_Q -> {
                        if (NetworkChecker.isNetworkAvailable()) {
                            toggleQueuePanel();
                        } else {
                            GuiUtil.showToast(this, "No internet connection available");
                        }
                        return true;
                    }

                    case KeyEvent.VK_BACK_QUOTE -> {
                        logout();
                        return true;
                    }

                    case KeyEvent.VK_A -> {
                        if (e.isShiftDown()) {
                            if (currentUser.getRoles().contains(RoleType.ARTIST.name())) {
                                if (NetworkChecker.isNetworkAvailable()) {
                                    showArtistUploadPanel();
                                } else {
                                    GuiUtil.showToast(this, "No internet connection available");
                                }
                            } else {
                                GuiUtil.showToast(this, "You are not an artist, you can't upload songs!");
                            }
                        }
                    }

                    case KeyEvent.VK_D -> {
                        if (e.isShiftDown()) {
                            if (userRole.equals("Admin")) {
                                if (NetworkChecker.isNetworkAvailable()) {
                                    showAdminStatisticsPanel();
                                } else {
                                    GuiUtil.showToast(this, "No internet connection available");
                                }
                            } else {
                                GuiUtil.showToast(this, "You need admin privileges to access statistics!");
                            }
                            return true;
                        }
                    }

                    case KeyEvent.VK_I -> {
                        if (e.isControlDown()) {
                            if (NetworkChecker.isNetworkAvailable()) {
                                showAccountSettingsPanel();
                            } else {
                                GuiUtil.showToast(this, "No internet connection available");
                            }
                            return true;
                        }
                    }

                }
            }
            return false;
        };
        // Add global keyboard listener for toggling between views
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);

        return centerCardPanel;
    }

    private void toggleCommitPanel() {
        togglePanel(ActivePanel.COMMITS, "commits", NavigationDestination.COMMITS, null);
    }

    public void toggleQueuePanel() {
        togglePanel(ActivePanel.QUEUE, "queue", NavigationDestination.QUEUE, null);
    }


    private void toggleInstructionPanel() {
        togglePanel(ActivePanel.INSTRUCTIONS, "instructions", NavigationDestination.INSTRUCTIONS, null);
    }


    private void toggleHome() {
        navigationManager.navigateTo(NavigationDestination.HOME, null);
        switchToPanel(ActivePanel.HOME, "home");
    }

    private void toggleVisualizer() {
        togglePanel(ActivePanel.VISUALIZER, "visualizer", NavigationDestination.VISUALIZER, null);
    }

    private void toggleVisualizerBands() {
        if (visualizerPanel == null) {
            return;
        }


        int[] bandOptions = {10, 16, 24, 32, 48, 64, 98, 128, 256};
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


    private void showArtistUploadPanel() {
        togglePanel(ActivePanel.ARTIST_UPLOAD, "artistUpload", NavigationDestination.ARTIST_UPLOAD, null);
    }

    private void showAdminStatisticsPanel() {
        togglePanel(ActivePanel.ADMIN_STATISTICS, "adminStatistics", NavigationDestination.ADMIN_STATISTICS, null);
    }

    private void showAccountSettingsPanel() {
        togglePanel(ActivePanel.ACCOUNT_SETTINGS, "accountSettings", NavigationDestination.ACCOUNT_SETTINGS, null);
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

        if (upgradeButton != null) {
            GuiUtil.styleButton(upgradeButton, ThemeManager.getInstance().getTextColor(), GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.2), ThemeManager.getInstance().getAccentColor());
        }


        // Force repaint
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.type()) {

                case SONG_LIKED_CHANGED -> refreshLikedSongsPanel();

                case TOGGLE_CAVA -> {
                    if (activePanel == ActivePanel.VISUALIZER && visualizerPanel != null) {
                        boolean isToggle = (boolean) event.data();
                        visualizerPanel.toggleCAVA(isToggle);
                    }
                }

                case PLAYBACK_STARTED, PLAYBACK_PAUSED -> {

                }

                case QUEUE_UPDATED -> queuePanel.updateQueueView();
            }
        });
    }

    @Override
    public void dispose() {
        if (keyEventDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher);
        }

        if (globalClickListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalClickListener);
        }

        navigationManager.removeNavigationListener(this);
        ThemeManager.getInstance().removeThemeChangeListener(this);
        playerFacade.unsubscribeFromPlayerEvents(this);
        MiniMusicPlayerGUI.clearInstance();
        playerFacade = null;
        super.dispose();
    }

    @Override
    public void onNavigationStateChanged(boolean canGoBack, boolean canGoForward) {
        if (goBackButton != null) {
            goBackButton.setEnabled(canGoBack);
        }
        if (goForwardButton != null) {
            goForwardButton.setEnabled(canGoForward);
        }
    }

    private void handleNavigationBack() {
        NavigationItem previousItem = navigationManager.goBack();
        if (previousItem != null) {
            applyNavigationState(previousItem);
        }
    }

    private void handleNavigationForward() {
        NavigationItem nextItem = navigationManager.goForward();
        if (nextItem != null) {
            applyNavigationState(nextItem);
        }
    }

    private void loadLikedSongsTemplate(JPanel targetContainer, boolean showLoadingIndicator, Runnable onComplete) {
        if (!NetworkChecker.isNetworkAvailable()) {
            targetContainer.removeAll();
            targetContainer.add(GuiUtil.createErrorLabel("Network is unavailable!"));
            targetContainer.revalidate();
            targetContainer.repaint();
            return;
        }

        // Optional loading indicator
        JLabel loadingLabel = null;
        if (showLoadingIndicator) {
            loadingLabel = GuiUtil.createLabel("Loading liked songs...", Font.ITALIC, 12);
            targetContainer.add(loadingLabel);
            targetContainer.revalidate();
        }

        final JLabel finalLoadingLabel = loadingLabel;

        SwingWorker<List<SongDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SongDTO> doInBackground() {
                try {
                    List<SongLikesDTO> songLikesDTOList = CommonApiUtil.findAllSongLikes();

                    return songLikesDTOList.stream()
                            .map(SongLikesDTO::getSongDTO)
                            .toList();
                } catch (Exception e) {
                    log.error("Failed to load liked songs", e);
                    return List.of();
                }
            }

            @Override
            protected void done() {
                try {
                    List<SongDTO> likedSongs = get();
                    targetContainer.removeAll();

                    if (likedSongs.isEmpty()) {
                        targetContainer.add(GuiUtil.createErrorLabel("No liked songs"));
                    } else {
                        PlaylistDTO likedSongsPlaylist = new PlaylistDTO();
                        likedSongsPlaylist.setName("Liked Songs");
                        likedSongsPlaylist.setSongs(likedSongs);

                        targetContainer.add(createLikedSongsCollectionPanel(likedSongsPlaylist));

                        JPanel recentLabelPanel = GuiUtil.createPanel(new BorderLayout());
                        recentLabelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        recentLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                        JLabel recentLabel = GuiUtil.createLabel("Recently Liked", Font.BOLD, 14);
                        recentLabelPanel.add(recentLabel, BorderLayout.WEST);
                        targetContainer.add(recentLabelPanel);
                        targetContainer.add(Box.createVerticalStrut(5));
                        PlaylistDTO playlistDTO = playerFacade.convertSongListToPlaylist(likedSongs, "Liked Songs");
                        for (int i = 0; i < Math.min(5, likedSongs.size()); i++) {
                            targetContainer.add(createSongPanel(likedSongs.get(i), playlistDTO));
                            targetContainer.add(Box.createVerticalStrut(5));
                        }
                    }

                    targetContainer.revalidate();
                    targetContainer.repaint();

                    if (onComplete != null) {
                        onComplete.run();
                    }

                } catch (Exception e) {
                    log.error("Failed to load liked songs", e);
                    targetContainer.removeAll();
                    if (finalLoadingLabel != null) {
                        targetContainer.remove(finalLoadingLabel);
                    }
                    targetContainer.add(GuiUtil.createErrorLabel("Failed to load liked songs"));
                    targetContainer.revalidate();
                    targetContainer.repaint();
                }
            }
        };
        worker.execute();
    }


    public void navigateToAlbumView(AlbumDTO album) {

        if (albumViewPanel != null) {
            albumViewPanel.displayAlbum(album);
            Map<String, Object> navigationData = new HashMap<>();
            navigationData.put(NavigationDestination.ALBUM_DATA, album);

            navigationManager.navigateTo(NavigationDestination.ALBUM_VIEW, navigationData);
            switchToPanel(ActivePanel.ALBUM_VIEW, "albumView");
        }
    }

    public void navigateToArtistView(ArtistDTO artist) {

        if (artistProfilePanel != null) {
            artistProfilePanel.displayArtist(artist);

            Map<String, Object> navigationData = new HashMap<>();
            navigationData.put(NavigationDestination.ARTIST_DATA, "Artist: " + artist.getStageName());

            navigationManager.navigateTo(NavigationDestination.ARTIST_PROFILE, navigationData);
            switchToPanel(ActivePanel.ARTIST_PROFILE, "artistProfile");

        }
    }

    public void navigateToPlaylistView(PlaylistDTO playlist, PlaylistSourceType sourceType) {
        if (albumViewPanel != null) {

            albumViewPanel.displayPlaylist(playlist, sourceType);

            Map<String, Object> navigationData = new HashMap<>();
            navigationData.put(NavigationDestination.PLAYLIST_DATA, playlist);
            navigationData.put(NavigationDestination.PLAYLIST_SOURCE_TYPE, sourceType);

            navigationManager.navigateTo(NavigationDestination.ALBUM_VIEW, navigationData);
            switchToPanel(ActivePanel.ALBUM_VIEW, "albumView");
        }
    }

    public void navigateToSongDetailsView(SongDTO song) {
        if (songDetailsPanel != null) {
            songDetailsPanel.displaySong(song);

            Map<String, Object> navigationData = new HashMap<>();
            navigationData.put(NavigationDestination.SONG_DATA, song);

            navigationManager.navigateTo(NavigationDestination.SONG_DETAILS, navigationData);
            switchToPanel(ActivePanel.SONG_DETAILS, "songDetails");
        }
    }

    // For go back and go forward.
    private void applyNavigationState(NavigationManager.NavigationItem item) {
        switch (item.destination()) {
            case NavigationDestination.HOME -> {
                switchToPanel(ActivePanel.HOME, "home");
            }
            case NavigationDestination.VISUALIZER -> {
                switchToPanel(ActivePanel.VISUALIZER, "visualizer");
            }
            case NavigationDestination.COMMITS -> {
                switchToPanel(ActivePanel.COMMITS, "commits");
            }
            case NavigationDestination.INSTRUCTIONS -> {
                switchToPanel(ActivePanel.INSTRUCTIONS, "instructions");

            }

            case NavigationDestination.SEARCH_RESULTS -> {
                switchToPanel(ActivePanel.SEARCH_RESULTS, "searchResults");
            }

            case NavigationDestination.SONG_DETAILS -> {
                switchToPanel(ActivePanel.SONG_DETAILS, "songDetails");
            }

            case NavigationDestination.ARTIST_PROFILE -> {
                switchToPanel(ActivePanel.ARTIST_PROFILE, "artistProfile");
            }

            case NavigationDestination.ALBUM_VIEW -> {
                if (item.data() instanceof Map) {
                    Map<String, Object> navigationData = (Map<String, Object>) item.data();

                    if (albumViewPanel != null) {
                        if (navigationData.containsKey(NavigationDestination.ALBUM_DATA)) {
                            AlbumDTO album = (AlbumDTO) navigationData.get(NavigationDestination.ALBUM_DATA);
                            albumViewPanel.displayAlbum(album);
                        } else if (navigationData.containsKey(NavigationDestination.PLAYLIST_DATA) &&
                                navigationData.containsKey(NavigationDestination.PLAYLIST_SOURCE_TYPE)) {
                            PlaylistDTO playlist = (PlaylistDTO) navigationData.get(NavigationDestination.PLAYLIST_DATA);
                            PlaylistSourceType sourceType =
                                    (PlaylistSourceType) navigationData.get(NavigationDestination.PLAYLIST_SOURCE_TYPE);
                            albumViewPanel.displayPlaylist(playlist, sourceType);
                        }
                    }
                }

                switchToPanel(ActivePanel.ALBUM_VIEW, "albumView");
            }

            case NavigationDestination.QUEUE -> {
                switchToPanel(ActivePanel.QUEUE, "queue");

            }

            case NavigationDestination.ARTIST_UPLOAD -> {
                switchToPanel(ActivePanel.ARTIST_UPLOAD, "artistUpload");

            }

            case NavigationDestination.ADMIN_STATISTICS -> {
                switchToPanel(ActivePanel.ADMIN_STATISTICS, "adminStatistics");

            }
            case NavigationDestination.ACCOUNT_SETTINGS -> {
                switchToPanel(ActivePanel.ACCOUNT_SETTINGS, "accountSettings");
            }

        }
    }

    private void switchToPanel(ActivePanel newPanel, String cardName) {
        activePanel = newPanel;

        if (activePanel != ActivePanel.VISUALIZER) {
            playerFacade.notifyToggleCava(false);
        }

        CardLayout cardLayout = (CardLayout) centerCardPanel.getLayout();
        cardLayout.show(centerCardPanel, cardName);

        if (newPanel == ActivePanel.VISUALIZER) {
            playerFacade.notifyToggleCava(true);
        }

        log.info("{} panel activated", newPanel.name().toLowerCase());
        GuiUtil.showToast(this, newPanel.getValue() + " panel activated");
    }

    private void togglePanel(ActivePanel panelType, String cardName, String navigationDestination, Object data) {
        if (activePanel == panelType) {
            navigationManager.navigateTo(NavigationDestination.HOME, null);
            switchToPanel(ActivePanel.HOME, "home");
            log.info("{} panel deactivated", panelType.getValue());
        } else {
            navigationManager.navigateTo(navigationDestination, data);
            switchToPanel(panelType, cardName);
        }
    }

    public void refreshUserAvatar() {
        if (avatarLabel != null) {
            // Reload the current user information
            currentUser = CommonApiUtil.fetchCurrentUser();

            // Reset and reload the avatar image
            avatarLabel.startLoading();
            playerFacade.populateUserProfile(currentUser, avatarLabel::setLoadedImage);
        }
    }
}
