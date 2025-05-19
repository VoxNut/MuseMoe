package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.enums.PlaylistSourceType;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.impl.GoogleDriveService;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AlbumViewPanel extends JPanel implements ThemeChangeListener, PlayerEventListener {
    private final MusicPlayerFacade playerFacade;
    private AlbumDTO currentAlbum;
    private PlaylistDTO currentPlaylist;
    private PlaylistSourceType sourceType;
    private JPanel headerPanel;
    private JPanel tracksPanel;
    private JPanel footerPanel;
    private JPanel moreByArtistPanel;
    private JTable tracksTable;
    private DefaultTableModel tableModel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel typeLabel;
    private JLabel detailsLabel;
    private AsyncImageLabel coverLabel;
    private JButton playButton;
    private JButton shuffleButton;
    private JButton addToPlaylistButton;
    private JButton downloadButton;
    private JButton moreOptionsButton;
    private List<SongDTO> tracks;
    private JLabel releaseInfoLabel;
    private JLabel moreByArtistLabel;
    private JPanel albumsGrid;
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;
    private Map<SongDTO, JButton> playPauseButtonMap = new HashMap<>();
    private int hoveredRow = -1;

    public AlbumViewPanel() {
        this.playerFacade = App.getBean(MusicPlayerFacade.class);
        this.backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        this.textColor = ThemeManager.getInstance().getTextColor();
        this.accentColor = ThemeManager.getInstance().getAccentColor();

        initComponents();
        ThemeManager.getInstance().addThemeChangeListener(this);
        playerFacade.subscribeToPlayerEvents(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Main content panel with vertical scrolling
        JPanel contentPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 20",
                "[grow,fill]",
                "[]15[]25[]25[]"
        ));

        // Header section (Album Info)
        createHeaderSection();
        contentPanel.add(headerPanel, "grow");

        // Tracklist section
        createTracksSection();
        contentPanel.add(tracksPanel, "grow");

        // Footer section
        createFooterSection();
        contentPanel.add(footerPanel, "grow");

        // More by artist section
        createMoreByArtistSection();
        contentPanel.add(moreByArtistPanel, "grow");

        // Add scrollable panel
        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createHeaderSection() {
        headerPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, gap 20, insets 0",
                "[250!,center][grow,fill]",
                "[]"
        ));


        // Cover image panel (left side)
        JPanel coverPanel = GuiUtil.createPanel(new BorderLayout());
        coverLabel = GuiUtil.createAsyncImageLabel(250, 250, 15);
        coverLabel.startLoading();

        coverLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    GuiUtil.addAlbumContextMenu(coverLabel, currentAlbum);
                }
            }
        });


        coverPanel.add(coverLabel, BorderLayout.CENTER);
        coverPanel.setPreferredSize(new Dimension(250, 250));

        // Info panel (right side)
        JPanel infoPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]5[]5[]15[]25[]"
        ));


        // Type label
        typeLabel = GuiUtil.createLabel("ALBUM", Font.BOLD, 14);
        typeLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        // Title label
        titleLabel = GuiUtil.createLabel("Album Title", Font.BOLD, 32);

        // Subtitle with artist
        subtitleLabel = GuiUtil.createLabel("Artist Name", Font.PLAIN, 18);
        subtitleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        subtitleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArtistDTO artist = CommonApiUtil.findArtistById(currentAlbum.getArtistId());
                log.info("Artist clicked: {}", artist.getStageName());
                HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(AlbumViewPanel.this);
                if (homePage != null) {
                    homePage.navigateToArtistView(artist);
                }
            }
        });

        // Details (year, songs, duration)
        detailsLabel = GuiUtil.createLabel("2023 • 12 songs • 45 min 20 sec", Font.PLAIN, 14);
        detailsLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        // Buttons panel
        JPanel buttonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));

        // Play button (larger than others)
        playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 50, 50);
        playButton.addActionListener(this::handlePlayButtonClick);

        // Other control buttons
        shuffleButton = GuiUtil.changeButtonIconColor(AppConstant.SHUFFLE_ICON_PATH, 36, 36);
        addToPlaylistButton = GuiUtil.changeButtonIconColor(AppConstant.ADD_TO_PLAYLIST_ICON_PATH, 36, 36);
        downloadButton = GuiUtil.changeButtonIconColor(AppConstant.DOWNLOAD_ICON_PATH, 36, 36);
        moreOptionsButton = GuiUtil.changeButtonIconColor(AppConstant.MORE_ICON_PATH, 36, 36);

        shuffleButton.addActionListener(e -> handleShuffleButtonClick());
        addToPlaylistButton.addActionListener(e -> handleAddToPlaylistButtonClick());
        downloadButton.addActionListener(e -> handleDownloadButtonClick());
        moreOptionsButton.addActionListener(e -> handleMoreOptionsButtonClick());

        buttonsPanel.add(playButton);
        buttonsPanel.add(shuffleButton);
        buttonsPanel.add(addToPlaylistButton);
        buttonsPanel.add(downloadButton);
        buttonsPanel.add(moreOptionsButton);

        // Add components to info panel
        infoPanel.add(typeLabel);
        infoPanel.add(titleLabel);
        infoPanel.add(subtitleLabel);
        infoPanel.add(detailsLabel);
        infoPanel.add(buttonsPanel);

        // Add cover panel and info panel to header
        headerPanel.add(coverPanel, "cell 0 0");
        headerPanel.add(infoPanel, "cell 1 0, grow");
    }

    private void createTracksSection() {
        tracksPanel = GuiUtil.createPanel(new BorderLayout(0, 10));
        tracksPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Create column names and widths
        String[] columnNames = {"#", "TITLE", "ARTIST", "PLAYS", "DURATION"};
        int[] columnWidths = {50, 0, 0, 80, 80};

        // Create styled table
        tracksTable = GuiUtil.createStyledTable(columnNames, columnWidths);
        tableModel = (DefaultTableModel) tracksTable.getModel();

        tracksTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tracksTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    tracksTable.repaint();
                }
            }
        });

        tracksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                tracksTable.repaint();
            }
        });

        tracksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && tracks != null && !tracks.isEmpty()) {
                    int row = tracksTable.getSelectedRow();
                    if (row < 0 || row >= tracks.size()) return;

                    SongDTO selectedSong = tracks.get(row);
                    SongDTO currentSong = playerFacade.getCurrentSong();

                    if (isSameSong(selectedSong, currentSong)) {
                        if (playerFacade.isPaused()) {
                            playerFacade.playCurrentSong();
                        } else {
                            playerFacade.pauseSong();
                        }
                    }
                    // Otherwise load & play new song
                    else {
                        if (sourceType == PlaylistSourceType.ALBUM) {
                            playerFacade.loadSongWithContext(
                                    selectedSong,
                                    playerFacade.convertSongListToPlaylist(tracks, currentAlbum.getTitle()),
                                    PlaylistSourceType.ALBUM
                            );
                        } else {
                            playerFacade.loadSongWithContext(
                                    selectedSong,
                                    currentPlaylist,
                                    sourceType
                            );
                        }
                    }
                } else if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e) && tracks != null && !tracks.isEmpty()) {
                    int row = tracksTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < tracks.size()) {
                        if (!tracksTable.isRowSelected(row)) {
                            tracksTable.setRowSelectionInterval(row, row);
                        }

                        SongDTO song = tracks.get(row);

                        JPopupMenu contextMenu = createSongContextMenu(song);
                        contextMenu.show(tracksTable, e.getX(), e.getY());
                    }
                }
            }
        });

        tracksTable.getColumnModel().getColumn(0).setCellRenderer((table, value, isSel, hasFocus, row, col) -> {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(true);

            SongDTO song = tracks.get(row);
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
                            if (sourceType == PlaylistSourceType.ALBUM)
                                playerFacade.loadSongWithContext(s,
                                        playerFacade.convertSongListToPlaylist(tracks, currentAlbum.getTitle()),
                                        sourceType);
                            else
                                playerFacade.loadSongWithContext(s, currentPlaylist, sourceType);
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

        tracksPanel.add(tracksTable);
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
            if (sourceType == PlaylistSourceType.ALBUM) {
                playerFacade.loadSongWithContext(
                        song,
                        playerFacade.convertSongListToPlaylist(tracks, currentAlbum.getTitle()),
                        PlaylistSourceType.ALBUM
                );
            } else {
                playerFacade.loadSongWithContext(song, currentPlaylist, sourceType);
            }
        });
        contextMenu.add(playItem);

        JMenuItem addToQueueItem = GuiUtil.createMenuItem("Add to Queue");
        addToQueueItem.addActionListener(e -> {
            playerFacade.addToQueueNext(song);
            GuiUtil.showToast(this, "Added to queue");
        });
        contextMenu.add(addToQueueItem);

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
                } else {
                    GuiUtil.showToast(this, "Failed to unlike song");
                }
            } else {
                if (CommonApiUtil.createSongLikes(song.getId())) {
                    playerFacade.notifySongLiked();
                    GuiUtil.showToast(this, "Added to liked songs");
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

    private void createFooterSection() {
        footerPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1", "[grow]", "[]"));

        releaseInfoLabel = GuiUtil.createLabel("Released: January 1, 2023", Font.PLAIN, 14);
        releaseInfoLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        footerPanel.add(releaseInfoLabel);
    }

    private void createMoreByArtistSection() {
        moreByArtistPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1", "[grow]", "[]10[]"));

        moreByArtistLabel = GuiUtil.createLabel("More by Artist Name", Font.BOLD, 22);

        // Grid for album covers
        albumsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 15, gapy 15",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"));

        moreByArtistPanel.add(moreByArtistLabel);
        moreByArtistPanel.add(albumsGrid, "grow");
    }

    public void displayAlbum(AlbumDTO album) {
        this.currentAlbum = album;
        this.sourceType = PlaylistSourceType.ALBUM;
        this.currentPlaylist = null;
        this.tracks = album.getSongDTOS();

        // Update header
        titleLabel.setText(album.getTitle());
        subtitleLabel.setText(album.getArtistName());

        // Calculate total duration
        int totalSeconds = tracks.stream()
                .mapToInt(song -> {
                    String duration = song.getSongLength();
                    if (duration == null) return 0;
                    String[] parts = duration.split(":");
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
                    }
                    return 0;
                })
                .sum();

        int totalMinutes = totalSeconds / 60;
        int remainingSeconds = totalSeconds % 60;

        String durationText = String.format("%d min %d sec", totalMinutes, remainingSeconds);
        detailsLabel.setText(String.format("%d • %d songs • %s",
                album.getReleaseYear(),
                tracks.size(),
                durationText));

        // Load album cover
        playerFacade.populateAlbumImage(album, coverLabel::setLoadedImage);

        // Update release info
        releaseInfoLabel.setText("Released: " + album.getReleaseYear());

        // Update more by artist section
        moreByArtistLabel.setText("More by " + album.getArtistName());

        // Load similar albums
        loadMoreAlbumsByArtist(album.getArtistId());

        // Update tracks table
        updateTracklistTable();

        updateHeaderPlayButton();

        highlightPlayingSong(playerFacade.getCurrentSong());
    }

    private void updateHeaderPlayButton() {
        boolean isFromThisContext = tracks != null && tracks.stream()
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

    public void displayPlaylist(PlaylistDTO playlist, PlaylistSourceType sourceType) {
        this.currentPlaylist = playlist;
        this.sourceType = sourceType;
        this.currentAlbum = null;
        this.tracks = playlist.getSongs();

        // Update header
        String playlistType = getPlaylistTypeLabel(sourceType);
        typeLabel.setText(playlistType);
        titleLabel.setText(playlist.getName());
        subtitleLabel.setText(playlist.getCreatedBy() != null ?
                "Created by " + playlist.getCreatedBy() :
                "MuseMoe Playlist");

        // Calculate total duration
        int totalSeconds = tracks.stream()
                .mapToInt(song -> {
                    String duration = song.getSongLength();
                    if (duration == null) return 0;
                    String[] parts = duration.split(":");
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
                    }
                    return 0;
                })
                .sum();

        int totalMinutes = totalSeconds / 60;
        int remainingSeconds = totalSeconds % 60;

        String durationText = String.format("%d min %d sec", totalMinutes, remainingSeconds);
        detailsLabel.setText(String.format("%d songs • %s", tracks.size(), durationText));

        // Set default playlist cover if exists, otherwise use first song's cover
        if (!tracks.isEmpty()) {
            playerFacade.populateSongImage(tracks.getFirst(), coverLabel::setLoadedImage);
        } else {
            coverLabel.setLoadedImage(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH));
        }

        // Hide more by artist section for playlists
        moreByArtistPanel.setVisible(false);

        // Update tracks table
        updateTracklistTable();

        updateHeaderPlayButton();

        highlightPlayingSong(playerFacade.getCurrentSong());
    }

    private void updateTracklistTable() {
        log.debug("Updating tracklist table with {} tracks", tracks != null ? tracks.size() : 0);

        tableModel.setRowCount(0);

        if (tracks == null || tracks.isEmpty()) {
            log.debug("No tracks to display.");
            return;
        }
        // Clear table
        tableModel.setRowCount(0);

        if (tracks == null || tracks.isEmpty()) {
            return;
        }

        // Add rows
        int trackNumber = 1;
        for (SongDTO song : tracks) {
            String title = song.getTitle();
            String artist = song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist";
            String plays = song.getPlayCount() > 0 ? NumberFormatUtil.formatWithCommas(song.getPlayCount()) : "-";
            String duration = song.getSongLength() != null ? song.getSongLength() : "-:--";

            tableModel.addRow(new Object[]{
                    trackNumber++,
                    title,
                    artist,
                    plays,
                    duration
            });
        }
        tableModel.fireTableDataChanged();
        tracksTable.repaint();
    }

    private void loadMoreAlbumsByArtist(Long artistId) {
        // Clear existing albums
        albumsGrid.removeAll();

        if (artistId == null) {
            moreByArtistPanel.setVisible(false);
            return;
        }

        // Show loading indicator
        JLabel loadingLabel = GuiUtil.createLabel("Loading albums...", Font.ITALIC, 12);
        albumsGrid.add(loadingLabel, "span");
        albumsGrid.revalidate();
        albumsGrid.repaint();

        // Load albums in background
        SwingWorker<List<AlbumDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AlbumDTO> doInBackground() {
                return CommonApiUtil.fetchAlbumsByArtistId(artistId);
            }

            @Override
            protected void done() {
                try {
                    List<AlbumDTO> artistAlbums = get();
                    albumsGrid.removeAll();

                    if (artistAlbums == null || artistAlbums.isEmpty()) {
                        moreByArtistPanel.setVisible(false);
                    } else {
                        moreByArtistPanel.setVisible(true);

                        // Filter out current album if showing an album
                        List<AlbumDTO> filteredAlbums = artistAlbums.stream()
                                .filter(album -> currentAlbum == null || !Objects.equals(album.getId(), currentAlbum.getId()))
                                .limit(8)
                                .collect(Collectors.toList());

                        for (AlbumDTO album : filteredAlbums) {
                            albumsGrid.add(createAlbumCard(album));
                        }
                    }

                    albumsGrid.revalidate();
                    albumsGrid.repaint();
                } catch (Exception e) {
                    log.error("Error loading more albums by artist", e);
                    moreByArtistPanel.setVisible(false);
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
        JLabel titleLabel = GuiUtil.createLabel(
                StringUtils.getTruncatedTextByWidth(album.getTitle(), FontUtil.getSpotifyFont(Font.BOLD, 12), 120),
                Font.BOLD, 12);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setToolTipText(album.getTitle());

        // Release year
        JLabel yearLabel = GuiUtil.createLabel(String.valueOf(album.getReleaseYear()), Font.PLAIN, 11);
        yearLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        yearLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        // Add components
        card.add(albumCover);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(yearLabel);

        // Add hover effect and click handler
        GuiUtil.addHoverEffect(card);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    HomePage homePage = GuiUtil.findHomePageInstance(AlbumViewPanel.this);
                    if (homePage != null) {
                        homePage.navigateToAlbumView(album);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    GuiUtil.addAlbumContextMenu(card, album);
                }
            }
        });

        return card;
    }

    private void handlePlayButtonClick(ActionEvent e) {
        if (tracks == null || tracks.isEmpty()) {
            return;
        }

        SongDTO currentSong = playerFacade.getCurrentSong();
        if (currentSong != null) {
            boolean playingFromThisContext = false;
            if (tracks.stream().anyMatch(song -> isSameSong(song, currentSong))) {
                playingFromThisContext = true;
            }

            if (playingFromThisContext) {
                if (playerFacade.isPaused()) {
                    playerFacade.playCurrentSong();
                } else {
                    playerFacade.pauseSong();
                }
                return;
            }
        }

        SongDTO firstSong = tracks.getFirst();
        if (sourceType == PlaylistSourceType.ALBUM && currentAlbum != null) {
            playerFacade.loadSongWithContext(
                    firstSong,
                    playerFacade.convertSongListToPlaylist(tracks, currentAlbum.getTitle()),
                    PlaylistSourceType.ALBUM
            );
        } else if (currentPlaylist != null) {
            playerFacade.loadSongWithContext(firstSong, currentPlaylist, sourceType);
        }
    }

    private void handleShuffleButtonClick() {
        if (tracks == null || tracks.isEmpty()) {
            return;
        }

        List<SongDTO> shuffledTracks = new ArrayList<>(tracks);
        Collections.shuffle(shuffledTracks);

        SongDTO firstSong = shuffledTracks.getFirst();

        if (sourceType == PlaylistSourceType.ALBUM && currentAlbum != null) {
            PlaylistDTO shuffledPlaylist = playerFacade.convertSongListToPlaylist(
                    shuffledTracks,
                    "Shuffled: " + currentAlbum.getTitle()
            );
            playerFacade.loadSongWithContext(firstSong, shuffledPlaylist, sourceType);
        } else if (currentPlaylist != null) {
            PlaylistDTO shuffledPlaylist = new PlaylistDTO();
            shuffledPlaylist.setName("Shuffled: " + currentPlaylist.getName());
            shuffledPlaylist.setSongs(shuffledTracks);
            playerFacade.loadSongWithContext(firstSong, shuffledPlaylist, sourceType);
        }
    }

    private void handleAddToPlaylistButtonClick() {
        if (tracks == null || tracks.isEmpty()) {
            return;
        }

        // Show playlist selection dialog
        List<PlaylistDTO> userPlaylists = CommonApiUtil.fetchPlaylistByUserId();

        if (userPlaylists.isEmpty()) {
            GuiUtil.showInfoMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "You don't have any playlists yet. Create one first.");
            return;
        }

        PlaylistSelectionPanel playlistPanel = new PlaylistSelectionPanel(userPlaylists);
        JDialog dialog = GuiUtil.createStyledDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Select Playlist",
                playlistPanel,
                backgroundColor,
                textColor
        );

        playlistPanel.addPropertyChangeListener("playlistSelected", evt -> {
            PlaylistDTO selectedPlaylist = (PlaylistDTO) evt.getNewValue();
            dialog.dispose();

            selectedPlaylist.setSongIds(tracks.stream().map(SongDTO::getId).collect(Collectors.toList()));

            boolean success = CommonApiUtil.addSongsToPlaylist(selectedPlaylist);

            if (success) {
                GuiUtil.showSuccessMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        tracks.size() + " songs added to " + selectedPlaylist.getName()
                );
            } else {
                GuiUtil.showErrorMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "Failed to add songs to playlist"
                );
            }
        });

        playlistPanel.addPropertyChangeListener("cancel", evt -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void handleDownloadButtonClick() {
        if (tracks == null || tracks.isEmpty()) {
            return;
        }

        int result = GuiUtil.showConfirmMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Download " + tracks.size() + " songs?",
                "Confirm Download"
        );

        if (result == JOptionPane.YES_OPTION) {
            downloadSongs();
        }
    }

    private void downloadSongs() {
        JDialog progressDialog = GuiUtil.createProgressDialog(
                SwingUtilities.getWindowAncestor(this),
                "Downloading Songs",
                "Downloading " + tracks.size() + " songs...");

        JProgressBar progressBar = GuiUtil.findFirstComponentByType(
                progressDialog.getContentPane(),
                JProgressBar.class,
                bar -> true
        );

        if (progressBar != null) {
            progressBar.setStringPainted(true);
        }

        SwingWorker<Void, DownloadStatus> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int total = tracks.size();
                int completed = 0;

                for (int i = 0; i < tracks.size(); i++) {
                    try {
                        SongDTO song = tracks.get(i);
                        String songName = StringUtils.getTruncatedText(song.getTitle());

                        publish(new DownloadStatus(
                                (i * 100) / total,
                                "Downloading " + songName + " (" + (i + 1) + "/" + total + ")"
                        ));

                        File targetFile = new File(AppConstant.DEFAULT_DOWNLOAD_DIR,
                                SongDownloadUtil.sanitizeFileName(songName) + ".mp3");

                        try (InputStream in = App.getBean(GoogleDriveService.class).getFileContent(song.getDriveFileId());
                             FileOutputStream out = new FileOutputStream(targetFile)) {

                            byte[] buffer = new byte[8192];
                            int bytesRead;

                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }

                            // Record the download
                            CommonApiUtil.createUserDownload(song);
                            completed++;
                        }

                        // Update progress after completion
                        publish(new DownloadStatus(
                                ((i + 1) * 100) / total,
                                "Downloaded " + songName + " (" + (i + 1) + "/" + total + ")"
                        ));

                    } catch (Exception e) {
                        log.error("Error downloading song at index {}: {}", i, e.getMessage(), e);
                    }
                }

                return null;
            }

            @Override
            protected void process(List<DownloadStatus> statuses) {
                DownloadStatus latest = statuses.get(statuses.size() - 1);

                if (progressBar != null) {
                    progressBar.setValue(latest.progressPercentage);
                    progressBar.setString(latest.progressPercentage + "%");
                }

                JLabel statusLabel = GuiUtil.findFirstComponentByType(
                        progressDialog.getContentPane(),
                        JLabel.class,
                        label -> true
                );

                if (statusLabel != null) {
                    statusLabel.setText(latest.statusMessage);
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();

                // Refresh the local songs cache
                LocalSongManager.getDownloadedSongs();

                // Show completion message
                GuiUtil.showSuccessMessageDialog(
                        SwingUtilities.getWindowAncestor(AlbumViewPanel.this),
                        "Download complete!"
                );

                // Update the UI if we're in a HomePage
                Component parent = SwingUtilities.getWindowAncestor(AlbumViewPanel.this);
                if (parent instanceof HomePage) {
                    ((HomePage) parent).refreshDownloadedSongsPanel();
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private String createButtonKey(SongDTO song) {
        if (song.getId() != null) {
            return "song:" + song.getId();
        } else if (song.getIsLocalFile()) {
            return "local:" + song.getLocalFilePath();
        }
        return "unknown:" + System.identityHashCode(song);
    }

    // Helper class for download status updates
    private static class DownloadStatus {
        final int progressPercentage;
        final String statusMessage;

        DownloadStatus(int progressPercentage, String statusMessage) {
            this.progressPercentage = progressPercentage;
            this.statusMessage = statusMessage;
        }
    }


    private void handleMoreOptionsButtonClick() {
        if (currentAlbum == null) return;

        JPopupMenu contextMenu = GuiUtil.createPopupMenu(backgroundColor, textColor);

        JMenuItem viewDetailsItem = GuiUtil.createMenuItem("View Artist");
        viewDetailsItem.addActionListener(e -> {
            ArtistDTO artist = CommonApiUtil.findArtistById(currentAlbum.getArtistId());
            HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(AlbumViewPanel.this);
            if (homePage != null) {
                homePage.navigateToArtistView(artist);
            }
        });
        contextMenu.add(viewDetailsItem);
        JMenuItem addToPlaylistItem = GuiUtil.createMenuItem("Add to Playlist");
        addToPlaylistItem.addActionListener(e -> handleAddToPlaylistButtonClick());
        contextMenu.add(addToPlaylistItem);

        // Share option
        JMenuItem shareItem = GuiUtil.createMenuItem("Share");
        shareItem.addActionListener(e -> {
            String shareUrl = "https://musemoe.com/album/" + currentAlbum.getId();
            StringSelection selection = new StringSelection(shareUrl);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            GuiUtil.showToast(SwingUtilities.getWindowAncestor(this), "Link copied to clipboard!");
        });
        contextMenu.add(shareItem);

        GuiUtil.registerPopupMenuForThemeUpdates(contextMenu);

        contextMenu.show(moreOptionsButton, 0, moreOptionsButton.getHeight());
    }


    private String getPlaylistTypeLabel(PlaylistSourceType sourceType) {
        return switch (sourceType) {
            case USER_PLAYLIST -> "PLAYLIST";
            case ALBUM -> "ALBUM";
            case LIKED_SONGS -> "LIKED SONGS";
            case QUEUE -> "QUEUE";
            case SEARCH_RESULTS -> "SEARCH RESULTS";
            default -> "PLAYLIST";
        };
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;
        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);
        repaint();
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        switch (event.type()) {
            case PLAYBACK_STARTED -> {
                SongDTO currentSong = (SongDTO) event.data();
                highlightPlayingSong(currentSong);
                updatePlayPauseButtons(currentSong, true);

                // Also update the header play button
                boolean isFromThisContext = tracks != null && tracks.stream()
                        .anyMatch(song -> isSameSong(song, currentSong));
                if (isFromThisContext) {
                    playButton.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.PAUSE_ICON_PATH, textColor, 50, 50
                    ));
                    playButton.setRolloverIcon(GuiUtil.createColoredIcon(
                            AppConstant.PAUSE_ICON_PATH, GuiUtil.lightenColor(textColor, 0.3f), 50, 50
                    ));
                }
            }
            case PLAYBACK_PAUSED -> {
                SongDTO currentSong = (SongDTO) event.data();
                updatePlayPauseButtons(currentSong, false);

                // Also update the header play button
                boolean isFromThisContext = tracks != null && tracks.stream()
                        .anyMatch(song -> isSameSong(song, currentSong));
                if (isFromThisContext) {
                    playButton.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH, textColor, 50, 50
                    ));
                    playButton.setRolloverIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH, GuiUtil.lightenColor(textColor, 0.3), 50, 50
                    ));
                }
            }
        }
    }

    private void updatePlayPauseButtons(SongDTO currentSong, boolean isPlaying) {
        if (currentSong == null) return;

        for (Map.Entry<SongDTO, JButton> entry : playPauseButtonMap.entrySet()) {
            SongDTO song = entry.getKey();
            JButton button = entry.getValue();

            if (isSameSong(song, currentSong)) {
                String iconPath = isPlaying ? AppConstant.PAUSE_ICON_PATH : AppConstant.PLAY_ICON_PATH;
                button.setIcon(GuiUtil.createColoredIcon(iconPath, textColor, 16, 16));
                button.setRolloverIcon(GuiUtil.createColoredIcon(iconPath, GuiUtil.lightenColor(textColor, 0.3f), 16, 16));
            } else {
                button.setIcon(GuiUtil.createColoredIcon(AppConstant.PLAY_ICON_PATH, textColor, 16, 16));
                button.setRolloverIcon(GuiUtil.createColoredIcon(AppConstant.PLAY_ICON_PATH, GuiUtil.lightenColor(textColor, 0.3f), 16, 16));
            }
        }

        tracksTable.repaint();
    }


    private void highlightPlayingSong(SongDTO song) {
        if (tracks == null || song == null) {
            return;
        }

        for (int i = 0; i < tracks.size(); i++) {
            SongDTO track = tracks.get(i);
            if (isSameSong(track, song)) {
                tracksTable.getSelectionModel().setSelectionInterval(i, i);
                tracksTable.scrollRectToVisible(tracksTable.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private boolean isSameSong(SongDTO song1, SongDTO song2) {
        if (song1 == null || song2 == null) return false;

        if (song1.getId() != null && song2.getId() != null) {
            return song1.getId().equals(song2.getId());
        } else if (song1.getIsLocalFile() && song2.getIsLocalFile()) {
            return song1.getLocalFilePath().equals(song2.getLocalFilePath());
        }

        return false;
    }

    public void cleanup() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
        playerFacade.unsubscribeFromPlayerEvents(this);
    }
}