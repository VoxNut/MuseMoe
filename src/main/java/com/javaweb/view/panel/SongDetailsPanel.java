package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.enums.PlaylistSourceType;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.*;
import com.javaweb.view.HomePage;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.dialog.PlaylistSelectionDialog;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Slf4j
public class SongDetailsPanel extends JPanel implements ThemeChangeListener, PlayerEventListener {
    private final MusicPlayerFacade playerFacade;
    private SongDTO currentSong;
    private JPanel topSection;
    private JPanel coverPanel;
    private JPanel metadataPanel;
    private JPanel controlsPanel;
    private JPanel lyricsPanel;
    private JPanel artistsPanel;
    private JPanel bottomSection;

    private AsyncImageLabel coverLabel;
    private JLabel songTypeLabel;
    private JLabel songTitleLabel;
    private JLabel artistLabel;
    private JLabel albumLabel;
    private JLabel yearLabel;
    private JLabel durationLabel;
    private JLabel playsLabel;
    private JTextArea lyricsTextArea;

    private JButton playPauseButton;
    private JButton addToPlaylistButton;
    private JButton downloadButton;
    private JButton moreOptionsButton;

    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    public SongDetailsPanel() {
        playerFacade = App.getBean(MusicPlayerFacade.class);
        playerFacade.subscribeToPlayerEvents(this);

        initializeComponents();

        ThemeManager.getInstance().addThemeChangeListener(this);
        onThemeChanged(
                ThemeManager.getInstance().getBackgroundColor(),
                ThemeManager.getInstance().getTextColor(),
                ThemeManager.getInstance().getAccentColor()
        );
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Main content with scroll
        JPanel contentPanel = createMainContentPanel();
        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMainContentPanel() {
        JPanel contentPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap",
                "[fill]",
                "[]20[]"  // Just two main rows: top section and bottom section
        ));

        // Top Section: Album Cover + Metadata + Controls
        topSection = createTopSection();
        contentPanel.add(topSection, "grow");

        // Add a separator line
        JSeparator separator = GuiUtil.createSeparator();
        contentPanel.add(separator, "growx, gaptop 10, gapbottom 10");

        // Bottom Section: Lyrics + Artist Info
        bottomSection = createBottomSection();
        contentPanel.add(bottomSection, "grow");

        return contentPanel;
    }

    private JPanel createTopSection() {
        JPanel section = GuiUtil.createPanel(new MigLayout(
                "fillx, gap 20",
                "[][grow]",
                "[]"  // Single row layout
        ));

        // Album Cover (left side)
        coverPanel = GuiUtil.createPanel(new BorderLayout());
        coverLabel = new AsyncImageLabel(250, 250, 15);
        coverLabel.startLoading();
        playerFacade.populateSongImage(currentSong, image -> coverLabel.setLoadedImage(image));

        coverPanel.add(coverLabel, BorderLayout.CENTER);

        // Metadata and Controls Panel (right side)
        JPanel rightSidePanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]25[]"
        ));

        // Metadata Panel
        metadataPanel = createMetadataPanel();
        rightSidePanel.add(metadataPanel, "grow");

        // Controls Panel (now inside top section)
        controlsPanel = createControlsPanel();
        rightSidePanel.add(controlsPanel, "grow");

        // Add panels to section
        section.add(coverPanel, "cell 0 0");
        section.add(rightSidePanel, "cell 1 0, grow");

        return section;
    }

    private JPanel createMetadataPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]5[]10[]"
        ));

        songTypeLabel = GuiUtil.createLabel("SONG", Font.BOLD, 14);
        songTitleLabel = GuiUtil.createLabel("Song Title", Font.BOLD, 32);

        // Metadata line
        JPanel metadataLine = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        artistLabel = GuiUtil.createLabel("Artist", Font.PLAIN, 18);


        JLabel separator1 = GuiUtil.createLabel("•", Font.PLAIN, 16);
        albumLabel = GuiUtil.createLabel("Album", Font.PLAIN, 16);
        albumLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        albumLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseAdapter) {
                if (currentSong.getAlbumId() != null) {
                    HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SongDetailsPanel.this);
                    AlbumDTO albumDTO = CommonApiUtil.fetchAlbumById(currentSong.getAlbumId());
                    homePage.navigateToAlbumView(albumDTO);
                }
            }
        });

        JLabel separator2 = GuiUtil.createLabel("•", Font.PLAIN, 16);
        yearLabel = GuiUtil.createLabel("2023", Font.PLAIN, 16);

        JLabel separator3 = GuiUtil.createLabel("•", Font.PLAIN, 16);
        durationLabel = GuiUtil.createLabel("3:30", Font.PLAIN, 16);

        JLabel separator4 = GuiUtil.createLabel("•", Font.PLAIN, 16);
        playsLabel = GuiUtil.createLabel("1,234 plays", Font.PLAIN, 16);

        // Add all metadata components
        metadataLine.add(artistLabel);
        metadataLine.add(separator1);
        metadataLine.add(albumLabel);
        metadataLine.add(separator2);
        metadataLine.add(yearLabel);
        metadataLine.add(separator3);
        metadataLine.add(durationLabel);
        metadataLine.add(separator4);
        metadataLine.add(playsLabel);

        panel.add(songTypeLabel);
        panel.add(songTitleLabel);
        panel.add(metadataLine);

        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));

        // Play button
        playPauseButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 50, 50);
        playPauseButton.addActionListener(e -> handlePlayButtonClick());
        GuiUtil.setSmartTooltip(playPauseButton, "Play");

        // Add to playlist button
        addToPlaylistButton = GuiUtil.changeButtonIconColor(AppConstant.ADD_TO_PLAYLIST_ICON_PATH, 36, 36);
        addToPlaylistButton.addActionListener(e -> handleAddToPlaylistClick());
        GuiUtil.setSmartTooltip(addToPlaylistButton, "Add song to playlist");
        // Download button
        downloadButton = GuiUtil.changeButtonIconColor(AppConstant.DOWNLOAD_ICON_PATH, 36, 36);
        downloadButton.addActionListener(e -> handleDownloadClick());
        GuiUtil.setSmartTooltip(downloadButton, "Download song");
        // More options button
        moreOptionsButton = GuiUtil.changeButtonIconColor(AppConstant.MORE_ICON_PATH, 36, 36);
        moreOptionsButton.addActionListener(e -> handleMoreOptionsClick());
        GuiUtil.setSmartTooltip(moreOptionsButton, "More options");
        // Add buttons
        panel.add(playPauseButton);
        panel.add(addToPlaylistButton);
        panel.add(downloadButton);
        panel.add(moreOptionsButton);

        return panel;
    }

    private JPanel createBottomSection() {
        JPanel section = GuiUtil.createPanel(new MigLayout(
                "fill, insets 0, gap 0!",
                "[60%!][40%!]",
                "[]"));


        // Lyrics Panel (left column)
        lyricsPanel = createLyricsPanel();

        // Artists Panel (right column)
        artistsPanel = createArtistsPanel();

        // Simple cell constraints with grow

        section.add(lyricsPanel, "width 60%!, spany, growy");
        section.add(artistsPanel, "width 40%!, spany, growy");

        return section;
    }

    private JPanel createLyricsPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]10[]"
        ));

        JLabel lyricsHeader = GuiUtil.createLabel("Lyrics", Font.BOLD, 22);

        lyricsTextArea = GuiUtil.createTextArea("", FontUtil.getSpotifyFont(Font.PLAIN, 16));

        lyricsTextArea.setLineWrap(true);
        lyricsTextArea.setWrapStyleWord(true);
        lyricsTextArea.setEditable(false);

        panel.add(lyricsHeader);
        panel.add(lyricsTextArea, "grow");

        return panel;
    }

    private JPanel createArtistsPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[center, grow]",
                "[]10[]"
        ));

        JLabel artistsHeader = GuiUtil.createLabel("Artists", Font.BOLD, 22);
        panel.add(artistsHeader, "center");


        return panel;
    }

    public void displaySong(SongDTO song) {
        this.currentSong = song;

        // Update song metadata
        songTitleLabel.setText(song.getTitle());
        artistLabel.setText(song.getSongArtist());

        if (song.getSongAlbum() != null && !song.getSongAlbum().isEmpty()) {
            albumLabel.setText(song.getSongAlbum());
        } else {
            albumLabel.setText("Single");
        }

        yearLabel.setText(song.getReleaseYear() != null ? song.getReleaseYear().toString() : "");
        durationLabel.setText(song.getSongLength());

        String formattedPlays = NumberFormat.getNumberInstance(Locale.US).format(song.getPlayCount());
        playsLabel.setText(formattedPlays + " plays");

        // Load album cover
        playerFacade.populateSongImage(song, coverLabel::setLoadedImage);
        if (currentSong != null) {
            GuiUtil.addSongContextMenu(coverLabel, currentSong);
        }


        // Update lyrics
        lyricsTextArea.setText(song.getSongLyrics() != null && !song.getSongLyrics().isEmpty() ? song.getSongLyrics() : "No lyrics available");

        // Update play button state
        updatePlayButtonState();

        // Load artists info
        updateArtistsPanel(song);
    }

    private void updateArtistsPanel(SongDTO song) {
        // Clear existing artist cards
        Component[] comps = artistsPanel.getComponents();
        for (int i = 1; i < comps.length; i++) { // Skip header
            artistsPanel.remove(comps[i]);
        }

        if (song.getArtistDTOs() != null && !song.getArtistDTOs().isEmpty()) {
            List<ArtistDTO> artists = song.getArtistDTOs();
            for (ArtistDTO artist : artists) {
                JPanel artistCard = createArtistCard(artist);
                artistsPanel.add(artistCard, "grow,wrap");
            }
        } else {
            // Show placeholder for unknown artist
            JLabel unknownLabel = new JLabel("Artist information not available");
            unknownLabel.setHorizontalAlignment(SwingConstants.CENTER);
            artistsPanel.add(unknownLabel);
        }

        artistsPanel.revalidate();
        artistsPanel.repaint();
    }

    private JPanel createArtistCard(ArtistDTO artist) {
        JPanel card = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 10",
                "[center]",
                "[]5[]5[]"
        ));

        // Circular artist profile image
        AsyncImageLabel profileLabel = GuiUtil.createArtistProfileLabel(120);
        profileLabel.startLoading();
        playerFacade.populateArtistProfile(artist, profileLabel::setLoadedImage);

        // Artist label
        JLabel typeLabel = GuiUtil.createLabel("ARTIST", Font.PLAIN, 12);
        typeLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        // Artist name
        JLabel nameLabel = GuiUtil.createLabel(artist.getStageName(), Font.BOLD, 16);

        card.add(profileLabel);
        card.add(typeLabel);
        card.add(nameLabel);


        GuiUtil.addHoverEffect(card);

        profileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    log.info("Artist clicked: {}", artist.getStageName());
                    HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SongDetailsPanel.this);
                    if (homePage != null) {
                        homePage.navigateToArtistView(artist);
                    }
                }

            }
        });

        return card;
    }

    private void updatePlayButtonState() {
        if (currentSong == null) return;

        boolean isCurrentSong = isSameSong(currentSong, playerFacade.getCurrentSong());
        boolean isPlaying = isCurrentSong && !playerFacade.isPaused();

        String iconPath = isPlaying ? AppConstant.PAUSE_ICON_PATH : AppConstant.PLAY_ICON_PATH;
        playPauseButton.setIcon(GuiUtil.createColoredIcon(iconPath, textColor, 50, 50));
        playPauseButton.setRolloverIcon(GuiUtil.createColoredIcon(
                iconPath, GuiUtil.lightenColor(textColor, 0.3f), 50, 50
        ));

        GuiUtil.setSmartTooltip(playPauseButton, isPlaying ? "Pause" : "Play");
    }

    private void handlePlayButtonClick() {
        if (currentSong == null) return;

        SongDTO playing = playerFacade.getCurrentSong();

        if (isSameSong(currentSong, playing)) {
            if (playerFacade.isPaused()) {
                playerFacade.playCurrentSong();
            } else {
                playerFacade.pauseSong();
            }
        } else {
            if (currentSong.getIsLocalFile()) {
                playerFacade.loadLocalSong(currentSong);
            } else {
                AlbumDTO albumDTO = CommonApiUtil.fetchAlbumContainsThisSong(currentSong.getId());
                playerFacade.loadSongWithContext(
                        currentSong,
                        playerFacade.convertSongListToPlaylist(albumDTO.getSongDTOS(), albumDTO.getTitle()),
                        PlaylistSourceType.ALBUM
                );
            }
        }

        updatePlayButtonState();
    }

    private void handleAddToPlaylistClick() {
        if (currentSong == null) return;

        // Show playlist selection dialog
        List<PlaylistDTO> userPlaylists = CommonApiUtil.fetchPlaylistByUserId();

        if (userPlaylists.isEmpty()) {
            GuiUtil.showToast(SwingUtilities.getWindowAncestor(this),
                    "You don't have any playlists yet. Create one first.");
            return;
        }
        PlaylistSelectionDialog playlistSelectionDialog = new PlaylistSelectionDialog(SwingUtilities.getWindowAncestor(this), currentSong);
        playlistSelectionDialog.setVisible(true);

    }

    private void handleDownloadClick() {
        if (currentSong == null || currentSong.getIsLocalFile()) return;

        int result = GuiUtil.showConfirmMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Download " + StringUtils.getTruncatedText(currentSong.getTitle()) + ".mp3?",
                "Confirm Download"
        );

        if (result == JOptionPane.YES_OPTION) {
            SongDownloadUtil.downloadSong(
                    SwingUtilities.getWindowAncestor(this),
                    currentSong
            );
        }
    }

    private void handleMoreOptionsClick() {
        if (currentSong == null) return;

        // Create popup menu with options
        JPopupMenu menu = GuiUtil.createPopupMenu(backgroundColor, textColor);

        JMenuItem viewAlbumItem = GuiUtil.createMenuItem("View Album");
        viewAlbumItem.addActionListener(e -> {
            if (currentSong.getAlbumId() != null) {
                HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(this);
                AlbumDTO albumDTO = CommonApiUtil.fetchAlbumById(currentSong.getAlbumId());
                homePage.navigateToAlbumView(albumDTO);
            }
        });

        JMenuItem addToQueueItem = GuiUtil.createMenuItem("Add to Queue");
        addToQueueItem.addActionListener(e -> {
            playerFacade.addToQueueNext(currentSong);
            GuiUtil.showToast(this, "Added to queue");
        });

        JMenuItem shareItem = GuiUtil.createMenuItem("Share");
        shareItem.addActionListener(e -> {
            // Implement sharing functionality
            String shareUrl = "https://musemoe.com/song/" + currentSong.getId();
            StringSelection selection = new StringSelection(shareUrl);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            GuiUtil.showToast(
                    SwingUtilities.getWindowAncestor(this),
                    "Link copied to clipboard!"
            );
        });
        Component component = SwingUtilities.getWindowAncestor(this);
        boolean isLiked = CommonApiUtil.checkSongLiked(currentSong.getId());
        JMenuItem likeItem = GuiUtil.createMenuItem(isLiked ? "Unlike" : "Like");
        likeItem.addActionListener(e -> {
            if (isLiked) {
                if (CommonApiUtil.deleteSongLikes(currentSong.getId())) {
                    App.getBean(MusicPlayerFacade.class).notifySongLiked();
                    GuiUtil.showToast(component, "Removed from liked songs");
                } else {
                    GuiUtil.showToast(component, "Failed to unlike song");
                }
            } else {
                if (CommonApiUtil.createSongLikes(currentSong.getId())) {
                    App.getBean(MusicPlayerFacade.class).notifySongLiked();
                    GuiUtil.showToast(component, "Added to liked songs");
                } else {
                    GuiUtil.showToast(component, "Failed to like song");
                }
            }
        });

        // Add items to menu
        menu.add(viewAlbumItem);
        menu.add(addToQueueItem);
        menu.add(shareItem);
        menu.add(likeItem);


        // Show context menu where the button is
        menu.show(moreOptionsButton, 0, moreOptionsButton.getHeight());
    }

    private boolean isSameSong(SongDTO song1, SongDTO song2) {
        if (song1 == null || song2 == null) return false;

        if (song1.getIsLocalFile() && song2.getIsLocalFile()) {
            return song1.getLocalFilePath() != null && song1.getLocalFilePath().equals(song2.getLocalFilePath());
        }

        return song1.getId() != null && song2.getId() != null && song1.getId().equals(song2.getId());
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);
        // Update play button icon color
        updatePlayButtonState();

        repaint();
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        switch (event.type()) {
            case PLAYBACK_STARTED -> {
                SongDTO song = (SongDTO) event.data();
                if (isSameSong(song, currentSong)) {
                    updatePlayButtonState();
                }
            }
            case PLAYBACK_PAUSED -> {
                SongDTO song = (SongDTO) event.data();
                if (isSameSong(song, currentSong)) {
                    updatePlayButtonState();
                }
            }
            case PLAYBACK_STOPPED -> updatePlayButtonState();
        }
    }

    public void cleanup() {
        playerFacade.unsubscribeFromPlayerEvents(this);
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}