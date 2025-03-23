package com.javaweb.view.custom.musicplayer;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.StringUtils;
import com.javaweb.view.HomePage;
import de.androidpit.colorthief.ColorThief;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MusicPlayerGUI extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(MusicPlayerGUI.class);
    private static MusicPlayerGUI instance;
    @Getter
    private MusicPlayer musicPlayer;
    private JFileChooser jFileChooser;
    private JLabel songTitle, songArtist, songImageLabel;
    private JPanel playbackBtns;
    private JSlider playbackSlider;
    private JSlider volumeSlider;
    private JLabel initialMessageLabel;
    private JPanel mainPanel;
    private JLabel labelBeginning;
    private JLabel labelEnd;
    private JToolBar toolBar; // Reference to the toolbar
    private JButton nextButton;
    private JButton pauseButton;
    private JButton playButton;
    private JButton prevButton;
    private JMenuBar menuBar;
    private JMenuItem loadSong;
    private JMenuItem loadPlaylist;
    private JMenuItem createPlaylist;
    private JMenu songMenu;
    private JMenu playlistMenu;
    @Getter
    private Color dialogTextColor;
    @Getter
    private Color dialogThemeColor;
    @Getter
    private Color tertiaryColor;
    private JButton replayButton;
    private JButton shuffleButton;
    private JLabel speakerLabel;
    private ImageIcon spotifyIcon;
    private JButton heartButton;
    private JButton repeatButton;
    private JLabel playlistNameLabel;


    @Getter
    private HomePage homePage;

    private MusicPlayerGUI(HomePage homePage) throws IOException {
        super("MuseMoe MiniPlayer");
        getRootPane().putClientProperty("TitlePane.font", FontUtil.getSpotifyFont(Font.BOLD, 18));
        getRootPane().putClientProperty("JRootPane.titleBarBackground", AppConstant.BACKGROUND_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", AppConstant.TEXT_COLOR);

        // Optional: To ensure consistent inactive state colors
        getRootPane().putClientProperty("JRootPane.titleBarInactiveBackground", AppConstant.BACKGROUND_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarInactiveForeground", AppConstant.TEXT_COLOR);

        // Apply the changes to this frame's root pane
        SwingUtilities.updateComponentTreeUI(this.getContentPane());

        this.homePage = homePage;
        // setOp
        // Set the size and default close operation
        setSize(410, 650);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Center the frame on the screen and prevent resizing
        setLocationRelativeTo(null);
        setResizable(false);

        // Set the layout manager to BorderLayout
        setLayout(new BorderLayout());

        // Set the background color
        getContentPane().setBackground(AppConstant.BACKGROUND_COLOR);

        // Initialize the music player and file chooser
        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();
        jFileChooser.setPreferredSize(AppConstant.FILE_CHOOSER_SIZE);

        // Set the default directory and file filter
        jFileChooser.setCurrentDirectory(new File("src/main/java/com/javaweb/view/custom/musicplayer/audio"));
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3 Files", "mp3"));

        // Add the toolbar to the NORTH position
        addToolbar();

        // Add the initial message label
        initialMessageLabel = GuiUtil.createLabel(
                "<html><div style=\"text-align: center;\">Please choose a song<br>or playlist!</div></html>", FontUtil.getSpotifyFont(Font.BOLD, 35));

        initialMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(initialMessageLabel, BorderLayout.CENTER);
        spotifyIcon = GuiUtil.createImageIcon(AppConstant.SPOTIFY_LOGO_PATH, 30, 30);
        GuiUtil.changeIconColor(spotifyIcon, AppConstant.TEXT_COLOR);
        setIconImage(spotifyIcon.getImage());
        dialogTextColor = AppConstant.TEXT_COLOR;
        dialogThemeColor = AppConstant.BACKGROUND_COLOR;

    }

    public static synchronized MusicPlayerGUI getInstance(HomePage homePage) throws IOException {
        if (instance == null) {
            instance = new MusicPlayerGUI(homePage);
        }
        return instance;
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

        // Add the song image label
        songImageLabel = new JLabel();
        songImageLabel.setIcon(GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 300, 300));
        songImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        songImageLabel.setBounds(50, 25, 300, 300);

        playlistNameLabel = new JLabel();
        playlistNameLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 12));
        playlistNameLabel.setForeground(dialogTextColor);
        playlistNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playlistNameLabel.setVisible(false);

        // Create a fixed height panel to contain the label
        JPanel playlistLabelPanel = new JPanel();
        playlistLabelPanel.setLayout(new BoxLayout(playlistLabelPanel, BoxLayout.Y_AXIS));
        playlistLabelPanel.setPreferredSize(new Dimension(400, 20));
        playlistLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        playlistLabelPanel.setOpaque(false);
        playlistLabelPanel.add(Box.createVerticalGlue());

        playlistLabelPanel.add(playlistNameLabel);

        mainPanel.add(playlistLabelPanel);


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

        imagePanel.add(songImageLabel);
        imagePanel.add(volumeSlider);
        imagePanel.add(speakerLabel);

        mainPanel.add(imagePanel);


        // Add the song title label
        songTitle = new JLabel("Song Title");
        songTitle.setFont(FontUtil.getSpotifyFont(Font.BOLD, 24));
        songTitle.setForeground(dialogTextColor);
        songTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(songTitle);

        // Add the song artist label
        songArtist = new JLabel("Artist");
        songArtist.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 24));
        songArtist.setForeground(dialogTextColor);
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

        heartButton = GuiUtil.changeButtonIconColor(AppConstant.HEART_OUTLINE_ICON, AppConstant.TEXT_COLOR, 30, 30);
        heartButton.addActionListener(e -> {
            try {
                toggleLikeSong(musicPlayer.getCurrentSong());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                updateHeartButtonIcon(musicPlayer.getCurrentSong());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        heartButton.setBounds(370, 0, 30, 30);

        repeatButton = GuiUtil.changeButtonIconColor(AppConstant.REPEAT_ICON_PATH, AppConstant.TEXT_COLOR, 20, 20);
        repeatButton.setBounds(10, 0, 20, 20);
        repeatButton.addActionListener(e -> {
            try {
                musicPlayer.cycleRepeatMode();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        playbackSliderPanel.add(heartButton);
        playbackSliderPanel.add(repeatButton);
        playbackSliderPanel.add(playbackSlider);
        playbackSliderPanel.add(Box.createVerticalStrut(1));
        playbackSliderPanel.add(labelPanel);


//        mainPanel.add(playbackSlider);
//        mainPanel.add(Box.createVerticalStrut(1));
//        mainPanel.add(createLabelsPanel());

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
        labelBeginning.setForeground(dialogTextColor);

        labelEnd = GuiUtil.createSpotifyFontLabel("00:00", Font.PLAIN, 18);
        labelEnd.setForeground(dialogTextColor);

        // Add labels to labelsPanel
        labelsPanel.add(labelBeginning, BorderLayout.WEST);
        labelsPanel.add(labelEnd, BorderLayout.EAST);
//        labelsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, labelsPanel.getPreferredSize().height));

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
        menuBar.setOpaque(true);
        menuBar.setBackground(AppConstant.BACKGROUND_COLOR);
        menuBar.setForeground(AppConstant.TEXT_COLOR);

        toolBar.add(menuBar);

        // Add the "Song" menu
        songMenu = GuiUtil.createMenu("Song");
        menuBar.add(songMenu);

        // Add "Load Song" menu item
        loadSong = GuiUtil.createMenuItem("Load Song");
        loadSong.addActionListener(e -> {
            int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
            File selectedFile = jFileChooser.getSelectedFile();

            if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                Song song = new Song(selectedFile.getPath());
                remove(initialMessageLabel);
                try {
                    addGuiComponents();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    musicPlayer.loadSong(song);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        songMenu.add(loadSong);

        // Add the "Playlist" menu
        playlistMenu = GuiUtil.createMenu("Playlist");
        menuBar.add(playlistMenu);

        // Add "Create Playlist" menu item
        createPlaylist = GuiUtil.createMenuItem("Create Playlist");

        createPlaylist.addActionListener(e -> {
            try {
                openPlaylistDialog();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        playlistMenu.add(createPlaylist);

        // Add "Load Playlist" menu item
        loadPlaylist = GuiUtil.createMenuItem("Load Playlist");

        loadPlaylist.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setPreferredSize(AppConstant.FILE_CHOOSER_SIZE);
            jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist Files", "txt"));
            jFileChooser.setCurrentDirectory(new File("src/main/java/com/javaweb/view/custom/musicplayer/playlist"));

            int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
            File selectedFile = jFileChooser.getSelectedFile();

            if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                remove(initialMessageLabel);
                try {
                    addGuiComponents();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                try {
                    musicPlayer.loadPlaylist(selectedFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
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
        replayButton.setBorderPainted(false);
        replayButton.setBounds(40, 0, 40, 40);
        replayButton.addActionListener(e -> {
            try {
                musicPlayer.replayFiveSeconds();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            enablePauseButtonDisablePlayButton();
        });
        playbackBtns.add(replayButton);

        // Previous button
        prevButton = GuiUtil.changeButtonIconColor(AppConstant.PREVIOUS_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30,
                30);
        prevButton.setBorderPainted(false);
        prevButton.setBounds(120, 0, 40, 40); // Set position and size
        prevButton.addActionListener(e -> {
            try {
                musicPlayer.prevSong();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        playbackBtns.add(prevButton);

        // Play button
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30, 30);
        playButton.setBorderPainted(false);
        playButton.setBounds(190, 0, 40, 40); // Set position and size
        playButton.addActionListener(e -> {
            musicPlayer.playCurrentSong();
        });
        playbackBtns.add(playButton);

        // Pause button
        pauseButton = GuiUtil.changeButtonIconColor(AppConstant.PAUSE_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30, 30);
        pauseButton.setBorderPainted(false);
        pauseButton.setVisible(false);
        pauseButton.setBounds(190, 0, 40, 40); // Set position and size
        pauseButton.addActionListener(e -> {
            try {
                musicPlayer.pauseSong();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        playbackBtns.add(pauseButton);

        // Next button
        nextButton = GuiUtil.changeButtonIconColor(AppConstant.NEXT_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 30, 30);
        nextButton.setBorderPainted(false);
        nextButton.setBounds(260, 0, 40, 40); // Set position and size
        nextButton.addActionListener(e -> {
            try {
                musicPlayer.nextSong();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        playbackBtns.add(nextButton);

        // Shuffle button
        shuffleButton = GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH, AppConstant.MUSIC_PLAYER_TEXT_COLOR, 25,
                25);
        shuffleButton.setBorderPainted(false);
        shuffleButton.setBounds(340, 0, 40, 40); // Set position and size
        shuffleButton.addActionListener(e -> {
            try {
                musicPlayer.shufflePlaylist();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        playbackBtns.add(shuffleButton);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void toggleShuffleButton(boolean enabled) throws IOException {
        if (!enabled) {
            shuffleButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH,
                    GuiUtil.darkenColor(dialogTextColor, 0.2f), 25, 25).getIcon());
        } else {
            shuffleButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH,
                    dialogTextColor, 25, 25).getIcon());
        }
    }

    // Method to update the song title and artist
    public void updateSongTitleAndArtist(Song song) {
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    // this will be used to update our slider from the music player class
    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
    }

    public void updatePlaybackSlider(Song song) {
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

    private void toggleLikeSong(Song song) throws IOException {
        Path path = Paths.get(AppConstant.LIKED_SONG_PATH);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        List<String> likedSongs = Files.readAllLines(path);
        String songPath = StringUtils.getRelativeFilePath(song.getFilePath());

        if (likedSongs.contains(songPath)) {
            likedSongs.remove(songPath);
        } else {
            likedSongs.add(songPath);
        }

        Files.write(path, likedSongs);
    }

    private void updateHeartButtonIcon(Song song) throws IOException {
        Path path = Paths.get(AppConstant.LIKED_SONG_PATH);
        if (Files.exists(path)) {
            List<String> likedSongs = Files.readAllLines(path);
            if (likedSongs.contains(StringUtils.getRelativeFilePath(song.getFilePath()))) {
                heartButton.setIcon(GuiUtil.createImageIcon(AppConstant.HEART_ICON, 25, 25));
                GuiUtil.changeButtonIconColor(heartButton, dialogTextColor);
            } else {
                heartButton.setIcon(GuiUtil.createImageIcon(AppConstant.HEART_OUTLINE_ICON, 25, 25));
                GuiUtil.changeButtonIconColor(heartButton, dialogTextColor);

            }
        } else {
            heartButton.setIcon(GuiUtil.createImageIcon(AppConstant.HEART_OUTLINE_ICON, 25, 25));
            GuiUtil.changeButtonIconColor(heartButton, dialogTextColor);
        }
    }

    public void updateSongDetails(Song song) throws IOException {
        updateSongTitleAndArtist(song);
        updateSongImage(song);
        try {
            updateHeartButtonIcon(song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to update the song image
    public void updateSongImage(Song song) throws IOException {
        if (song.getSongImage() != null) {
            // ImageIcon imageIcon = new ImageIcon(song.getSongImage());

            songImageLabel.setIcon(GuiUtil.createRoundedCornerImageIcon(song.getSongImage(), 30));

            // Extract the top two dominant colors
            int[][] dominantColors = ColorThief.getPalette(song.getSongImage(), 3, 1, false);
            if (dominantColors != null && dominantColors.length >= 2) {
                Color primaryColor = new Color(dominantColors[0][0], dominantColors[0][1], dominantColors[0][2]);
                Color secondaryColor = new Color(dominantColors[1][0], dominantColors[1][1], dominantColors[1][2]);
                tertiaryColor = new Color(dominantColors[2][0], dominantColors[2][1], dominantColors[2][2]);
                // change the primary and secondary color if the contrast ration is too low
                double contrastRatio = GuiUtil.calculateContrast(primaryColor, secondaryColor);
                if (contrastRatio < 4.5) {
                    dialogThemeColor = primaryColor;
                    dialogTextColor = GuiUtil.lightenColor(primaryColor, 0.2f);
                } else {
                    dialogThemeColor = primaryColor;
                    dialogTextColor = secondaryColor;
                }
                adjustColor();
            }
        } else {
            ImageIcon defaultIcon = GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 300, 300);
            songImageLabel.setIcon(defaultIcon);

            // Set default colors
            dialogThemeColor = AppConstant.BACKGROUND_COLOR;
            dialogTextColor = AppConstant.TEXT_COLOR;

            adjustColor();

        }
    }

    public void adjustColor() {
        getRootPane().putClientProperty("TitlePane.font", FontUtil.getSpotifyFont(Font.BOLD, 18));
        getRootPane().putClientProperty("JRootPane.titleBarBackground", GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        getRootPane().putClientProperty("JRootPane.titleBarForeground", dialogTextColor);

        // Optional: To ensure consistent inactive state colors
        getRootPane().putClientProperty("JRootPane.titleBarInactiveBackground",
                GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        getRootPane().putClientProperty("JRootPane.titleBarInactiveForeground", dialogTextColor);

        // Apply the changes to this frame's root pane
        SwingUtilities.updateComponentTreeUI(this.getContentPane());

        loadSong.setMargin(new Insets(0, 0, 0, 0));
        loadSong.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        loadSong.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

        createPlaylist.setMargin(new Insets(0, 0, 0, 0));
        createPlaylist.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        createPlaylist.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

        loadPlaylist.setMargin(new Insets(0, 0, 0, 0));
        loadPlaylist.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        loadPlaylist.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

        songMenu.getPopupMenu().setBorder(null);

        playlistMenu.getPopupMenu().setBorder(null);

        GuiUtil.setGradientBackground(mainPanel, dialogThemeColor, GuiUtil.darkenColor(dialogThemeColor, 0.2f), 0.5f,
                0.7f, 0.5f);

        GuiUtil.changeButtonIconColor(nextButton, dialogTextColor);
        GuiUtil.changeButtonIconColor(prevButton, dialogTextColor);
        GuiUtil.changeButtonIconColor(playButton, dialogTextColor);
        GuiUtil.changeButtonIconColor(pauseButton, dialogTextColor);
        GuiUtil.changeButtonIconColor(replayButton, dialogTextColor);
        GuiUtil.changeButtonIconColor(shuffleButton, dialogTextColor);
        GuiUtil.changeLabelIconColor(speakerLabel, dialogTextColor);
        GuiUtil.changeButtonIconColor(repeatButton, dialogTextColor);
        GuiUtil.changeButtonIconColor(heartButton, dialogTextColor);

        playbackSlider.setBackground(GuiUtil.lightenColor(dialogThemeColor, 0.3f));
        playbackSlider.setForeground(tertiaryColor);
        volumeSlider.setBackground(GuiUtil.lightenColor(dialogThemeColor, 0.2f));
        volumeSlider.setForeground(dialogTextColor);

        toolBar.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        toolBar.setForeground(dialogTextColor);
        menuBar.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        menuBar.setForeground(dialogTextColor);

        songMenu.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        songMenu.setForeground(dialogTextColor);

        playlistMenu.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        playlistMenu.setForeground(dialogTextColor);

        loadSong.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        loadSong.setForeground(dialogTextColor);

        createPlaylist.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        createPlaylist.setForeground(dialogTextColor);

        loadPlaylist.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        loadPlaylist.setForeground(dialogTextColor);

        songTitle.setForeground(dialogTextColor);
        songArtist.setForeground(dialogTextColor);

        labelBeginning.setForeground(dialogTextColor);
        labelEnd.setForeground(dialogTextColor);
        if (playlistNameLabel != null) {
            playlistNameLabel.setForeground(dialogTextColor);
        }

        homePage.extractColor(dialogThemeColor, dialogTextColor, tertiaryColor);

        GuiUtil.changeIconColor(spotifyIcon, dialogTextColor);
        setIconImage(spotifyIcon.getImage());


    }

    private void openPlaylistDialog() throws IOException {
        MusicPlaylistDialog dialog = new MusicPlaylistDialog(this, this.dialogThemeColor, this.dialogTextColor);
        dialog.setVisible(true);
    }

    // Methods to toggle play and pause buttons
    public void enablePauseButtonDisablePlayButton() {
        JButton playButton = (JButton) playbackBtns.getComponent(2);
        JButton pauseButton = (JButton) playbackBtns.getComponent(3);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);

        homePage.enablePauseButtonDisablePlayButton();
    }

    public void enablePlayButtonDisablePauseButton() {
        JButton playButton = (JButton) playbackBtns.getComponent(2);
        JButton pauseButton = (JButton) playbackBtns.getComponent(3);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
        homePage.enablePlayButtonDisablePauseButton();

    }

    private void configurePlaybackSlider() {
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setPreferredSize(new Dimension(500, 40));
        playbackSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, playbackSlider.getPreferredSize().height));
        playbackSlider.setForeground(dialogTextColor);
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

        // Add mouse listeners to handle dragging
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    musicPlayer.pauseSong();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int sliderValue = playbackSlider.getValue();
                int newTimeInMilli = (int) (sliderValue / musicPlayer.getCurrentSong().getFrameRatePerMilliseconds());

                musicPlayer.setCurrentTimeInMilli(newTimeInMilli);
                musicPlayer.setCurrentFrame(sliderValue);
                musicPlayer.playCurrentSong();

                enablePauseButtonDisablePlayButton();
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
            if (musicPlayer != null) {
                int value = volumeSlider.getValue();
                System.out.println("slider volume in GUI: " + value);
                musicPlayer.setVolume(value);

                // Calculate percentage (0-100)
                int percentage = (int) (((double) (value - volumeSlider.getMinimum()) /
                        (volumeSlider.getMaximum() - volumeSlider.getMinimum())) * 100);

                // Update speaker icon based on volume level
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
                GuiUtil.changeLabelIconColor(speakerLabel, dialogTextColor);
            }
        });
    }

    public void setVolumeSliderValue(int value) {
        volumeSlider.setValue(value);
    }

    public void updateRepeatButtonIcon() throws IOException {
        switch (musicPlayer.getRepeatMode()) {
            case NO_REPEAT:
                repeatButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.REPEAT_ICON_PATH, dialogTextColor, 20, 20).getIcon());
                break;
            case REPEAT_ALL:
                repeatButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.ON_REPEAT_ICON_PATH, dialogTextColor, 20, 20).getIcon());
                break;
            case REPEAT_ONE:
                repeatButton.setIcon(GuiUtil.changeButtonIconColor(AppConstant.REPEAT_1_ICON_PATH, dialogTextColor, 20, 20).getIcon());
                break;
        }
    }

    public void updatePlaylistName(File playlistFile) {
        if (playlistFile != null) {
            String fileName = playlistFile.getName();
            // Remove .txt extension
            String playlistName = fileName.substring(0, fileName.lastIndexOf('.'));
            // Convert to title case and replace underscores with spaces
            playlistName = playlistName.replace('_', ' ');
            playlistName = Stream.of(playlistName.split(" "))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));

            playlistNameLabel.setText(playlistName);
            playlistNameLabel.setVisible(true);
        } else {
            playlistNameLabel.setVisible(false);
        }
    }


}