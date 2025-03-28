package com.javaweb.view;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.SecurityUtils;
import com.javaweb.view.custom.musicplayer.MusicPlayerGUI;
import lombok.Getter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HomePage extends JFrame {

    private CardLayout cardLayout;
    private JPanel centerPanel;
    private final String userImageLink;
    private final String username;
    private JLabel avatarLabel;
    private JLabel usernameLabel;
    @Getter
    private final Set<String> roles;
    private JLabel clockLabel;
    @Getter
    private final UserDTO currentUser;
    private MusicPlayerGUI musicPlayerGUI;
    private JLabel spinningDisc;
    private JPanel controlButtonsPanel;
    private Color primaryColor;
    private Color secondaryColor;
    private JPanel miniMusicPlayerPanel;
    private JSlider playbackSlider;
    private JButton prevButton;
    private JButton playButton;
    private JButton pauseButton;
    private JButton nextButton;
    private JPanel headerPanel;
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
    private final Map<String, JButton> navButtons = new HashMap<>();
    private JButton homeButton;
    private JLabel dateLabel;

    public HomePage(String userImageLink, String username, Set<String> roles, UserDTO currentUser) throws IOException {
        this.currentUser = currentUser;
        this.userImageLink = userImageLink;
        this.username = username;
        this.roles = roles;

        SecurityUtils.setAuthorities(roles);
        initializeFrame();
        JPanel mainPanel = createMainPanel();
        homeButton.setBackground(AppConstant.ACTIVE_BUTTON_BACKGROUND_COLOR);
        homeButton.setForeground(AppConstant.ACTIVE_BUTTON_TEXT_COLOR);
        add(mainPanel);
        spinningDisc.setIcon(
                GuiUtil.createDiscImageIcon(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH), 50, 50, 7));

        setVisible(true);
        GuiUtil.applyWindowStyle(this);

    }


    private void initializeFrame() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createMainPanel() throws IOException {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createNavBar(), BorderLayout.WEST);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createContentPanel() throws IOException {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
        return contentPanel;
    }

    private JPanel createHeaderPanel() throws IOException {
        headerPanel = GuiUtil.createPanel(new BorderLayout(), AppConstant.HEADER_BACKGROUND_COLOR);
        GuiUtil.setGradientBackground(headerPanel, AppConstant.BACKGROUND_COLOR,
                GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1f), 0.5f, 0.5f, 0.5f);

        //Date label
        dateLabel = new JLabel();
        dateLabel.setForeground(GuiUtil.darkenColor(AppConstant.TEXT_COLOR, 0.2f));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Timer dateTimer = new Timer(1000, e -> {
            String currentDate = LocalDate.now().format(dateFormatter);
            dateLabel.setText(currentDate);
        });
        dateTimer.start();

        // Clock Label
        clockLabel = new JLabel();
        clockLabel.setForeground(GuiUtil.darkenColor(AppConstant.TEXT_COLOR, 0.2f));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timer timer = new Timer(1000, e -> {
            String currentTime = LocalTime.now().format(formatter);
            clockLabel.setText(currentTime);
        });
        timer.start();

        // Create wrapper with GridBagLayout
        JPanel dateTimeWrapper = new JPanel(new GridBagLayout());
        dateTimeWrapper.setOpaque(false);

        // Create inner panel for date/time
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dateTimePanel.setOpaque(false);
        dateTimePanel.add(dateLabel);
        dateTimePanel.add(clockLabel);

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


        // Create user info panel
        userInfoPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT), AppConstant.HEADER_BACKGROUND_COLOR);
        userInfoPanel.setOpaque(false);

        // Create Mini Music Player Panel
        JPanel miniMusicPlayerPanel = createMiniMusicPlayerPanel();
        userInfoPanel.add(miniMusicPlayerPanel);

        // Determine user role
        String userRole = determineUserRole(roles);

        // Create and add username label
        if (username == null) {
            usernameLabel = new JLabel("???" + " - " + userRole);

        } else {
            usernameLabel = new JLabel(username + " - " + userRole);
        }
        usernameLabel.setForeground(GuiUtil.darkenColor(AppConstant.TEXT_COLOR, 0.2f));

        userInfoPanel.add(usernameLabel);

        // Create and add avatar label
        avatarLabel = createUserAvatar();
        userInfoPanel.add(avatarLabel);

        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        headerPanel.setOpaque(true);
        return headerPanel;
    }

    private JPanel createMiniMusicPlayerPanel() throws IOException {
        miniMusicPlayerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        miniMusicPlayerPanel.setBackground(AppConstant.HEADER_BACKGROUND_COLOR);
        miniMusicPlayerPanel.setOpaque(false);

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

        miniMusicPlayerPanel.add(scrollingLabel);

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
        playbackSlider = new JSlider();
        playbackSlider.setPreferredSize(new Dimension(300, 40));
        playbackSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, playbackSlider.getPreferredSize().height));
        playbackSlider.setBackground(AppConstant.HEADER_BACKGROUND_COLOR);
        playbackSlider.setForeground(AppConstant.TEXT_COLOR);
        playbackSlider.setFocusable(false);
        playbackSlider.setVisible(false);
        sliderPanel.add(playbackSlider, BorderLayout.CENTER);
        sliderPanel.add(createLabelsPanel(), BorderLayout.SOUTH);

        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    musicPlayerGUI.getMusicPlayer().pauseSong();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int sliderValue = playbackSlider.getValue();

                int newTimeInMilli = (int) (sliderValue
                        / musicPlayerGUI.getMusicPlayer().getCurrentSong().getFrameRatePerMilliseconds());

                musicPlayerGUI.getMusicPlayer().setCurrentTimeInMilli(newTimeInMilli);
                musicPlayerGUI.getMusicPlayer().setCurrentFrame(sliderValue);

                musicPlayerGUI.getMusicPlayer().playCurrentSong();

                // toggle on pause button and toggle off play button
                enablePauseButtonDisablePlayButton();
            }
        });
        // Control buttons panel
        controlButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controlButtonsPanel.setBackground(AppConstant.HEADER_BACKGROUND_COLOR);
        controlButtonsPanel.setVisible(false);
        controlButtonsPanel.setOpaque(false);

        // Previous button
        prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        prevButton.addActionListener(e -> {
            try {
                musicPlayerGUI.getMusicPlayer().prevSong();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Play button
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        playButton.setBorderPainted(false);
        playButton.addActionListener(e -> {
            musicPlayerGUI.getMusicPlayer().playCurrentSong();
        });

        // Pause button
        pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        pauseButton.setBorderPainted(false);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(e -> {
            try {
                musicPlayerGUI.getMusicPlayer().pauseSong();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Next button
        nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        nextButton.addActionListener(e -> {
            try {
                musicPlayerGUI.getMusicPlayer().nextSong();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
                try {
                    musicPlayerGUI.getMusicPlayer().pauseSong();
                    musicPlayerGUI.dispose();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                spinningDisc.setVisible(false);
                controlButtonsPanel.setVisible(false);
                playbackSlider.setVisible(false);
                stopDiscSpinning();
                stopTextScrolling();
                scrollPosition = 0;

                playbackSlider.setValue(0);
                scrollingLabel.setVisible(false);
                playMusicButton.setText("Play music!");


                //Return to original color
                extractColor(AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, AppConstant.TEXT_COLOR);
                GuiUtil.setGradientBackground(headerPanel, primaryColor, GuiUtil.darkenColor(primaryColor, 0.1f), 0.5f,
                        0.5f, 0.5f);
                dateLabel.setForeground(GuiUtil.darkenColor(AppConstant.TEXT_COLOR, 0.2f));
                clockLabel.setForeground(GuiUtil.darkenColor(AppConstant.TEXT_COLOR, 0.2f));
                usernameLabel.setForeground(GuiUtil.darkenColor(AppConstant.TEXT_COLOR, 0.2f));


            } else {
                openMusicPlayer();
            }
        });

        // Assemble mini music player components
        miniMusicPlayerPanel.add(spinningDisc);
        miniMusicPlayerPanel.add(sliderPanel);
        miniMusicPlayerPanel.add(controlButtonsPanel);
        miniMusicPlayerPanel.add(playMusicButton);


        return miniMusicPlayerPanel;
    }

    private void openMusicPlayer() {
        try {
            if (musicPlayerGUI != null) {
                if (musicPlayerGUI.getMusicPlayer().getCurrentSong() != null) {
                    showMusicPlayerHeader();
                    extractColor(musicPlayerGUI.getBackgroundColor(), musicPlayerGUI.getTextColor(), musicPlayerGUI.getAccentColor());
                    enablePlayButtonDisablePauseButton();
                    updatePlaybackSlider(musicPlayerGUI.getMusicPlayer().getCurrentSong());
                    updateSpinningDisc(musicPlayerGUI.getMusicPlayer().getCurrentSong());
                    updateScrollingText(musicPlayerGUI.getMusicPlayer().getCurrentSong());
                    setPlaybackSliderValue(musicPlayerGUI.getMusicPlayer().getCalculatedFrame());
                }
            }
            musicPlayerGUI = MusicPlayerGUI.getInstance(this);
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
        labelBeginning.setForeground(secondaryColor);

        labelEnd = GuiUtil.createSpotifyFontLabel("00:00", Font.PLAIN, 18);
        labelEnd.setForeground(secondaryColor);

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

    public void extractColor(Color primaryColor, Color secondaryColor, Color tertiaryColor) {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;

        GuiUtil.changeButtonIconColor(nextButton, secondaryColor);
        GuiUtil.changeButtonIconColor(prevButton, secondaryColor);
        GuiUtil.changeButtonIconColor(playButton, secondaryColor);
        GuiUtil.changeButtonIconColor(pauseButton, secondaryColor);

        playbackSlider.setBackground(GuiUtil.lightenColor(primaryColor, 0.3f));
        playbackSlider.setForeground(tertiaryColor);
        // playbackSlider.setPaintLabels(true);
        GuiUtil.setGradientBackground(headerPanel, primaryColor, GuiUtil.darkenColor(primaryColor, 0.3f), 0.5f, 0.5f,
                0.5f);
        dateLabel.setForeground(GuiUtil.lightenColor(primaryColor, 0.2f));
        clockLabel.setForeground(GuiUtil.lightenColor(primaryColor, 0.2f));
        usernameLabel.setForeground(GuiUtil.lightenColor(primaryColor, 0.2f));

        playMusicButton.setBackground(primaryColor);
        playMusicButton.setForeground(tertiaryColor);

        scrollingLabel.setForeground(secondaryColor);

        labelBeginning.setForeground(secondaryColor);
        labelEnd.setForeground(secondaryColor);

    }

    public String determineUserRole(Set<String> roles) {
        if (roles.contains(AppConstant.ARTIST_ROLE)) {
            return "Artist";
        } else if (roles.contains(AppConstant.PREMIUM_ROLE)) {
            return "Premium user";
        } else if (roles.contains(AppConstant.FREE_ROLE)) {
            return "Free user";
        } else {
            return "Admin";
        }
    }

    public void showMusicPlayerHeader() {
        spinningDisc.setVisible(true);
        playbackSlider.setVisible(true);
        controlButtonsPanel.setVisible(true);
        startTextScrolling();
        playMusicButton.setText("Stop music!");
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel();
        navBar.setLayout(new BoxLayout(navBar, BoxLayout.Y_AXIS));
        navBar.setBackground(AppConstant.NAVBAR_BACKGROUND_COLOR);
        navBar.setPreferredSize(new Dimension(300, getHeight()));

        navBar.add(createTitleLabel());
        navBar.add(createLogoLabel());
        navBar.add(Box.createRigidArea(new Dimension(0, 20)));

        homeButton = createNavButton("Trang chủ", "home");
        navButtons.put("home", homeButton);
        JButton productsButton = createNavButton("Sản phẩm", "products");
        navButtons.put("products", productsButton);
        JButton usersButton = createNavButton("Nhân viên", "users");
        navButtons.put("users", usersButton);
        JButton discountsButton = createNavButton("Giảm giá", "discounts");
        navButtons.put("discounts", discountsButton);
        JButton salesButton = createNavButton("Bán hàng", "sales");
        navButtons.put("sales", salesButton);
        JButton ordersButton = createNavButton("Hóa đơn", "orders");
        navButtons.put("orders", ordersButton);
        JButton statisticsButton = createNavButton("Thống kê", "statistics");
        navButtons.put("statistics", statisticsButton);

        if (SecurityUtils.getAuthorities().contains(AppConstant.ADMIN_ROLE)) {
            navBar.add(homeButton);
            navBar.add(productsButton);
            navBar.add(usersButton);
            navBar.add(discountsButton);
            navBar.add(salesButton);
            navBar.add(ordersButton);
            navBar.add(statisticsButton);
        } else if (SecurityUtils.getAuthorities().contains(AppConstant.ARTIST_ROLE)) {
            navBar.add(homeButton);
            navBar.add(productsButton);
            navBar.add(discountsButton);
            navBar.add(salesButton);
            navBar.add(ordersButton);
            navBar.add(statisticsButton);
        } else if (SecurityUtils.getAuthorities().contains(AppConstant.FREE_ROLE)) {
            navBar.add(homeButton);
        } else if (SecurityUtils.getAuthorities().contains(AppConstant.PREMIUM_ROLE)) {
            navBar.add(homeButton);
            navBar.add(productsButton);
            navBar.add(discountsButton);
            navBar.add(ordersButton);
            navBar.add(statisticsButton);
        }

        navBar.add(Box.createVerticalGlue());
        navBar.add(createQuoteLabel());

        return navBar;
    }

    public void updateUsernameLabel(String newUsername) {
        usernameLabel.setText(newUsername + " - " + determineUserRole(roles));
    }

    public void updateAvatarImage(String imagePath) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            originalImage = GuiUtil.createDefaultAvatar(50, 50);
        }

        // Create a high-quality circular avatar
        BufferedImage circularImage = GuiUtil.createSmoothCircularAvatar(originalImage, 50);

        // Update the avatar label with the new circular image
        avatarLabel.setIcon(new ImageIcon(circularImage));
    }

    private JLabel createUserAvatar() {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(new File(userImageLink));
        } catch (IOException e) {
            originalImage = GuiUtil.createDefaultAvatar(50, 50);
        }

        BufferedImage circularImage = GuiUtil.createSmoothCircularAvatar(originalImage, 50);

        avatarLabel = new JLabel(new ImageIcon(circularImage));
        avatarLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem profileItem = new JMenuItem("Account");
        JMenuItem logoutItem = new JMenuItem("Log out");

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
            resetNavButtonColors();
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
        int option = GuiUtil.showConfirmMessageDialog(this, "Do you really want to log out?", "Exit confirm",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginPage loginPage = new LoginPage();
                UIManager.put("TitlePane.iconSize", new Dimension(24, 24));
                loginPage.getUsernameField().setText(currentUser.getUsername());
                /*
                 * Remember username after logging out
                 * */
                loginPage.setIconImage(GuiUtil.createImageIcon(AppConstant.MUSE_MOE_ICON_PATH, 512, 512).getImage());
                loginPage.setVisible(true);
            });

            if (musicPlayerGUI != null) {
                musicPlayerGUI.getMusicPlayer().stopSong();
            }
            MusicPlayerGUI.instance = null;
        }

    }

    private JPanel createCenterPanel() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);


        return centerPanel;
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("Muse Moe", SwingConstants.CENTER);
        titleLabel.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 30));
        titleLabel.setForeground(AppConstant.BUTTON_TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return titleLabel;
    }

    private JLabel createLogoLabel() {
        ImageIcon logoIcon = new ImageIcon(AppConstant.LOGO_PATH);
        Image logoImage = logoIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return logoLabel;
    }

    private JLabel createQuoteLabel() {
        JLabel quoteLabel = new JLabel(
                "<html><div style='text-align: center;'>\"Cà phê và sách – vị đắng và tri thức quyện thành niềm say mê bất tận.\"<br></br><span style='color: #D7B899;'>-VoxNuts</span></div></html>");
        quoteLabel.setFont(FontUtil.getJetBrainsMonoFont(Font.ITALIC, 12));
        quoteLabel.setForeground(AppConstant.BUTTON_TEXT_COLOR);
        quoteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quoteLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return quoteLabel;
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
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.add(createGifLabel(), BorderLayout.CENTER);
        homePanel.add(createCenterTitleLabel(), BorderLayout.NORTH);
        return homePanel;
    }

    private JLabel createGifLabel() {
        ImageIcon gifIcon = new ImageIcon(AppConstant.GIF_PATH);
        JLabel gifLabel = new JLabel(gifIcon) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(gifIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gifLabel.setVerticalAlignment(SwingConstants.CENTER);
        return gifLabel;
    }

    private JLabel createCenterTitleLabel() {
        JLabel centerTitleLabel = new JLabel("LATTE LITERATURE", SwingConstants.CENTER);
        centerTitleLabel.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 50));
        centerTitleLabel.setForeground(AppConstant.ACTIVE_BACKGROUND_COLOR);
        centerTitleLabel.setBackground(AppConstant.NAVBAR_BACKGROUND_COLOR);
        centerTitleLabel.setOpaque(true);
        centerTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerTitleLabel.setVerticalAlignment(SwingConstants.TOP);
        centerTitleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        return centerTitleLabel;
    }


    private class ScrollingLabel extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontMetrics fm = g2d.getFontMetrics();
            String text = getText();
            int textWidth = fm.stringWidth(text);

            // Draw text twice for seamless scrolling
            g2d.setColor(getForeground());
            g2d.drawString(text, -scrollPosition, (float) getHeight() / 2 + (float) fm.getAscent() / 2);
            g2d.drawString(text, textWidth - scrollPosition, (float) getHeight() / 2 + (float) fm.getAscent() / 2);

            g2d.dispose();
        }
    }

    private void resetNavButtonColors() {
        for (JButton button : navButtons.values()) {
            button.setForeground(AppConstant.BUTTON_TEXT_COLOR);
            button.setBackground(AppConstant.BUTTON_BACKGROUND_COLOR);
        }
    }


}