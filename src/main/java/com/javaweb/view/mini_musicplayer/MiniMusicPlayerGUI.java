package com.javaweb.view.mini_musicplayer;

import com.javaweb.constant.AppConstant;
import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerFacade;
import com.javaweb.view.mini_musicplayer.event.MusicPlayerMediator;
import com.javaweb.view.mini_musicplayer.event.PlayerEvent;
import com.javaweb.view.mini_musicplayer.event.PlayerEventListener;
import com.javaweb.view.mini_musicplayer.panel.PlaylistPanel;
import com.javaweb.view.mini_musicplayer.panel.PlaylistSelectionPanel;
import com.javaweb.view.mini_musicplayer.panel.SongSelectionPanel;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

public class MiniMusicPlayerGUI extends JFrame implements PlayerEventListener, ThemeChangeListener {
    @Setter
    public static MiniMusicPlayerGUI instance;
    private JLabel songTitle, songArtist, songImageLabel;
    private JPanel playbackBtns;


    @Getter
    private JSlider playbackSlider;
    private JSlider volumeSlider;
    private final JLabel initialMessageLabel;
    private JPanel mainPanel;
    @Getter
    private JLabel labelBeginning;
    @Getter
    private JLabel labelEnd;

    private JToolBar toolBar;
    private JButton nextButton;
    @Getter
    private JButton pauseButton;
    @Getter
    private JButton playButton;
    private JButton prevButton;
    private JMenuBar menuBar;
    private JMenu songMenu;
    private JMenu playlistMenu;
    private JMenuItem loadSong;
    private JMenuItem loadPlaylist;

    @Getter
    private Color textColor = AppConstant.TEXT_COLOR;
    @Getter
    private Color backgroundColor = AppConstant.BACKGROUND_COLOR;
    @Getter
    private Color accentColor = backgroundColor.darker();

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
    private PlaylistPanel songPanel;
    private JButton outLineHeartButton;
    private final MusicPlayerFacade playerFacade;

    private JDialog songDialog;
    private JDialog playlistDialog;
    private JDialog songPlaylistDialog;

    private MiniMusicPlayerGUI() {
        super("MuseMoe Miniplayer");
        GuiUtil.styleTitleBar(this, GuiUtil.lightenColor(backgroundColor, 0.12), textColor);


        // Set the size and default close operation
        setSize(420, 680);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Center the frame on the screen and prevent resizing
        setLocationRelativeTo(null);
        setResizable(false);

        // Set the layout manager to BorderLayout
        setLayout(new BorderLayout());

        // Set the background color
        getContentPane().setBackground(AppConstant.BACKGROUND_COLOR);

        // Initialize the music player and file chooser

        // Add the toolbar to the NORTH position
        addToolbar();

        // Add the initial message label
        initialMessageLabel = GuiUtil.createLabel(
                "<html><div style=\"text-align: center;\">Please choose a song<br>or playlist!</div></html>", FontUtil.getSpotifyFont(Font.BOLD, 35));

        initialMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(initialMessageLabel, BorderLayout.CENTER);
        miniMuseMoeIcon = GuiUtil.createImageIcon(AppConstant.MUSE_MOE_LOGO_PATH, 30, 30);
        GuiUtil.changeIconColor(miniMuseMoeIcon, textColor);
        setIconImage(miniMuseMoeIcon.getImage());
        //Command and factory design pattern.
        playerFacade = MusicPlayerFacade.getInstance();
        MusicPlayerMediator.getInstance().subscribeToPlayerEvents(this);
        ThemeManager.getInstance().addThemeChangeListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

    }

    public static synchronized MiniMusicPlayerGUI getInstance() throws IOException {
        if (instance == null) {
            instance = new MiniMusicPlayerGUI();
        }
        return instance;
    }

    public static void cleanupInstance() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }


    private void addGuiComponents() throws IOException {
        if (mainPanel != null) {
            return;
        }

        // Create a main panel for other components
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        // Center align components
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        playlistNameLabel = new JLabel();
        playlistNameLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        playlistNameLabel.setForeground(textColor);
        playlistNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playlistNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playlistNameLabel.setVisible(false);

        // Create a fixed height panel to contain the label
        JPanel playlistLabelPanel = new JPanel();
        playlistLabelPanel.setLayout(new BoxLayout(playlistLabelPanel, BoxLayout.Y_AXIS));
        playlistLabelPanel.setPreferredSize(new Dimension(400, 30));
        playlistLabelPanel.setOpaque(false);


        playlistLabelPanel.add(Box.createVerticalStrut(5));
        playlistLabelPanel.add(playlistNameLabel);

        // Add the song image label
        songImageLabel = new JLabel();
        songImageLabel.setIcon(GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 300, 300));
        songImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        songImageLabel.setBounds(50, 25, 300, 300);


        //Add the imagePanel
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(null);
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(400, 300));
        //Volume Slider
        configureVolumeSlider();
        volumeSlider.setBounds(360, 90, 20, 150);


        //Volume Icon
        speakerLabel = new JLabel();
        speakerLabel.setIcon(GuiUtil.createImageIcon(AppConstant.SPEAKER_75_ICON, 20, 20));
        GuiUtil.changeLabelIconColor(speakerLabel, AppConstant.TEXT_COLOR);
        speakerLabel.setBounds(363, 240, 20, 20);


        mainPanel.add(playlistLabelPanel);

        imagePanel.add(songImageLabel);
        imagePanel.add(volumeSlider);
        imagePanel.add(speakerLabel);

        mainPanel.add(imagePanel);


        // Add the song title label
        songTitle = new JLabel("Song Title");
        songTitle.setFont(FontUtil.getSpotifyFont(Font.BOLD, 24));
        songTitle.setForeground(textColor);
        songTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(songTitle);

        // Add the song artist label
        songArtist = new JLabel("Artist");
        songArtist.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 24));
        songArtist.setForeground(textColor);
        songArtist.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(songArtist);

        // Add the playback slider
        configurePlaybackSlider();
        JPanel labelPanel = createLabelsPanel();
        labelPanel.setBounds(0, 50, 400, 18);
        playbackSlider.setBounds(0, 15, 400, 40);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel playbackSliderPanel = new JPanel();
        playbackSliderPanel.setLayout(null);
        playbackSliderPanel.setOpaque(false);
        playbackSliderPanel.setPreferredSize(new Dimension(400, 50));

        heartButton = GuiUtil.changeButtonIconColor(AppConstant.HEART_ICON, AppConstant.TEXT_COLOR, 25, 25);
        heartButton.addActionListener(e -> toggleHeartButton());
        heartButton.setVisible(false);
        heartButton.setBounds(370, 0, 30, 30);

        outLineHeartButton = GuiUtil.changeButtonIconColor(AppConstant.HEART_OUTLINE_ICON, AppConstant.TEXT_COLOR, 25, 25);
        outLineHeartButton.addActionListener(e -> toggleOutlineHeartButton());
        outLineHeartButton.setBounds(370, 0, 30, 30);
        outLineHeartButton.setVisible(false);
        repeatButton = GuiUtil.changeButtonIconColor(AppConstant.REPEAT_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
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
//        mainPanel.add(Box.createVerticalStrut(3));
        mainPanel.add(playbackBtns);

        // Add the main panel to the CENTER position
        add(mainPanel, BorderLayout.CENTER);

    }

    private JPanel createLabelsPanel() {
        // Create labels panel
        JPanel labelsPanel = new JPanel(new BorderLayout());
        labelsPanel.setPreferredSize(new Dimension(getWidth(), 18));
        labelsPanel.setOpaque(false);

        labelBeginning = GuiUtil.createSpotifyFontLabel("00:00", Font.PLAIN, 18);
        labelBeginning.setForeground(textColor);

        labelEnd = GuiUtil.createSpotifyFontLabel("00:00", Font.PLAIN, 18);
        labelEnd.setForeground(textColor);

        // Add labels to labelsPanel
        labelsPanel.add(labelBeginning, BorderLayout.WEST);
        labelsPanel.add(labelEnd, BorderLayout.EAST);

        return labelsPanel;
    }

    private void addToolbar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(true);
        toolBar.setBorderPainted(false);
        toolBar.setBackground(AppConstant.BACKGROUND_COLOR);
        toolBar.setForeground(AppConstant.TEXT_COLOR);

        // Add the menu bar
        menuBar = new JMenuBar();
        menuBar.setBorderPainted(false);
        menuBar.setOpaque(false);
        menuBar.setBackground(AppConstant.BACKGROUND_COLOR);
        menuBar.setForeground(AppConstant.TEXT_COLOR);

        toolBar.add(menuBar);

        // Add the "Song" menu
        songMenu = GuiUtil.createMenu("Song");
        menuBar.add(songMenu);

        // Add "Load Song" menu item
        loadSong = GuiUtil.createMenuItem("Load Song");
        loadSong.addActionListener(e -> {
            try {

                if (playerFacade.isHavingAd()) {
                    GuiUtil.showInfoMessageDialog(this, "Please patience finishing ads. That helps us a lot :)");
                    return;
                }
                playerFacade.setCurrentPlaylist(null);
                //Init essential UI
                remove(initialMessageLabel);
                addGuiComponents();

                // Fetch all downloaded songs;
                List<SongDTO> songs = CommonApiUtil.fetchUserDownloadedSongs();

                if (songs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No songs downloaded!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Create and display the SongSelectionPanel
                songSelectionPanel = new SongSelectionPanel(songs);

                songDialog = GuiUtil.createStyledDialog(this, "Select Song", songSelectionPanel, backgroundColor, textColor);
                songDialog.setContentPane(songSelectionPanel);
                songDialog.pack();
                songDialog.setLocationRelativeTo(this);

                // Add listeners for song selection and cancellation
                songSelectionPanel.addPropertyChangeListener("songSelected", evt -> {
                    SongDTO selectedSong = (SongDTO) evt.getNewValue();
                    songDialog.dispose();
                    // Load the selected song into the music player
                    playerFacade.loadSong(selectedSong);
                });

                songSelectionPanel.addPropertyChangeListener("cancel",
                        evt -> {
                            songDialog.dispose();
                            songSelectionPanel.cleanup();

                        });
                songDialog.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load songs!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        songMenu.add(loadSong);

        // Add the "Playlist" menu
        playlistMenu = GuiUtil.createMenu("Playlist");
        menuBar.add(playlistMenu);

        // Add "Load Playlist" menu item
        loadPlaylist = GuiUtil.createMenuItem("Load Playlist");

        loadPlaylist.addActionListener(e -> {
            try {
                if (playerFacade.isHavingAd()) {
                    GuiUtil.showInfoMessageDialog(this, "Please patience finishing ads. That helps us a lot :)");
                    return;
                }
                remove(initialMessageLabel);
                addGuiComponents();
                // Fetch the user's playlists
                List<PlaylistDTO> playlists = CommonApiUtil.fetchPlaylistByUserId();

                if (playlists.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No playlists found!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Create and display the PlaylistSelectionPanel
                playlistPanel = new PlaylistSelectionPanel(playlists);

                playlistDialog = GuiUtil.createStyledDialog(this, "Select Playlist", playlistPanel, backgroundColor, textColor);


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
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load playlists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        playlistMenu.add(loadPlaylist);

        // Add the toolbar to the NORTH position
        add(toolBar, BorderLayout.NORTH);
    }

    private void addPlaybackBtns() throws IOException {
        playbackBtns = new JPanel();
        playbackBtns.setLayout(null);
        playbackBtns.setOpaque(false);

        // Replay 5 Seconds button
        replayButton = GuiUtil.changeButtonIconColor(AppConstant.REPLAY_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 25,
                25);
        replayButton.setBounds(40, 0, 40, 40);
        replayButton.addActionListener(e -> {
            //Replay 5s
            playerFacade.replayFiveSeconds();
            enablePauseButtonDisablePlayButton();
        });
        playbackBtns.add(replayButton);

        // Previous button
        prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30,
                30);
        prevButton.setBounds(120, 0, 40, 40); // Set position and size
        prevButton.addActionListener(e ->
                //Prev song
                playerFacade.prevSong());
        playbackBtns.add(prevButton);

        // Play button
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30, 30);
        playButton.setBounds(190, 0, 40, 40); // Set position and size
        playButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) return;
            playerFacade.playCurrentSong();
        });
        playbackBtns.add(playButton);

        // Pause button
        pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30, 30);
        pauseButton.setVisible(false);
        pauseButton.setBounds(190, 0, 40, 40); // Set position and size
        pauseButton.addActionListener(e -> {
            if (playerFacade.isHavingAd()) return;
            //Pause Song
            playerFacade.pauseSong();
        });
        playbackBtns.add(pauseButton);

        // Next button
        nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30, 30);
        nextButton.setBounds(260, 0, 40, 40); // Set position and size
        nextButton.addActionListener(e -> {
            playerFacade.nextSong();
        });
        playbackBtns.add(nextButton);

        // Shuffle button
        shuffleButton = GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 25,
                25);
        shuffleButton.setBounds(340, 0, 40, 40); // Set position and size
        shuffleButton.addActionListener(e -> {
            playerFacade.shufflePlaylist();
        });
        playbackBtns.add(shuffleButton);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void toggleShuffleButton(boolean enabled) throws IOException {
        if (!enabled) {
            shuffleButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH,
                    GuiUtil.darkenColor(textColor, 0.2f), 25, 25).getIcon());
        } else {
            shuffleButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH,
                    textColor, 25, 25).getIcon());
        }
    }


    // Method to update the song title and artist
    public void updateSongTitleAndArtist(SongDTO song) {
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    // this will be used to update our slider from the music player class
    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
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
            GuiUtil.changeButtonIconColor(outLineHeartButton, textColor);
        } else {
            GuiUtil.showErrorMessageDialog(this, "An error has occurred when removed : " + playerFacade.getCurrentSong().getSongTitle() + "from liked songs!");
        }


    }

    private void toggleOutlineHeartButton() {
        if (CommonApiUtil.createSongLikes(playerFacade.getCurrentSong().getId())) {
            outLineHeartButton.setVisible(false);
            heartButton.setVisible(true);
            GuiUtil.changeButtonIconColor(heartButton, textColor);
        } else {
            GuiUtil.showErrorMessageDialog(this, "An error has occurred when added : " + playerFacade.getCurrentSong().getSongTitle() + "to liked songs!");
        }
    }


    private void updateHeartButtonIcon(SongDTO song) {
        boolean liked = CommonApiUtil.checkSongLiked(song.getId());
        if (liked) {
            outLineHeartButton.setVisible(false);
            heartButton.setVisible(true);
        } else {
            outLineHeartButton.setVisible(true);
            heartButton.setVisible(false);
        }
        GuiUtil.changeButtonIconColor(heartButton, textColor);
    }

    public void updateSongDetails(SongDTO song) {
        updateSongTitleAndArtist(song);
        updateSongImage(song);
        if (!playerFacade.isHavingAd()) {
            updateHeartButtonIcon(song);
        }
    }

    // Method to update the song image
    public void updateSongImage(SongDTO song) {
        if (song.getSongImage() != null) {

            songImageLabel.setIcon(GuiUtil.createRoundedCornerImageIcon(song.getSongImage(), 30));

            Color[] themeColors = GuiUtil.extractThemeColors(song.getSongImage());

            this.backgroundColor = themeColors[0];
            this.textColor = themeColors[1];
            this.accentColor = themeColors[2];

            // Apply the extracted colors to the UI
            ThemeManager.getInstance().setThemeColors(
                    backgroundColor, // backgroundColor
                    textColor, // textColor
                    accentColor  // accentColor
            );
        } else {
            // Default styling when no image is available
            ImageIcon defaultIcon = GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 300, 300);
            songImageLabel.setIcon(defaultIcon);

            // Use default colors
            ThemeManager.getInstance().setThemeColors(
                    AppConstant.BACKGROUND_COLOR,
                    AppConstant.TEXT_COLOR,
                    GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1f));
        }
    }

    // New helper method to enhance colors for better visual appeal
    private Color enhanceColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        // Slightly increase saturation for more vibrant colors
        hsb[1] = Math.min(1.0f, hsb[1] * 1.1f);

        // Adjust brightness to ensure it's not too dark or too light
        if (hsb[2] < 0.2f) {
            hsb[2] = 0.2f; // Ensure dark colors are visible
        } else if (hsb[2] > 0.9f) {
            hsb[2] = 0.9f; // Prevent colors that are too bright
        }

        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    // New helper method to create high-contrast text color
    private Color createHighContrastTextColor(Color backgroundColor) {
        float[] hsb = Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), null);

        if (hsb[2] < 0.5) {
            return GuiUtil.lightenColor(backgroundColor, 0.6f);
        } else {
            return GuiUtil.darkenColor(backgroundColor, 0.6f);
        }
    }

    public void adjustColor() {
        // Apply colors to this component's UI elements

        // Update panel themes
    }

    // New helper method to apply consistent coloring to all buttons
    private void applyConsistentButtonColors(Color baseColor) {
        GuiUtil.changeButtonIconColor(nextButton, baseColor);
        GuiUtil.changeButtonIconColor(prevButton, baseColor);
        GuiUtil.changeButtonIconColor(playButton, baseColor);
        GuiUtil.changeButtonIconColor(pauseButton, baseColor);
        GuiUtil.changeButtonIconColor(replayButton, baseColor);
        GuiUtil.changeButtonIconColor(shuffleButton, baseColor);
        GuiUtil.changeLabelIconColor(speakerLabel, baseColor);
        GuiUtil.changeButtonIconColor(repeatButton, baseColor);
        GuiUtil.changeButtonIconColor(heartButton, baseColor);
    }

    // New helper method to apply consistent text styling
    private void applyConsistentTextColors(Color baseColor) {
        songTitle.setForeground(baseColor);
        songArtist.setForeground(GuiUtil.darkenColor(baseColor, 0.1f));
        labelBeginning.setForeground(baseColor);
        labelEnd.setForeground(baseColor);
        if (playlistNameLabel != null) {
            playlistNameLabel.setForeground(baseColor);
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
        playbackSlider.setForeground(textColor);
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

                    MusicPlayerMediator.getInstance().notifySliderDragging(value, timeInMillis);
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
            int value = volumeSlider.getValue();
            System.out.println("slider volume in GUI: " + value);
            playerFacade.setVolume(value);
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
            GuiUtil.changeLabelIconColor(speakerLabel, textColor);
        });
    }

    public void setVolumeSliderValue(int value) {
        volumeSlider.setValue(value);
    }

    public void updateRepeatButtonIcon(RepeatMode repeatMode) throws IOException {
        switch (repeatMode) {
            case NO_REPEAT:
                repeatButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.REPEAT_ICON_PATH, textColor, 20, 20).getIcon());
                break;
            case REPEAT_ALL:
                repeatButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.ON_REPEAT_ICON_PATH, textColor, 20, 20).getIcon());
                break;
            case REPEAT_ONE:
                repeatButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.REPEAT_1_ICON_PATH, textColor, 20, 20).getIcon());
                break;
        }
    }


    private void showSongSelectionDialog(PlaylistDTO playlist) {
        try {
            songPanel = new PlaylistPanel(playlist.getSongs());
            songPlaylistDialog = GuiUtil.createStyledDialog(this, "Select Song", songPanel, backgroundColor, textColor);

            songPanel.addPropertyChangeListener("songSelected", evt -> {
                SongDTO selectedSong = (SongDTO) evt.getNewValue();
                songPlaylistDialog.dispose();
                playerFacade.loadSong(selectedSong);
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


    @Override
    public void onPlayerEvent(PlayerEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.type()) {
                case SONG_LOADED -> {
                    SongDTO song = (SongDTO) event.data();
                    updateSongDetails(song);
                    updatePlaybackSlider(song);
                    setPlaybackSliderValue(0);
                    setVolumeSliderValue(0);
                    enablePauseButtonDisablePlayButton();
                    playlistNameLabel.setVisible(false);
                    try {
                        toggleShuffleButton(false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
                    try {
                        updateRepeatButtonIcon((RepeatMode) event.data());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                case PLAYLIST_LOADED -> {
                    PlaylistDTO playlist = (PlaylistDTO) event.data();
                    if (playlist != null) {
                        playlistNameLabel.setText(playlist.getName());
                        playlistNameLabel.setVisible(true);
                        try {
                            toggleShuffleButton(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        playlistNameLabel.setVisible(false);
                        try {
                            toggleShuffleButton(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

            }
        });
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {

        GuiUtil.styleTitleBar(this, backgroundColor, textColor);

        Color menuBackground = GuiUtil.darkenColor(backgroundColor, 0.1f);

        GuiUtil.styleMenuItem(loadSong, menuBackground, textColor);
        GuiUtil.styleMenuItem(loadPlaylist, menuBackground, textColor);

        float centerX = 0.5f;
        float centerY = 0.5f;
        float radius = 0.8f;
        Color gradientCenter = GuiUtil.lightenColor(backgroundColor, 0.1f);
        Color gradientOuter = GuiUtil.darkenColor(backgroundColor, 0.1f);

        GuiUtil.setGradientBackground(mainPanel, gradientCenter, gradientOuter, centerX, centerY, radius);

        applyConsistentButtonColors(textColor);

        // Set slider colors with better contrast
        playbackSlider.setBackground(GuiUtil.lightenColor(backgroundColor, 0.1f));
        playbackSlider.setForeground(accentColor);

        volumeSlider.setBackground(GuiUtil.lightenColor(backgroundColor, 0.05f));
        volumeSlider.setForeground(accentColor);

        // Style menu components
        toolBar.setForeground(textColor);
        toolBar.setBackground(menuBackground);
        menuBar.setBackground(menuBackground);
        menuBar.setForeground(textColor);
        songMenu.setBackground(menuBackground);
        songMenu.setForeground(textColor);
        playlistMenu.setBackground(menuBackground);
        playlistMenu.setForeground(textColor);

        songMenu.getPopupMenu().setBorder(null);
        playlistMenu.getPopupMenu().setBorder(null);

        loadSong.setMargin(new Insets(0, 0, 0, 0));
        loadSong.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

        loadPlaylist.setMargin(new Insets(0, 0, 0, 0));
        loadPlaylist.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

        loadSong.setBackground(menuBackground);
        loadSong.setForeground(textColor);

        loadPlaylist.setBackground(menuBackground);
        loadPlaylist.setForeground(textColor);

        // Make text elements more visible with consistent colors
        applyConsistentTextColors(textColor);

        // Update icon colors
        GuiUtil.changeIconColor(miniMuseMoeIcon, textColor);
        setIconImage(miniMuseMoeIcon.getImage());

    }


}