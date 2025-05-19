package com.javaweb.view;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.enums.PlaylistSourceType;
import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.*;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.panel.PlaylistPanel;
import com.javaweb.view.panel.PlaylistSelectionPanel;
import com.javaweb.view.panel.SongSelectionPanel;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MiniMusicPlayerGUI extends JFrame implements PlayerEventListener, ThemeChangeListener {
    @Setter
    public static MiniMusicPlayerGUI instance;
    private JLabel songTitle, songArtist, songImageLabel;
    private JPanel playbackBtns;

    @Getter
    private JSlider playbackSlider;
    private JSlider volumeSlider;
    @Getter
    private JLabel labelBeginning;
    @Getter
    private JLabel labelEnd;

    @Getter
    private JButton pauseButton;
    @Getter
    private JButton playButton;

    @Getter
    private JButton replayButton;
    @Getter
    private JButton shuffleButton;
    private JLabel speakerLabel;
    private final ImageIcon miniMuseMoeIcon;
    @Getter
    private JButton heartButton;
    @Getter
    private JButton repeatButton;
    @Getter
    @Setter
    private JLabel playlistNameLabel;

    private SongSelectionPanel songSelectionPanel;
    private PlaylistSelectionPanel playlistPanel;
    private JButton outLineHeartButton;
    private final MusicPlayerFacade playerFacade;

    private JDialog songDialog;
    private JDialog playlistDialog;
    private JDialog songPlaylistDialog;
    private final JPanel rootPanel;

    private MiniMusicPlayerGUI() {
        super("MuseMoe Miniplayer");


        initializeFrame();

        // Register for events
        playerFacade = App.getBean(MusicPlayerFacade.class);
        playerFacade.subscribeToPlayerEvents(this);
        ThemeManager.getInstance().addThemeChangeListener(this);

        // Mini muse icon setup
        miniMuseMoeIcon = GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 30, 30);
        GuiUtil.changeIconColor(miniMuseMoeIcon, ThemeManager.getInstance().getTextColor());
        setIconImage(miniMuseMoeIcon.getImage());

        rootPanel = GuiUtil.createPanel(new BorderLayout());
        rootPanel.add(createToolBar(), BorderLayout.NORTH);
        rootPanel.add(createContentPanel(), BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);

        float centerX = 0.5f;
        float centerY = 0.5f;
        float radius = 0.8f;
        Color gradientCenter = GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f);
        Color gradientOuter = GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f);
        GuiUtil.setGradientBackground(rootPanel, gradientCenter, gradientOuter, centerX, centerY, radius);

        // Set up window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

    }

    public void initializeFrame() {

        GuiUtil.styleTitleBar(this, GuiUtil.lightenColor(ThemeManager.getInstance().getBackgroundColor(), 0.12), AppConstant.TEXT_COLOR);

        // Set the size and default close operation
        setSize(420, 680);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());


    }

    public JPanel createContentPanel() {
        JPanel contentPanel = GuiUtil.createPanel(new CardLayout());
        contentPanel.setOpaque(false);

        JPanel messagePanel = GuiUtil.createPanel(new GridBagLayout());
        messagePanel.setOpaque(false);

        JLabel initialMessageLabel = GuiUtil.createLabel(
                "<html><div style=\"text-align: center;\">Please choose a song<br>or playlist!</div></html>",
                FontUtil.getSpotifyFont(Font.BOLD, 35));
        initialMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        initialMessageLabel.setForeground(AppConstant.TEXT_COLOR);
        messagePanel.add(initialMessageLabel);

        JPanel mainPanel = addGuiComponents();

        contentPanel.add(messagePanel, "message");
        contentPanel.add(mainPanel, "main");


        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();

        if (playerFacade.getCurrentSong() != null) {
            cardLayout.show(contentPanel, "main");
        } else {
            cardLayout.show(contentPanel, "message");
        }

        return contentPanel;
    }

    public static synchronized MiniMusicPlayerGUI getInstance() {
        if (instance == null) {
            instance = new MiniMusicPlayerGUI();
        }
        return instance;
    }


    private JPanel addGuiComponents() {

        // Create a main panel for other components
        JPanel mainPanel = GuiUtil.createPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Center align components
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        playlistNameLabel = new JLabel();
        playlistNameLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        playlistNameLabel.setForeground(AppConstant.TEXT_COLOR);
        playlistNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playlistNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playlistNameLabel.setVisible(false);

        // Create a fixed height panel to contain the label
        JPanel playlistLabelPanel = GuiUtil.createPanel();
        playlistLabelPanel.setLayout(new BoxLayout(playlistLabelPanel, BoxLayout.Y_AXIS));
        playlistLabelPanel.setPreferredSize(new Dimension(400, 30));


        playlistLabelPanel.add(Box.createVerticalStrut(5));
        playlistLabelPanel.add(playlistNameLabel);

        // Add the song image label
        songImageLabel = new JLabel();
        songImageLabel.setIcon(GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 300, 300));
        songImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        songImageLabel.setBounds(50, 25, 300, 300);


        //Add the imagePanel
        JPanel imagePanel = GuiUtil.createPanel();
        imagePanel.setLayout(null);
        imagePanel.setPreferredSize(new Dimension(400, 300));
        //Volume Slider
        configureVolumeSlider();
        volumeSlider.setBounds(360, 90, 20, 150);


        //Volume Icon
        speakerLabel = new JLabel();
        speakerLabel.setIcon(GuiUtil.createImageIcon(AppConstant.SPEAKER_75_ICON, 20, 20));
        GuiUtil.changeLabelIconColor(speakerLabel);
        speakerLabel.setBounds(363, 240, 20, 20);


        mainPanel.add(playlistLabelPanel);

        imagePanel.add(songImageLabel);
        imagePanel.add(volumeSlider);
        imagePanel.add(speakerLabel);

        mainPanel.add(imagePanel);


        // Add the song title label
        songTitle = new JLabel("Song Title");
        songTitle.setFont(FontUtil.getSpotifyFont(Font.BOLD, 24));
        songTitle.setForeground(AppConstant.TEXT_COLOR);
        songTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(songTitle);

        // Add the song artist label
        songArtist = new JLabel("Artist");
        songArtist.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 24));
        songArtist.setForeground(AppConstant.TEXT_COLOR);
        songArtist.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(songArtist);

        // Add the playback slider
        configurePlaybackSlider();
        JPanel labelPanel = createLabelsPanel();
        labelPanel.setBounds(0, 50, 400, 18);
        playbackSlider.setBounds(0, 15, 400, 40);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel playbackSliderPanel = GuiUtil.createPanel();
        playbackSliderPanel.setLayout(null);
        playbackSliderPanel.setPreferredSize(new Dimension(400, 50));

        heartButton = GuiUtil.changeButtonIconColor(AppConstant.HEART_ICON_PATH, 25, 25);
        heartButton.addActionListener(e -> toggleHeartButton());
        heartButton.setVisible(false);
        heartButton.setBounds(370, 0, 30, 30);

        outLineHeartButton = GuiUtil.changeButtonIconColor(AppConstant.HEART_OUTLINE_ICON_PATH, 25, 25);
        outLineHeartButton.addActionListener(e -> toggleOutlineHeartButton());
        outLineHeartButton.setBounds(370, 0, 30, 30);
        outLineHeartButton.setVisible(false);


        repeatButton = GuiUtil.changeButtonIconColor(AppConstant.REPEAT_ICON_PATH, 20, 20);
        repeatButton.setBounds(10, 0, 20, 20);
        repeatButton.addActionListener(e -> {
            /*
            Cycle repeat button:
            No Repeat -> Repeat All
            Repeat All -> Repeat One
            Repeat One -> No Repeat
            */
            playerFacade.cycleRepeatMode();
        });

        playbackSliderPanel.add(heartButton);
        playbackSliderPanel.add(outLineHeartButton);
        playbackSliderPanel.add(repeatButton);
        playbackSliderPanel.add(playbackSlider);
        playbackSliderPanel.add(Box.createVerticalStrut(1));
        playbackSliderPanel.add(labelPanel);


        mainPanel.add(playbackSliderPanel);


        // Add the playback buttons
        addPlaybackBtns();
        playbackBtns.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(playbackBtns);


        return mainPanel;

    }

    private JPanel createLabelsPanel() {
        // Create labels panel
        JPanel labelsPanel = GuiUtil.createPanel(new BorderLayout());
        labelsPanel.setPreferredSize(new Dimension(getWidth(), 18));

        labelBeginning = GuiUtil.createLabel("00:00", Font.PLAIN, 18);
        labelBeginning.setForeground(AppConstant.TEXT_COLOR);

        labelEnd = GuiUtil.createLabel("00:00", Font.PLAIN, 18);
        labelEnd.setForeground(AppConstant.TEXT_COLOR);

        // Add labels to labelsPanel
        labelsPanel.add(labelBeginning, BorderLayout.WEST);
        labelsPanel.add(labelEnd, BorderLayout.EAST);

        return labelsPanel;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = GuiUtil.createToolBar();
        // Get theme colors


        // Add the menu bar with proper styling
        JMenuBar menuBar = GuiUtil.createMenuBar();
        toolBar.add(menuBar);

        // Add the "Song" menu with proper styling
        JMenu songMenu = GuiUtil.createMenu("Song");
        menuBar.add(songMenu);

        // Add "Load Song" menu item
        JMenuItem loadSong = GuiUtil.createMenuItem("Load Downloaded Songs");
        loadSong.addActionListener(e -> {
            try {
                if (playerFacade.isHavingAd()) {
                    GuiUtil.showInfoMessageDialog(this, "Please patience finishing ads. That helps us a lot :)");
                    return;
                }
                playerFacade.setCurrentPlaylist(null);


                // Fetch all downloaded songs;
                List<SongDTO> songs = LocalSongManager.getDownloadedSongs();

                if (songs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No songs downloaded!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Create and display the SongSelectionPanel
                songSelectionPanel = new SongSelectionPanel(songs);

                songDialog = GuiUtil.createStyledDialog(this, "Select Song", songSelectionPanel, ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor());
                songDialog.setContentPane(songSelectionPanel);
                songDialog.pack();
                songDialog.setLocationRelativeTo(this);

                // Add listeners for song selection and cancellation
                songSelectionPanel.addPropertyChangeListener("songSelected", evt -> {
                    SongDTO selectedSong = (SongDTO) evt.getNewValue();
                    songDialog.dispose();
                    // Load the selected song into the music player
                    playerFacade.loadLocalSong(selectedSong);

                });

                songSelectionPanel.addPropertyChangeListener("cancel",
                        evt -> {
                            songDialog.dispose();
                            songSelectionPanel.cleanup();

                        });
                songDialog.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load downloaded songs!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        songMenu.add(loadSong);

        // Add the "Playlist" menu
        JMenu playlistMenu = GuiUtil.createMenu("Playlist");
        menuBar.add(playlistMenu);

        // Add "Load Playlist" menu item
        JMenuItem loadPlaylist = GuiUtil.createMenuItem("Load Playlist");

        loadPlaylist.addActionListener(e -> {
            if (NetworkChecker.isNetworkAvailable()) {
                if (playerFacade.isHavingAd()) {
                    GuiUtil.showInfoMessageDialog(this, "Please patience finishing ads. That helps us a lot :)");
                    return;
                }

                List<PlaylistDTO> playlists = CommonApiUtil.fetchPlaylistByUserId();

                // Create and display the PlaylistSelectionPanel
                playlists.forEach(playlist -> {
                    if (!playlist.isEmptyPlaylist()) {
                        playerFacade.populateSongImage(playlist.getSongs().getFirst(), null);
                    }
                });
                playlistPanel = new PlaylistSelectionPanel(playlists);

                playlistDialog = GuiUtil.createStyledDialog(this, "Select Playlist", playlistPanel, ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor());


                // Add listeners for playlist selection and cancellation
                playlistPanel.addPropertyChangeListener("playlistSelected", evt -> {
                    PlaylistDTO selectedPlaylist = (PlaylistDTO) evt.getNewValue();
                    playerFacade.setCurrentPlaylist(selectedPlaylist);
                    playlistDialog.dispose();
                    // Show the songs in the selected playlist
                    showSongSelectionDialog(selectedPlaylist);
                });

                playlistPanel.addPropertyChangeListener("cancel", evt -> {
                    playlistDialog.dispose();
                    playlistPanel.cleanup();
                });

                playlistDialog.setVisible(true);

            } else {
                GuiUtil.showNetworkErrorDialog(this, "Internet connection is unavailable!");
            }
        });
        playlistMenu.add(loadPlaylist);

        return toolBar;
    }

    private void addPlaybackBtns() {
        playbackBtns = GuiUtil.createPanel();
        playbackBtns.setLayout(null);

        // Replay 5 Seconds button
        replayButton = GuiUtil.changeButtonIconColor(AppConstant.REPLAY_ICON_PATH, 25,
                25);
        replayButton.setBounds(40, 0, 40, 40);
        replayButton.addActionListener(e -> {
            //Replay 5s
            playerFacade.replayFiveSeconds();
            enablePauseButtonDisablePlayButton();
        });
        playbackBtns.add(replayButton);

        // Previous button
        JButton prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, 30,
                30);
        prevButton.setBounds(120, 0, 40, 40); // Set position and size
        prevButton.addActionListener(e ->
                //Prev song
                playerFacade.prevSong());
        playbackBtns.add(prevButton);

        // Play button
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 30, 30);
        playButton.setBounds(190, 0, 40, 40);
        playButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) return;
            playerFacade.playCurrentSong();
        });
        playbackBtns.add(playButton);

        // Pause button
        pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, 30, 30);
        pauseButton.setVisible(false);
        pauseButton.setBounds(190, 0, 40, 40); // Set position and size
        pauseButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) return;
            //Pause Song
            playerFacade.pauseSong();
        });
        playbackBtns.add(pauseButton);

        // Next button
        JButton nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, 30, 30);
        nextButton.setBounds(260, 0, 40, 40); // Set position and size
        nextButton.addActionListener(e -> {
            if (playerFacade.getCurrentPlaylist().isEmptyPlaylist() && playerFacade.getCurrentPlaylist().getSourceType() == PlaylistSourceType.QUEUE) {
                GuiUtil.showToast(this, "No more songs in queue!");
            } else {
                playerFacade.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        // Shuffle button
        shuffleButton = GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH, 25,
                25);
        shuffleButton.setBounds(340, 0, 40, 40);
        shuffleButton.addActionListener(e -> {
            playerFacade.shufflePlaylist();
        });
        playbackBtns.add(shuffleButton);

    }

    public void toggleShuffleButton(boolean enabled) {
        if (!enabled) {
            shuffleButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH, ThemeManager.getInstance().getTextColor(), 25, 25).getIcon());
        } else {
            shuffleButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH
                    , 25, 25).getIcon());
        }
    }


    // Method to update the song title and artist
    public void updateSongTitleAndArtist(SongDTO song) {
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getSongArtist());
    }

    // this will be used to update our slider from the music player class
    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
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

    public void updateSongTimeLabel(int currentTimeInMilli) {
        int minutes = (currentTimeInMilli / 1000) / 60;
        int seconds = (currentTimeInMilli / 1000) % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        labelBeginning.setText(formattedTime);
    }

    private void toggleHeartButton() {
        if (CommonApiUtil.deleteSongLikes(playerFacade.getCurrentSong().getId())) {
            outLineHeartButton.setVisible(true);
            heartButton.setVisible(false);
            GuiUtil.changeButtonIconColor(outLineHeartButton);
            playerFacade.notifySongLiked();
        } else {
            GuiUtil.showErrorMessageDialog(this, "An error has occurred when removed : " + playerFacade.getCurrentSong().getTitle() + "from liked songs!");
        }
    }

    private void toggleOutlineHeartButton() {
        if (CommonApiUtil.createSongLikes(playerFacade.getCurrentSong().getId())) {
            outLineHeartButton.setVisible(false);
            heartButton.setVisible(true);
            GuiUtil.changeButtonIconColor(heartButton);
            playerFacade.notifySongLiked();
        } else {
            GuiUtil.showErrorMessageDialog(this, "An error has occurred when added : " + playerFacade.getCurrentSong().getTitle() + "to liked songs!");
        }
    }


    private void updateHeartButtonIcon(SongDTO song) {
        boolean liked = CommonApiUtil.checkSongLiked(song.getId());
        if (liked) {
            outLineHeartButton.setVisible(false);
            heartButton.setVisible(true);
            GuiUtil.changeButtonIconColor(heartButton);
        } else {
            outLineHeartButton.setVisible(true);
            heartButton.setVisible(false);
            GuiUtil.changeButtonIconColor(outLineHeartButton);
        }
    }

    public void updateSongDetails(SongDTO song) {
        updateSongTitleAndArtist(song);
        updateSongImage(song);

        if (song.getIsLocalFile()) {
            heartButton.setVisible(false);
            outLineHeartButton.setVisible(false);
        } else if (!playerFacade.isHavingAd()) {
            updateHeartButtonIcon(song);
        }
    }

    // Method to update the song image
    public void updateSongImage(SongDTO song) {
        playerFacade.populateSongImage(song, null);
        if (song.getSongImage() != null) {
            songImageLabel.setIcon(GuiUtil.createRoundedCornerImageIcon(song.getSongImage(), 10, 300, 300));
        } else {
            // Default styling when no image is available
            ImageIcon defaultIcon = GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 300, 300);
            songImageLabel.setIcon(defaultIcon);
        }
    }

    // Methods to toggle play and pause buttons
    public void enablePauseButtonDisablePlayButton() {
        playButton = (JButton) playbackBtns.getComponent(2);
        pauseButton = (JButton) playbackBtns.getComponent(3);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);

    }

    public void enablePlayButtonDisablePauseButton() {
        JButton playButton = (JButton) playbackBtns.getComponent(2);
        JButton pauseButton = (JButton) playbackBtns.getComponent(3);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);

    }

    private void configurePlaybackSlider() {
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setPreferredSize(new Dimension(500, 40));
        playbackSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, playbackSlider.getPreferredSize().height));
        playbackSlider.setForeground(AppConstant.TEXT_COLOR);
        playbackSlider.setFocusable(false);
        playbackSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Optimize slider performance
        playbackSlider.putClientProperty("Slider.paintThumbArrowShape", Boolean.FALSE);
        playbackSlider.setSnapToTicks(false);
        playbackSlider.setPaintTicks(false);
        playbackSlider.setPaintLabels(false);

        // Use lightweight tooltips
        playbackSlider.setToolTipText(null);

        // Double buffering for smoother updates
        playbackSlider.setDoubleBuffered(false);

        // Add mouse listeners to handle clicking and dragging
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Skip action if ad is playing
                if (playerFacade.isHavingAd()) {
                    return;
                }

                // Pause playback before seeking
                playerFacade.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (playerFacade.isHavingAd()) return;

                int sliderValue = playbackSlider.getValue();
                int newTimeInMilli = (int) (sliderValue / playerFacade.getCurrentSong().getFrameRatePerMilliseconds());

                playerFacade.setCurrentTimeInMilli(newTimeInMilli);
                playerFacade.setCurrentFrame(sliderValue);

                // Resume playback from the new position
                playerFacade.playCurrentSong();

            }
        });

        // Also handle mouse dragging for smoother seeking
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

                    playerFacade.notifySliderDragging(value, timeInMillis);
                }
                SwingUtilities.invokeLater(() -> playbackSlider.setValueIsAdjusting(false));
            }

        });
    }

    private void configureVolumeSlider() {
        volumeSlider = new JSlider(JSlider.VERTICAL, -40, 40, 0);
        volumeSlider.setMinimum(-40);
        volumeSlider.setMaximum(40);
        volumeSlider.setPreferredSize(new Dimension(20, 150));
        volumeSlider.setFocusable(false);
        volumeSlider.setToolTipText("Volume");

        // Optimize slider performance
        volumeSlider.setPaintTicks(false);
        volumeSlider.setPaintLabels(false);
        volumeSlider.setSnapToTicks(false);


        volumeSlider.addChangeListener(e -> {
            if (!volumeSlider.getValueIsAdjusting()) {
                int value = volumeSlider.getValue();
                System.out.println("slider volume in GUI: " + value);

                Timer volumeTimer = new Timer(100, v -> playerFacade.setVolume(value));
                volumeTimer.setRepeats(false);
                volumeTimer.start();

            }
        });
    }


    private void updateVolumeIcon(int value) {
        int percentage = (int) (((double) (value - volumeSlider.getMinimum()) /
                (volumeSlider.getMaximum() - volumeSlider.getMinimum())) * 100);

        String iconPath;
        if (percentage == 0) {
            iconPath = AppConstant.SPEAKER_0_ICON;
        } else if (percentage <= 25) {
            iconPath = AppConstant.SPEAKER_25_ICON;
        } else if (percentage <= 75) {
            iconPath = AppConstant.SPEAKER_75_ICON;
        } else {
            iconPath = AppConstant.SPEAKER_ICON;
        }

        speakerLabel.setIcon(GuiUtil.createImageIcon(iconPath, 20, 20));
        GuiUtil.changeLabelIconColor(speakerLabel);
    }

    public void setVolumeSliderValue(int value) {
        volumeSlider.setValue(value);
    }

    public void updateRepeatButtonIcon(RepeatMode repeatMode) {
        String iconPath = switch (repeatMode) {
            case NO_REPEAT -> AppConstant.REPEAT_ICON_PATH;
            case REPEAT_ALL -> AppConstant.ON_REPEAT_ICON_PATH;
            case REPEAT_ONE -> AppConstant.REPEAT_1_ICON_PATH;
        };

        // Create colored icons for both normal and hover states
        ImageIcon normalIcon = GuiUtil.createColoredIcon(iconPath, ThemeManager.getInstance().getTextColor(), 20, 20);
        ImageIcon hoverIcon = GuiUtil.createColoredIcon(iconPath, GuiUtil.lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f), 20, 20);

        // Update both icons on the existing button
        repeatButton.setIcon(normalIcon);
        repeatButton.setRolloverIcon(hoverIcon);

        // Force a repaint to show the new icon
        repeatButton.repaint();
    }


    private void showSongSelectionDialog(PlaylistDTO playlist) {
        try {
            PlaylistPanel songPanel = new PlaylistPanel(playlist.getSongs());
            songPlaylistDialog = GuiUtil.createStyledDialog(this, "Select Song", songPanel, ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor());

            songPanel.addPropertyChangeListener("songSelected", evt -> {
                SongDTO selectedSong = (SongDTO) evt.getNewValue();
                songPlaylistDialog.dispose();
                playerFacade.loadSongWithContext(selectedSong, playlist, PlaylistSourceType.USER_PLAYLIST);
            });

            songPanel.addPropertyChangeListener("cancel", evt -> {
                playlistPanel.cleanup();
                songPlaylistDialog.dispose();
            });

            songPlaylistDialog.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load songs!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hidePlaylistNameLabel(boolean isHiding) {
        playlistNameLabel.setVisible(!isHiding);
    }

    private void setHeartButtonsVisibility(boolean visible) {
        heartButton.setVisible(visible && CommonApiUtil.checkSongLiked(playerFacade.getCurrentSong().getId()));
        outLineHeartButton.setVisible(visible && !CommonApiUtil.checkSongLiked(playerFacade.getCurrentSong().getId()));
    }


    @Override
    public void onPlayerEvent(PlayerEvent event) {
        SwingUtilities.invokeLater(() -> {
            Container contentPane = getContentPane();
            JPanel rootPanel = (JPanel) contentPane.getComponent(0);
            JPanel contentPanel = (JPanel) rootPanel.getComponent(1);
            CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
            switch (event.type()) {
                case SONG_LOADED -> {
                    SongDTO song = (SongDTO) event.data();
                    cardLayout.show(contentPanel, "main");
                    updateSongDetails(song);
                    updatePlaybackSlider(song);
                    setPlaybackSliderValue(0);
                    setVolumeSliderValue(Math.round(playerFacade.getCurrentVolumeGain()));
                    enablePauseButtonDisablePlayButton();
                    hidePlaylistNameLabel(true);


                    toggleShuffleButton(false);


                }
                case SONG_LIKED_CHANGED -> {
                    updateHeartButtonIcon(playerFacade.getCurrentSong());
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

                case REPEAT_MODE_CHANGED -> {
                    updateRepeatButtonIcon((RepeatMode) event.data());
                }

                case PLAYLIST_LOADED -> {
                    PlaylistDTO playlist = (PlaylistDTO) event.data();

                    Font font = FontUtil.getSpotifyFont(Font.BOLD, 15);
                    if (playlist != null) {

                        playlistNameLabel.setText(StringUtils.getTruncatedTextByWidth(playlist.getName(), font, 200));

                        hidePlaylistNameLabel(false);
                        toggleShuffleButton(true);

                        if (playlist.getName().equals("Local Songs")) {
                            heartButton.setVisible(false);
                            outLineHeartButton.setVisible(false);
                        }

                    } else {
                        playlistNameLabel.setVisible(false);
                        toggleShuffleButton(false);
                    }
                }
                case AD_ON -> {
                    heartButton.setVisible(false);
                    outLineHeartButton.setVisible(false);
                    shuffleButton.setVisible(false);
                    repeatButton.setVisible(false);
                    replayButton.setVisible(false);
                }
                case AD_OFF -> {
                    heartButton.setVisible(true);
                    shuffleButton.setVisible(true);
                    repeatButton.setVisible(true);
                    replayButton.setVisible(true);
                }

                case SLIDER_CHANGED -> setPlaybackSliderValue((int) event.data());

                case SLIDER_DRAGGING -> {
                    int[] data = (int[]) event.data();
                    if (playbackSlider.getValueIsAdjusting()) {
                        return;
                    }
                    setPlaybackSliderValue(data[0]);
                    updateSongTimeLabel(data[1]);
                }

                case VOLUME_CHANGED -> {
                    float value = (float) event.data();
                    updateVolumeIcon(Math.round(value));
                    setVolumeSliderValue(Math.round(value));
                }

            }
        });
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        //TitleBar
        GuiUtil.styleTitleBar(this, backgroundColor, textColor);

        //Gradient
        float centerX = 0.5f;
        float centerY = 0.5f;
        float radius = 0.8f;
        Color gradientCenter = GuiUtil.lightenColor(backgroundColor, 0.1f);
        Color gradientOuter = GuiUtil.darkenColor(backgroundColor, 0.1f);
        GuiUtil.setGradientBackground(rootPanel, gradientCenter, gradientOuter, centerX, centerY, radius);

        //Update components colors
        GuiUtil.updatePanelColors(rootPanel, backgroundColor, textColor, accentColor);


        //Icons
        GuiUtil.changeIconColor(miniMuseMoeIcon, textColor);
        setIconImage(miniMuseMoeIcon.getImage());

        //Force repaint
        SwingUtilities.invokeLater(this::repaint);
    }


}