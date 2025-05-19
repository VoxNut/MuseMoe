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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class ArtistProfilePanel extends JPanel implements ThemeChangeListener, PlayerEventListener {
    private final MusicPlayerFacade playerFacade;
    private ArtistDTO currentArtist;
    private List<SongDTO> popularTracks;
    private List<AlbumDTO> artistAlbums;

    // UI components
    private JPanel headerPanel;
    private JPanel tracksPanel;
    private JPanel albumsPanel;
    private JPanel mainContentPanel;

    // Header components
    private AsyncImageLabel artistHeaderImage;
    private JLabel artistNameLabel;
    private JLabel verifiedArtistLabel;
    private JLabel listenerCountLabel;

    // Controls
    private JButton playButton;
    private JButton followButton;
    private JButton moreOptionsButton;

    // Bio
    private JTextArea bioTextArea;

    // Tracks table
    private JTable popularTracksTable;
    private DefaultTableModel tracksTableModel;
    private Map<SongDTO, JButton> playPauseButtonMap = new HashMap<>();
    private int hoveredRow = -1;

    // Theme colors
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    public ArtistProfilePanel() {
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

        // Create main content with scroll
        mainContentPanel = createMainContentPanel();
        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(mainContentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMainContentPanel() {
        JPanel content = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow,fill]",
                "[]15[]15[]"
        ));
        // Header with artist image, info, controls, and bio
        headerPanel = createHeaderPanel();
        content.add(headerPanel, "grow");

        // Popular tracks section
        tracksPanel = createTracksPanel();
        content.add(tracksPanel, "growx");

        // Albums section
        albumsPanel = createAlbumsPanel();
        content.add(albumsPanel, "growx");

        return content;
    }

    private JPanel createHeaderPanel() {
        headerPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, gap 40, insets 0",
                "[250!,center][grow,fill]",
                "[]"
        ));

        // Left side: Artist image
        JPanel coverPanel = GuiUtil.createPanel(new BorderLayout());

        // Circular artist image
        artistHeaderImage = new AsyncImageLabel(250, 250, 15, true);

        artistHeaderImage.startLoading();
        coverPanel.add(artistHeaderImage, BorderLayout.CENTER);
        coverPanel.setPreferredSize(new Dimension(250, 250));

        // Right side: split into two columns
        JPanel rightPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, gap 20, insets 0",
                "[grow,fill][grow,fill]",
                "[grow,fill]"
        ));

        // Left column of right side: Info + Controls
        JPanel leftColumnPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]15[]"  // Info panel and controls panel
        ));

        // Info panel (artist details)
        JPanel infoPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]5[]5[]"
        ));

        // Verified artist badge
        verifiedArtistLabel = GuiUtil.createLabel("VERIFIED ARTIST", Font.BOLD, 14);
        verifiedArtistLabel.setIcon(GuiUtil.createColoredIcon(AppConstant.VERIFIED_ICON_PATH,
                GuiUtil.darkenColor(ThemeManager.getInstance().getTextColor(), 0.3f), 16, 16));

        // Artist name
        artistNameLabel = GuiUtil.createLabel("Artist Name", Font.BOLD, 32);

        // Listener count
        listenerCountLabel = GuiUtil.createLabel("0 listeners", Font.PLAIN, 14);
        listenerCountLabel.setForeground(GuiUtil.darkenColor(ThemeManager.getInstance().getTextColor(), 0.3f));

        // Add components to info panel
        infoPanel.add(verifiedArtistLabel);
        infoPanel.add(artistNameLabel);
        infoPanel.add(listenerCountLabel);

        // Controls panel (buttons)
        JPanel buttonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));

        // Play button
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 50, 50);
        playButton.addActionListener(this::handlePlayButtonClick);

        // Follow button
        followButton = GuiUtil.createButton("Follow");
        followButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 16));
        followButton.addActionListener(e -> handleFollowButtonClick());

        // More options button
        moreOptionsButton = GuiUtil.changeButtonIconColor(AppConstant.MORE_ICON_PATH, 36, 36);
        moreOptionsButton.addActionListener(e -> handleMoreOptionsClick());

        // Add buttons
        buttonsPanel.add(playButton);
        buttonsPanel.add(followButton);
        buttonsPanel.add(moreOptionsButton);

        // Add info and buttons panels to the left column
        leftColumnPanel.add(infoPanel, "grow");
        leftColumnPanel.add(buttonsPanel, "grow");

        // Right column of right side: Bio panel
        JPanel bioPanel = createBioPanel();

        // Add both columns to right panel
        rightPanel.add(leftColumnPanel, "grow");
        rightPanel.add(bioPanel, "grow");

        // Add artist image and right panel to header
        headerPanel.add(coverPanel, "cell 0 0");
        headerPanel.add(rightPanel, "cell 1 0, grow");

        return headerPanel;
    }


    private JPanel createBioPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]10[]"
        ));

        JLabel aboutLabel = GuiUtil.createLabel("About", Font.BOLD, 22);

        bioTextArea = GuiUtil.createTextArea("", FontUtil.getSpotifyFont(Font.PLAIN, 16));
        bioTextArea.setWrapStyleWord(true);
        bioTextArea.setLineWrap(true);
        bioTextArea.setMargin(new Insets(0, 0, 5, 0));

        panel.add(aboutLabel);
        panel.add(bioTextArea, "grow");

        return panel;
    }

    private JPanel createTracksPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 20",
                "[grow]",
                "[]5[]"
        ));

        JLabel popularLabel = GuiUtil.createLabel("Popular", Font.BOLD, 22);

        // Create tracks table
        String[] columnNames = {"#", "TITLE", "PLAYS", "DURATION", ""};
        int[] columnWidths = {50, 0, 150, 100, 50};

        popularTracksTable = GuiUtil.createStyledTable(columnNames, columnWidths);
        tracksTableModel = (DefaultTableModel) popularTracksTable.getModel();

        popularTracksTable.getTableHeader().setVisible(true);
        popularTracksTable.setPreferredScrollableViewportSize(new Dimension(0, 200));

        // Track mouse over for hovering effect
        popularTracksTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = popularTracksTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    popularTracksTable.repaint();
                }
            }
        });

        popularTracksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                popularTracksTable.repaint();
            }
        });

        popularTracksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && popularTracks != null && !popularTracks.isEmpty()) {
                    int row = popularTracksTable.getSelectedRow();
                    if (row < 0 || row >= popularTracks.size()) return;

                    SongDTO selectedSong = popularTracks.get(row);
                    SongDTO currentSong = playerFacade.getCurrentSong();

                    if (isSameSong(selectedSong, currentSong)) {
                        if (playerFacade.isPaused()) {
                            playerFacade.playCurrentSong();
                        } else {
                            playerFacade.pauseSong();
                        }
                    } else {
                        PlaylistDTO artistPlaylist = playerFacade.convertSongListToPlaylist(
                                popularTracks,
                                currentArtist.getStageName() + " - Popular"
                        );
                        playerFacade.loadSongWithContext(
                                selectedSong,
                                artistPlaylist,
                                PlaylistSourceType.POPULAR
                        );
                    }
                } else if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e) && popularTracks != null && !popularTracks.isEmpty()) {
                    int row = popularTracksTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < popularTracks.size()) {
                        if (!popularTracksTable.isRowSelected(row)) {
                            popularTracksTable.setRowSelectionInterval(row, row);
                        }
                        SongDTO song = popularTracks.get(row);

                        JPopupMenu contextMenu = createSongContextMenu(song);
                        contextMenu.show(popularTracksTable, e.getX(), e.getY());
                    }
                }
            }
        });

        popularTracksTable.getColumnModel().getColumn(0).setCellRenderer((table, value, isSel, hasFocus, row, col) -> {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(true);

            SongDTO song = popularTracks.get(row);
            if (row == hoveredRow) {
                JButton btn = playPauseButtonMap.computeIfAbsent(song, s -> {
                    JButton b = new JButton();
                    b.setBorder(null);
                    b.setContentAreaFilled(false);

                    boolean isCurrentSong = isSameSong(s, playerFacade.getCurrentSong());
                    boolean isPlaying = isCurrentSong && !playerFacade.isPaused();
                    String iconPath = isPlaying ? AppConstant.PAUSE_ICON_PATH : AppConstant.PLAY_ICON_PATH;
                    b.setIcon(GuiUtil.createColoredIcon(iconPath, textColor, 16, 16));

                    b.addActionListener(a -> {
                        SongDTO cur = playerFacade.getCurrentSong();
                        if (isSameSong(cur, s)) {
                            if (playerFacade.isPaused()) playerFacade.playCurrentSong();
                            else playerFacade.pauseSong();
                        } else {
                            PlaylistDTO artistPlaylist = playerFacade.convertSongListToPlaylist(
                                    popularTracks,
                                    currentArtist.getStageName() + " - Popular"
                            );
                            playerFacade.loadSongWithContext(
                                    cur,
                                    artistPlaylist,
                                    PlaylistSourceType.POPULAR
                            );
                        }
                    });
                    return b;
                });

                cell.add(btn, BorderLayout.CENTER);
            } else {
                JLabel lbl = new JLabel(String.valueOf(value), SwingConstants.CENTER);
                lbl.setForeground(textColor);
                cell.add(lbl, BorderLayout.CENTER);
            }
            return cell;
        });

        panel.add(popularLabel);
        panel.add(popularTracksTable, "grow");

        return panel;
    }

    private JPanel createAlbumsPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 20",
                "[grow]",
                "[]15[]"
        ));

        JLabel albumsLabel = GuiUtil.createLabel("Albums", Font.BOLD, 22);

        // Create albums grid
        JPanel albumsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 7, fillx, gapx 15, gapy 15",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        panel.add(albumsLabel);
        panel.add(albumsGrid, "grow");

        return panel;
    }

    private void updateHeaderPlayButton() {
        boolean isFromThisContext = popularTracks != null && popularTracks.stream()
                .anyMatch(song -> isSameSong(song, playerFacade.getCurrentSong()));

        if (isFromThisContext && !playerFacade.isPaused()) {
            playButton.setIcon(GuiUtil.createColoredIcon(
                    AppConstant.PAUSE_ICON_PATH, textColor, 50, 50
            ));
            playButton.setRolloverIcon(GuiUtil.createColoredIcon(
                    AppConstant.PAUSE_ICON_PATH, GuiUtil.lightenColor(textColor, 0.3f), 50, 50
            ));
        } else {
            // For all other cases - show play button
            playButton.setIcon(GuiUtil.createColoredIcon(
                    AppConstant.PLAY_ICON_PATH, textColor, 50, 50
            ));
            playButton.setRolloverIcon(GuiUtil.createColoredIcon(
                    AppConstant.PLAY_ICON_PATH, GuiUtil.lightenColor(textColor, 0.3f), 50, 50
            ));
        }
    }

    public void displayArtist(ArtistDTO artist) {
        this.currentArtist = artist;

        // Update header
        artistNameLabel.setText(artist.getStageName());

        // Format listener count with commas
        listenerCountLabel.setText(NumberFormatUtil.formatWithCommas(currentArtist.getFollowerCount()) +
                ((currentArtist.getFollowerCount() > 1) ? " listeners" : " listener"));

        // Load artist image
        playerFacade.populateArtistProfile(artist, artistHeaderImage::setLoadedImage);

        // Update follow button
        updateFollowButtonState();

        // Load artist bio
        bioTextArea.setText(artist.getBio());

        // Load popular tracks
        loadPopularTracks(artist.getId());

        // Load albums
        loadArtistAlbums(artist.getId());

        updateHeaderPlayButton();
    }

    private void loadPopularTracks(Long artistId) {
        // Clear existing tracks
        tracksTableModel.setRowCount(0);
        playPauseButtonMap.clear();

        try {
            popularTracks = CommonApiUtil.fetchPopularTracksByArtistId(artistId);

            if (popularTracks == null || popularTracks.isEmpty()) {
                JLabel noTracksLabel = GuiUtil.createLabel("No tracks available", Font.ITALIC, 14);
                tracksPanel.add(noTracksLabel);
            } else {
                // Populate table with tracks
                for (int i = 0; i < popularTracks.size(); i++) {
                    SongDTO song = popularTracks.get(i);

                    // Format play count with commas
                    NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
                    String formattedPlays = formatter.format(song.getPlayCount());

                    tracksTableModel.addRow(new Object[]{
                            i + 1,
                            song.getTitle(),
                            formattedPlays,
                            song.getSongLength(),
                            ""
                    });
                }
            }

            // Check if any song is currently playing
            if (playerFacade.getCurrentSong() != null) {
                highlightPlayingSong(playerFacade.getCurrentSong());
            }

        } catch (Exception e) {
            log.error("Error loading popular tracks", e);
        }

    }

    private void loadArtistAlbums(Long artistId) {
        // Get the albums panel component
        JPanel albumsGrid = (JPanel) albumsPanel.getComponent(1);
        albumsGrid.removeAll();

        SwingWorker<List<AlbumDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AlbumDTO> doInBackground() {
                return CommonApiUtil.fetchAlbumsByArtistId(artistId);
            }

            @Override
            protected void done() {
                try {
                    artistAlbums = get();

                    if (artistAlbums == null || artistAlbums.isEmpty()) {
                        JLabel noAlbumsLabel = GuiUtil.createLabel("No albums available", Font.ITALIC, 14);
                        albumsGrid.add(noAlbumsLabel, "span");
                    } else {
                        // Add albums to grid
                        for (AlbumDTO album : artistAlbums) {
                            albumsGrid.add(createAlbumCard(album));
                        }
                    }

                    albumsGrid.revalidate();
                    albumsGrid.repaint();

                } catch (Exception e) {
                    log.error("Error loading artist albums", e);
                }
            }
        };

        worker.execute();
    }

    private JPanel createAlbumCard(AlbumDTO album) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Album cover
        AsyncImageLabel albumCover = GuiUtil.createAsyncImageLabel(150, 150, 15);
        albumCover.startLoading();
        albumCover.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerFacade.populateAlbumImage(album, albumCover::setLoadedImage);

        // Album title
        Font titleFont = FontUtil.getSpotifyFont(Font.BOLD, 12);
        JLabel titleLabel = GuiUtil.createLabel(
                StringUtils.getTruncatedTextByWidth(album.getTitle(), titleFont, 150),
                Font.BOLD, 12);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setToolTipText(album.getTitle());

        // Release year
        JLabel yearLabel = GuiUtil.createLabel(String.valueOf(album.getReleaseYear()), Font.PLAIN, 11);
        yearLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        card.add(albumCover);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(yearLabel);

        // Add hover effect and click handler
        GuiUtil.addHoverEffect(card);
        albumCover.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    log.info("Album clicked: {}", album.getTitle());
                    HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(ArtistProfilePanel.this);
                    homePage.navigateToAlbumView(album);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    GuiUtil.addAlbumContextMenu(albumCover, album);
                }
            }
        });

        return card;
    }


    private JPopupMenu createSongContextMenu(SongDTO song) {
        JPopupMenu contextMenu = GuiUtil.createPopupMenu(backgroundColor, textColor);

        // View Details option
        JMenuItem viewDetailsItem = GuiUtil.createMenuItem("View Details");
        viewDetailsItem.addActionListener(e -> {
            HomePage homePage = GuiUtil.findHomePageInstance(this);
            if (homePage != null) {
                homePage.navigateToSongDetailsView(song);
            }
        });
        contextMenu.add(viewDetailsItem);

        // Play option
        JMenuItem playItem = GuiUtil.createMenuItem("Play");
        playItem.addActionListener(e -> {
            // Create artist playlist
            PlaylistDTO artistPlaylist = playerFacade.convertSongListToPlaylist(
                    popularTracks,
                    currentArtist.getStageName() + " - Popular"
            );
            playerFacade.loadSongWithContext(
                    song,
                    artistPlaylist,
                    PlaylistSourceType.POPULAR
            );
        });
        contextMenu.add(playItem);

        JMenuItem addToQueueItem = GuiUtil.createMenuItem("Add to Queue");
        addToQueueItem.addActionListener(e -> {
            playerFacade.addToQueueNext(song);
            GuiUtil.showToast(this, "Added to queue");
        });

        // Add to playlist option
        JMenuItem addToPlaylistItem = GuiUtil.createMenuItem("Add to Playlist");
        addToPlaylistItem.addActionListener(e -> {
            PlaylistSelectionDialog dialog = new PlaylistSelectionDialog(
                    SwingUtilities.getWindowAncestor(this),
                    song
            );
            dialog.setVisible(true);
        });
        contextMenu.add(addToPlaylistItem);

        // Like/Unlike option
        boolean isLiked = CommonApiUtil.checkSongLiked(song.getId());
        JMenuItem likeItem = GuiUtil.createMenuItem(isLiked ? "Unlike" : "Like");
        likeItem.addActionListener(e -> {
            if (isLiked) {
                if (CommonApiUtil.deleteSongLikes(song.getId())) {
                    playerFacade.notifySongLiked();
                    GuiUtil.showToast(this, "Removed from liked songs");
                    popularTracksTable.repaint();
                } else {
                    GuiUtil.showToast(this, "Failed to unlike song");
                }
            } else {
                if (CommonApiUtil.createSongLikes(song.getId())) {
                    playerFacade.notifySongLiked();
                    GuiUtil.showToast(this, "Added to liked songs");
                    popularTracksTable.repaint();
                } else {
                    GuiUtil.showToast(this, "Failed to like song");
                }
            }
        });
        contextMenu.add(likeItem);

        // Download option
        if (SongDownloadUtil.hasDownloadPermission()) {
            JMenuItem downloadItem = GuiUtil.createMenuItem("Download");
            downloadItem.addActionListener(e -> SongDownloadUtil.downloadSong(this, song));
            contextMenu.add(downloadItem);
        }

        GuiUtil.registerPopupMenuForThemeUpdates(contextMenu);
        return contextMenu;
    }

    private void handlePlayButtonClick(ActionEvent e) {
        if (popularTracks == null || popularTracks.isEmpty()) {
            return;
        }

        SongDTO currentSong = playerFacade.getCurrentSong();
        boolean isFromThisArtist = false;

        // Check if currently playing song is from this artist
        if (currentSong != null && popularTracks.stream().anyMatch(song -> isSameSong(song, currentSong))) {
            isFromThisArtist = true;
        }

        // If we're already playing from this artist, toggle play/pause
        if (isFromThisArtist) {
            if (playerFacade.isPaused()) {
                playerFacade.playCurrentSong();
            } else {
                playerFacade.pauseSong();
            }
            return;
        }

        // Otherwise, start playing the first song
        SongDTO firstSong = popularTracks.getFirst();
        PlaylistDTO artistPlaylist = playerFacade.convertSongListToPlaylist(
                popularTracks,
                currentArtist.getStageName() + " - Popular"
        );

        playerFacade.loadSongWithContext(
                firstSong,
                artistPlaylist,
                PlaylistSourceType.POPULAR
        );
    }

    private void handleFollowButtonClick() {
        if (currentArtist == null) return;

        boolean isFollowing = CommonApiUtil.checkArtistFollowed(currentArtist.getId());

        if (isFollowing) {
            // Unfollow artist
            if (CommonApiUtil.unfollowArtist(currentArtist.getId())) {
                GuiUtil.showToast(this, "Unfollowed " + currentArtist.getStageName());
                updateFollowButtonState();
                // Update the count display
                currentArtist.setFollowerCount(currentArtist.getFollowerCount() - 1);
                listenerCountLabel.setText(NumberFormatUtil.formatWithCommas(currentArtist.getFollowerCount()) +
                        ((currentArtist.getFollowerCount() > 1) ? " listeners" : " listener"));
            } else {
                GuiUtil.showToast(this, "Failed to unfollow artist");
            }
        } else {
            // Follow artist
            if (CommonApiUtil.followArtist(currentArtist.getId())) {
                GuiUtil.showToast(this, "Following " + currentArtist.getStageName());
                updateFollowButtonState();
                // Update the count display
                currentArtist.setFollowerCount(currentArtist.getFollowerCount() + 1);
                listenerCountLabel.setText(NumberFormatUtil.formatWithCommas(currentArtist.getFollowerCount()) +
                        ((currentArtist.getFollowerCount() > 1) ? " listeners" : " listener"));
            } else {
                GuiUtil.showToast(this, "Failed to follow artist");
            }
        }
    }

    private void handleMoreOptionsClick() {
        if (currentArtist == null) return;

        JPopupMenu menu = GuiUtil.createPopupMenu(backgroundColor, textColor);

        // Share artist
        JMenuItem shareItem = GuiUtil.createMenuItem("Share");
        shareItem.addActionListener(e -> {
            String shareUrl = "https://musemoe.com/artist/" + currentArtist.getId();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(shareUrl), null
            );
            GuiUtil.showToast(this, "Artist link copied to clipboard");
        });
        menu.add(shareItem);

        // Report an issue
        JMenuItem reportItem = GuiUtil.createMenuItem("Report");
        reportItem.addActionListener(e -> GuiUtil.showInfoMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Thank you for your feedback. We'll review the artist information."
        ));
        menu.add(reportItem);

        menu.show(moreOptionsButton, 0, moreOptionsButton.getHeight());
    }

    private void updateFollowButtonState() {
        if (currentArtist == null) return;

        boolean isFollowing = CommonApiUtil.checkArtistFollowed(currentArtist.getId());

        if (isFollowing) {
            followButton.setText("Following");
        } else {
            followButton.setText("Follow");
        }
    }

    private void updatePlayButtonState() {
        if (popularTracks == null || popularTracks.isEmpty()) return;

        SongDTO currentSong = playerFacade.getCurrentSong();
        boolean isPlaying = !playerFacade.isPaused();
        boolean isFromThisArtist = false;

        if (currentSong != null) {
            isFromThisArtist = popularTracks.stream().anyMatch(song -> isSameSong(song, currentSong));
        }

        String iconPath;
        if (isFromThisArtist && isPlaying) {
            iconPath = AppConstant.PAUSE_ICON_PATH;
        } else {
            iconPath = AppConstant.PLAY_ICON_PATH;
        }

        playButton.setIcon(GuiUtil.createColoredIcon(iconPath, textColor, 50, 50));
        playButton.setRolloverIcon(GuiUtil.createColoredIcon(iconPath, GuiUtil.lightenColor(textColor, 0.3f), 50, 50));
    }

    private void updatePlayPauseButtons(SongDTO currentSong, boolean isPlaying) {
        if (currentSong == null) return;

        for (Map.Entry<SongDTO, JButton> entry : playPauseButtonMap.entrySet()) {
            SongDTO song = entry.getKey();
            JButton button = entry.getValue();

            if (isSameSong(song, currentSong)) {
                String iconPath = isPlaying ? AppConstant.PAUSE_ICON_PATH : AppConstant.PLAY_ICON_PATH;
                button.setIcon(GuiUtil.createColoredIcon(iconPath, textColor, 16, 16));
            } else {
                button.setIcon(GuiUtil.createColoredIcon(AppConstant.PLAY_ICON_PATH, textColor, 16, 16));
            }
        }

        updatePlayButtonState();
        popularTracksTable.repaint();
    }

    private void highlightPlayingSong(SongDTO song) {
        if (popularTracks == null || song == null) {
            return;
        }

        for (int i = 0; i < popularTracks.size(); i++) {
            SongDTO track = popularTracks.get(i);
            if (isSameSong(track, song)) {
                popularTracksTable.getSelectionModel().setSelectionInterval(i, i);
                popularTracksTable.scrollRectToVisible(popularTracksTable.getCellRect(i, 0, true));
                break;
            }
        }

        popularTracksTable.repaint();
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

        // Update follow button
        updateFollowButtonState();

        // Update play/pause buttons in table
        for (Map.Entry<SongDTO, JButton> entry : playPauseButtonMap.entrySet()) {
            SongDTO song = entry.getKey();
            JButton button = entry.getValue();

            boolean isCurrentSong = isSameSong(song, playerFacade.getCurrentSong());
            boolean isPlaying = isCurrentSong && !playerFacade.isPaused();
            String iconPath = isPlaying ? AppConstant.PAUSE_ICON_PATH : AppConstant.PLAY_ICON_PATH;
            button.setIcon(GuiUtil.createColoredIcon(iconPath, textColor, 16, 16));
        }

        repaint();
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        switch (event.type()) {
            case PLAYBACK_STARTED -> {
                SongDTO currentSong = (SongDTO) event.data();
                highlightPlayingSong(currentSong);
                updatePlayPauseButtons(currentSong, true);
            }
            case PLAYBACK_PAUSED -> {
                SongDTO currentSong = (SongDTO) event.data();
                updatePlayPauseButtons(currentSong, false);
            }
            case PLAYBACK_STOPPED -> {
                popularTracksTable.clearSelection();
                popularTracksTable.repaint();
                updatePlayButtonState();
            }
        }
    }

    public void cleanup() {
        playerFacade.unsubscribeFromPlayerEvents(this);
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}