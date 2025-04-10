package com.javaweb.view;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.mini_musicplayer.MusicPlayerGUI;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerFacade;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerMediator;
import com.javaweb.view.mini_musicplayer.event.PlayerEvent;
import com.javaweb.view.mini_musicplayer.event.PlayerEventListener;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class HomePage extends JFrame implements PlayerEventListener, ThemeChangeListener {
    private static final Dimension FRAME_SIZE = new Dimension(1024, 768);
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private JLabel avatarLabel;
    private JLabel fullNameLabel;
    private MusicPlayerGUI musicPlayerGUI;
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
    private JButton playMusicButton;
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
    private JPanel navBar;
    private final JPanel mainPanel;
    private JLabel welcomeLabel;

    private final MusicPlayerFacade playerFacade;

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


    private JPanel createMainPanel() throws IOException {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Apply a radial gradient using the GuiUtil method instead of the linear gradient
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(AppConstant.BACKGROUND_COLOR, 0.1f),
                GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1f),
                0.5f, 0.5f, 0.8f);

        topPanel = createHeaderPanel();
        topPanel.setOpaque(false);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        combinedCenterPanel = new JPanel(new BorderLayout());
        combinedCenterPanel.setOpaque(false);

        navBar = createNavBar();
        navBar.setOpaque(false);
        combinedCenterPanel.add(navBar, BorderLayout.WEST);

        centerPanel = createCenterPanel();
        centerPanel.setOpaque(false);
        combinedCenterPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(combinedCenterPanel, BorderLayout.CENTER);

        footerPanel = createMiniMusicPlayerPanel();
        footerPanel.setOpaque(false);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        return mainPanel;
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout(), AppConstant.HEADER_BACKGROUND_COLOR);

        //Date label
        dateLabel = new JLabel();
        dateLabel.setForeground(AppConstant.TEXT_COLOR);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Timer dateTimer = new Timer(1000, e -> {
            String currentDate = LocalDate.now().format(dateFormatter);
            dateLabel.setText(currentDate);
        });
        dateTimer.start();

        // Create wrapper with GridBagLayout
        JPanel dateTimeWrapper = new JPanel(new GridBagLayout());
        dateTimeWrapper.setOpaque(false);

        // Create inner panel for date/time
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dateTimePanel.setOpaque(false);
        dateTimePanel.add(dateLabel);

        // Setup GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 15, 0, 15);

        dateTimeWrapper.add(dateTimePanel, gbc);
        headerPanel.add(dateTimeWrapper, BorderLayout.WEST);

        // Create user info panel for the top right
        userInfoPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT), AppConstant.HEADER_BACKGROUND_COLOR);
        userInfoPanel.setOpaque(false);

        // Determine user role
        String userRole = determineUserRole(
                getCurrentUser().getRoles()
        );

        // Create and add username label
        if (getCurrentUser().getFullName() == null) {
            fullNameLabel = GuiUtil.createLabel("???" + " - " + userRole);
        } else {
            fullNameLabel = GuiUtil.createLabel(getCurrentUser().getFullName() + " - " + userRole);
        }
        fullNameLabel.setForeground(AppConstant.TEXT_COLOR);

        userInfoPanel.add(fullNameLabel);

        // Create and add avatar label
        avatarLabel = createUserAvatar();
        userInfoPanel.add(avatarLabel);

        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        headerPanel.setOpaque(true);
        return headerPanel;
    }

    private JPanel createMiniMusicPlayerPanel() throws IOException {
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

                // toggle on pause button and toggle off play button
                enablePauseButtonDisablePlayButton();
            }
        });

        // Control buttons panel - now using FlowLayout CENTER for better alignment
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
        playButton.setBorderPainted(false);
        playButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) {
                return;
            }
            playerFacade.playCurrentSong();
        });

        // Pause button
        pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        pauseButton.setBorderPainted(false);
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

        // "Play Music!" button
        playMusicButton = new JButton("Play music!");
        playMusicButton.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 14));
        playMusicButton.setBorderPainted(false);
        playMusicButton.setContentAreaFilled(true);
        playMusicButton.setBackground(AppConstant.BACKGROUND_COLOR);
        playMusicButton.setForeground(AppConstant.TEXT_COLOR);

        playMusicButton.addActionListener(e -> {
            if (playMusicButton.getText().equals("Stop music!")) {
                playerFacade.pauseSong();
                musicPlayerGUI.dispose();


                spinningDisc.setVisible(false);
                controlButtonsPanel.setVisible(false);
                playbackSlider.setVisible(false);
                labelEnd.setVisible(false);
                labelBeginning.setVisible(false);
                stopDiscSpinning();
                stopTextScrolling();
                scrollPosition = 0;
                playbackSlider.setValue(0);
                scrollingLabel.setVisible(false);
                playMusicButton.setText("Play music!");

                // Return to original color
                onThemeChanged(AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, AppConstant.TEXTFIELD_BACKGROUND_COLOR);
                dateLabel.setForeground(AppConstant.TEXT_COLOR);
                fullNameLabel.setForeground(AppConstant.TEXT_COLOR);
            } else {
                openMusicPlayer();
            }
        });

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
        controlsWrapper.add(playMusicButton);

        // Add padding to the panel
        miniMusicPlayerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        miniMusicPlayerPanel.add(controlsWrapper, BorderLayout.CENTER);

        return miniMusicPlayerPanel;
    }

    private void openMusicPlayer() {
        try {
            if (musicPlayerGUI != null) {
                if (playerFacade.getCurrentSong() != null) {
                    showPlaybackSlider();
                    onThemeChanged(musicPlayerGUI.getBackgroundColor(), musicPlayerGUI.getTextColor(), musicPlayerGUI.getAccentColor());
                    enablePlayButtonDisablePauseButton();
                    updatePlaybackSlider(playerFacade.getCurrentSong());
                    updateSpinningDisc(playerFacade.getCurrentSong());
                    updateScrollingText(playerFacade.getCurrentSong());
                    setPlaybackSliderValue(playerFacade.getCalculatedFrame());
                }
            }
            musicPlayerGUI = MusicPlayerGUI.getInstance();
            if (musicPlayerGUI.isVisible()) {
                musicPlayerGUI.toFront();
                musicPlayerGUI.requestFocus();
            } else {
                musicPlayerGUI.setVisible(true);
            }


        } catch (IOException ex) {
            GuiUtil.showErrorMessageDialog(this, "Cannot open MiniMusic Player.");
        }
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
            return "Premium user";
        } else if (roles.contains(AppConstant.ARTIST_ROLE)) {
            return "Artist";
        } else {
            return "Free user";
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
        playMusicButton.setText("Stop music!");
    }

    private JPanel createNavBar() {
        JPanel sideNav = new JPanel();
        sideNav.setLayout(new BoxLayout(sideNav, BoxLayout.Y_AXIS));
        sideNav.setBackground(AppConstant.BACKGROUND_COLOR);
        sideNav.setPreferredSize(new Dimension(300, getHeight()));
        return sideNav;
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
            try {
                logout();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        profileItem.addActionListener(e -> {
            navigateTo("profile");
        });

        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        return avatarLabel;
    }

    private void logout() throws IOException {
        int option = GuiUtil.showConfirmMessageDialog(this, "Do you really want to log out MuseMoe? We'll miss you :(", "Logout confirm");
        if (option == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginPage loginPage = new LoginPage();
                UIManager.put("TitlePane.iconSize", new Dimension(24, 24));
                loginPage.getUsernameField().setText(getCurrentUser().getUsername());
                loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 512, 512).getImage());
                loginPage.setVisible(true);
            });

            if (musicPlayerGUI != null) {
                playerFacade.stopSong();
            }
            MusicPlayerGUI.instance = null;
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


    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(400, 60));
        button.setMaximumSize(new Dimension(400, 60));
        button.setBackground(AppConstant.BUTTON_BACKGROUND_COLOR);
        button.setForeground(AppConstant.BUTTON_TEXT_COLOR);
        button.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 20));
        button.setFocusPainted(false);
        button.addActionListener(e -> {
            navigateTo(cardName);
        });
        return button;
    }


    private void navigateTo(String cardName) {
        cardLayout.show(centerPanel, cardName);
        for (Component component : centerPanel.getComponents()) {
            if (cardName.equals(component.getName())) {
                component.setVisible(true);
                component.addNotify();
            }
        }
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

        playbackSlider.setBackground(GuiUtil.lightenColor(backgroundColor, 0.2f));
        playbackSlider.setForeground(accentColor);

        // Force repaint of main panel to update the gradient
        Container contentPane = getContentPane();
        contentPane.repaint();

        // Update text colors
        dateLabel.setForeground(textColor);
        fullNameLabel.setForeground(textColor);

        playMusicButton.setBackground(backgroundColor);
        playMusicButton.setForeground(textColor);

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
        //Apply again the mainPanel
        GuiUtil.setGradientBackground(mainPanel,
                GuiUtil.lightenColor(backgroundColor, 0.1f),
                GuiUtil.darkenColor(backgroundColor, 0.1f),
                0.5f, 0.5f, 0.8f);

        //Welcome label
        welcomeLabel.setForeground(textColor);

    }


    private class ScrollingLabel extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Calculate dimensions relative to component size
            int width = getWidth();
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
            switch (event.getType()) {
                case SONG_LOADED -> {
                    SongDTO song = (SongDTO) event.getData();
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
                    int[] data = (int[]) event.getData();
                    setPlaybackSliderValue(data[0]);
                    updateSongTimeLabel(data[1]);
                }

                case HOME_PAGE_SLIDER_CHANGED -> showPlaybackSlider();

                case SLIDER_CHANGED -> setPlaybackSliderValue((int) event.getData());
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