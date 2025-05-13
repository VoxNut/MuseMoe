package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SearchResultsPanel extends JPanel implements ThemeChangeListener {
    private final MusicPlayerFacade playerFacade;
    private JPanel contentPanel;
    private JPanel navigationPanel;
    private CardLayout cardLayout;

    private JButton allButton;
    private JButton songsButton;
    private JButton playlistsButton;
    private JButton albumsButton;
    private JButton artistsButton;

    private JPanel allPanel;
    private JPanel songsPanel;
    private JPanel playlistsPanel;
    private JPanel albumsPanel;
    private JPanel artistsPanel;
    private JScrollPane scrollPane;

    private String currentQuery = "";
    private Map<SongDTO, JButton> playPauseButtonMap = new HashMap<>();
    private String currentTab = "ALL";

    public SearchResultsPanel() {
        this.playerFacade = App.getBean(MusicPlayerFacade.class);
        setOpaque(false);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = GuiUtil.createPanel(cardLayout);

        createNavigationPanel();

        createContentPanels();

        JPanel mainPanel = GuiUtil.createPanel(new BorderLayout());
        mainPanel.add(navigationPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        scrollPane = GuiUtil.createStyledScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        ThemeManager.getInstance().addThemeChangeListener(this);
        onThemeChanged(
                ThemeManager.getInstance().getBackgroundColor(),
                ThemeManager.getInstance().getTextColor(),
                ThemeManager.getInstance().getAccentColor()
        );
    }

    private void createNavigationPanel() {
        navigationPanel = GuiUtil.createPanel(new MigLayout("insets 5 10 5 10, fillx", "", "[]"));
        navigationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f)));
        JPanel buttonPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        buttonPanel.setOpaque(false);

        allButton = createTabButton("ALL");
        songsButton = createTabButton("SONGS");
        playlistsButton = createTabButton("PLAYLISTS");
        albumsButton = createTabButton("ALBUMS");
        artistsButton = createTabButton("ARTISTS");

        buttonPanel.add(allButton);
        buttonPanel.add(songsButton);
        buttonPanel.add(playlistsButton);
        buttonPanel.add(albumsButton);
        buttonPanel.add(artistsButton);

        navigationPanel.add(buttonPanel, "left");

        updateSelectedTab(currentTab);

    }

    private void updateSelectedTab(String selectedTab) {
        Color backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        Color accentColor = ThemeManager.getInstance().getAccentColor();

        allButton.setBackground(backgroundColor);
        allButton.setForeground(ThemeManager.getInstance().getTextColor());

        songsButton.setBackground(backgroundColor);
        songsButton.setForeground(ThemeManager.getInstance().getTextColor());

        playlistsButton.setBackground(backgroundColor);
        playlistsButton.setForeground(ThemeManager.getInstance().getTextColor());

        albumsButton.setBackground(backgroundColor);
        albumsButton.setForeground(ThemeManager.getInstance().getTextColor());

        artistsButton.setBackground(backgroundColor);
        artistsButton.setForeground(ThemeManager.getInstance().getTextColor());

        JButton selectedButton;
        switch (selectedTab) {
            case "SONGS" -> selectedButton = songsButton;
            case "PLAYLISTS" -> selectedButton = playlistsButton;
            case "ALBUMS" -> selectedButton = albumsButton;
            case "ARTISTS" -> selectedButton = artistsButton;
            default -> selectedButton = allButton;
        }

        selectedButton.setBackground(accentColor);
        selectedButton.setForeground(backgroundColor);

    }

    private JButton createTabButton(String text) {
        JButton button = GuiUtil.createButton(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, text);
            currentTab = text;
            updateSelectedTab(text);
        });
        return button;
    }


    public void updateSearchResults(String query, List<SongDTO> songs,
                                    List<PlaylistDTO> playlists,
                                    List<AlbumDTO> albums,
                                    List<ArtistDTO> artists) {
        this.currentQuery = query;

        // Clear existing content
        allPanel.removeAll();
        songsPanel.removeAll();
        playlistsPanel.removeAll();
        albumsPanel.removeAll();
        artistsPanel.removeAll();
        playPauseButtonMap.clear();

        // Update ALL tab
        updateAllPanel(songs, playlists, albums, artists);

        // Update individual tabs
        updateSongsPanel(songs);
        updatePlaylistsPanel(playlists);
        updateAlbumsPanel(albums);
        updateArtistsPanel(artists);

        // Show the current tab
        cardLayout.show(contentPanel, currentTab);

        // Refresh UI
        revalidate();
        repaint();
    }

    private void updateAllPanel(List<SongDTO> songs, List<PlaylistDTO> playlists,
                                List<AlbumDTO> albums, List<ArtistDTO> artists) {
        // Top results section
        if (!songs.isEmpty() || !artists.isEmpty() || !albums.isEmpty()) {
            JPanel topResultPanel = createTopResultPanel(songs, artists, albums);
            JPanel songsListPanel = createSongsListPanel(songs);

            // Add panels with split layout
            JPanel topSection = GuiUtil.createPanel(new MigLayout("fill, insets 0", "[30%][grow]", "[]"));
            topSection.add(topResultPanel, "width 30%");
            topSection.add(songsListPanel, "width 70%");

            allPanel.add(topSection, "growx, wrap");
        }

        // Artists section (if available)
        if (!artists.isEmpty()) {
            JPanel artistsSection = createArtistsSection(artists);
            allPanel.add(artistsSection, "growx, wrap");
        }

        // Albums section (if available)
        if (!albums.isEmpty()) {
            JPanel albumsSection = createAlbumsSection(albums);
            allPanel.add(albumsSection, "growx, wrap");
        }

        // Playlists section (if available)
        if (!playlists.isEmpty()) {
            JPanel playlistsSection = createPlaylistsSection(playlists);
            allPanel.add(playlistsSection, "growx, wrap");
        }
    }

    private JPanel createTopResultPanel(List<SongDTO> songs, List<ArtistDTO> artists, List<AlbumDTO> albums) {
        JPanel panel = GuiUtil.createPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 20));

        JLabel headerLabel = GuiUtil.createLabel("Top result", Font.BOLD, 18);
        panel.add(headerLabel, BorderLayout.NORTH);

        // Determine the top result (prioritize songs, then artists, then albums)
        JPanel resultCard = null;

        if (!songs.isEmpty()) {
            SongDTO topSong = songs.getFirst();
            resultCard = createTopSongCard(topSong);
        } else if (!artists.isEmpty()) {
            ArtistDTO topArtist = artists.getFirst();
            resultCard = createTopArtistCard(topArtist);
        } else if (!albums.isEmpty()) {
            AlbumDTO topAlbum = albums.getFirst();
            resultCard = createTopAlbumCard(topAlbum);
        }

        if (resultCard != null) {
            panel.add(resultCard, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createTopSongCard(SongDTO song) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create song cover image
        AsyncImageLabel coverLabel = GuiUtil.createAsyncImageLabel(150, 150, 15);
        coverLabel.startLoading();
        playerFacade.populateSongImage(song, coverLabel::setLoadedImage);
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create song details
        JLabel titleLabel = GuiUtil.createLabel(song.getTitle(), Font.BOLD, 14);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(
                song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist",
                Font.PLAIN, 12
        );
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Play button
        JButton playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 40, 40);
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> playerFacade.loadSong(song));

        // Add components to card
        card.add(Box.createVerticalStrut(10));
        card.add(coverLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(artistLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(playButton);
        card.add(Box.createVerticalStrut(10));

        // Add hover effect
        GuiUtil.addHoverEffect(card);
        GuiUtil.addSongContextMenu(card, song);

        return card;
    }

    private JPanel createTopArtistCard(ArtistDTO artist) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create artist profile image
        AsyncImageLabel profileLabel = GuiUtil.createArtistProfileLabel(150);
        profileLabel.startLoading();
        playerFacade.populateArtistProfile(artist, profileLabel::setLoadedImage);
        profileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create artist details
        JLabel nameLabel = GuiUtil.createLabel(artist.getStageName(), Font.BOLD, 14);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel("Artist", Font.PLAIN, 12);
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to card
        card.add(Box.createVerticalStrut(10));
        card.add(profileLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(artistLabel);
        card.add(Box.createVerticalStrut(10));

        // Add hover effect
        GuiUtil.addHoverEffect(card);

        return card;
    }

    private void createContentPanels() {
        // Create tab panels
        allPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow, fill]", "[]10[]10[]10[]10[]"));
        songsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow, fill]", "[]10[]"));
        playlistsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow, fill]", "[]10[]"));
        albumsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow, fill]", "[]10[]"));
        artistsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow, fill]", "[]10[]"));

        // Add panels to card layout
        contentPanel.add(allPanel, "ALL");
        contentPanel.add(songsPanel, "SONGS");
        contentPanel.add(playlistsPanel, "PLAYLISTS");
        contentPanel.add(albumsPanel, "ALBUMS");
        contentPanel.add(artistsPanel, "ARTISTS");
    }

    private JPanel createTopAlbumCard(AlbumDTO album) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(ThemeManager.getInstance().getAccentColor().darker(), 1, true)
        ));

        // Create album cover image
        AsyncImageLabel coverLabel = GuiUtil.createAsyncImageLabel(150, 150, 15);
        coverLabel.startLoading();
        playerFacade.populateAlbumImage(album, coverLabel::setLoadedImage);
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create album details
        JLabel titleLabel = GuiUtil.createLabel(album.getTitle(), Font.BOLD, 14);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(album.getArtistName(), Font.PLAIN, 12);
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to card
        card.add(Box.createVerticalStrut(10));
        card.add(coverLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(artistLabel);
        card.add(Box.createVerticalStrut(10));

        // Add hover effect
        GuiUtil.addHoverEffect(card);

        return card;
    }

    private JPanel createSongsListPanel(List<SongDTO> songs) {
        JPanel panel = GuiUtil.createPanel(new BorderLayout(0, 10));

        JLabel headerLabel = GuiUtil.createLabel("Songs", Font.BOLD, 18);
        panel.add(headerLabel, BorderLayout.NORTH);

        // Create songs list
        JPanel songsListPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow]", "[]0[]"));

        // Show up to 4 songs in ALL tab
        int limit = Math.min(4, songs.size());
        for (int i = 0; i < limit; i++) {
            SongDTO song = songs.get(i);
            JPanel songItem = createSongListItem(song);
            songsListPanel.add(songItem, "growx");
        }

        panel.add(songsListPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSongListItem(SongDTO song) {
        JPanel songPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        songPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        songPanel.setPreferredSize(new Dimension(200, 50));
        // Play button
        JButton playButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 24, 24);
        playPauseButtonMap.put(song, playButton);

        playButton.addActionListener(e -> {
            SongDTO currentSong = playerFacade.getCurrentSong();
            boolean isSameSong = false;

            if (currentSong != null && song != null) {
                if (currentSong.getIsLocalFile() && song.getIsLocalFile()) {
                    isSameSong = currentSong.getLocalFilePath() != null &&
                            currentSong.getLocalFilePath().equals(song.getLocalFilePath());
                } else if (!currentSong.getIsLocalFile() && !song.getIsLocalFile()) {
                    isSameSong = currentSong.getId() != null &&
                            song.getId() != null &&
                            currentSong.getId().equals(song.getId());
                }
            }

            if (isSameSong) {
                if (playerFacade.isPaused()) {
                    playerFacade.playCurrentSong();
                } else {
                    playerFacade.pauseSong();
                }
            } else {
                if (song.getIsLocalFile()) {
                    try {
                        playerFacade.loadLocalSong(song);
                    } catch (IOException ex) {
                        log.error("Error loading local song: {}", ex.getMessage());
                    }
                } else {
                    playerFacade.loadSong(song);
                }
            }
        });

        // Song title and artist
        JPanel infoPanel = GuiUtil.createPanel(new BorderLayout());
        String title = song.getTitle();
        String artist = song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist";

        JLabel songLabel = GuiUtil.createLabel(title, Font.BOLD, 14);
        JLabel artistLabel = GuiUtil.createLabel(artist, Font.PLAIN, 12);

        JPanel labelPanel = GuiUtil.createPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(songLabel);
        labelPanel.add(artistLabel);

        infoPanel.add(labelPanel, BorderLayout.CENTER);

        // Duration
        JLabel durationLabel = GuiUtil.createLabel(song.getSongLength(), Font.PLAIN, 12);

        // Add components to panel
        songPanel.add(playButton, BorderLayout.WEST);
        songPanel.add(infoPanel, BorderLayout.CENTER);
        songPanel.add(durationLabel, BorderLayout.EAST);

        // Add hover effect
        GuiUtil.addHoverEffect(songPanel);
        GuiUtil.addSongContextMenu(songPanel, song);

        return songPanel;
    }

    private JPanel createArtistsSection(List<ArtistDTO> artists) {
        JPanel section = GuiUtil.createPanel(new BorderLayout());

        // Header
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Artists", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);


        section.add(headerPanel, BorderLayout.NORTH);

        // Artists grid
        JPanel artistsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        // Add artists
        int limit = Math.min(8, artists.size());
        for (int i = 0; i < limit; i++) {
            ArtistDTO artist = artists.get(i);
            artistsGrid.add(createArtistCard(artist));
        }

        section.add(artistsGrid, BorderLayout.CENTER);
        return section;
    }

    private JPanel createArtistCard(ArtistDTO artist) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create circular artist profile image
        AsyncImageLabel profileLabel = GuiUtil.createArtistProfileLabel(100);
        profileLabel.startLoading();
        playerFacade.populateArtistProfile(artist, profileLabel::setLoadedImage);
        profileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Artist name and type label
        JLabel nameLabel = GuiUtil.createLabel(artist.getStageName(), Font.BOLD, 12);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel typeLabel = GuiUtil.createLabel("Artist", Font.PLAIN, 11);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        card.add(profileLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(nameLabel);
        card.add(typeLabel);

        // Add hover effect
        GuiUtil.addHoverEffect(card);

        return card;
    }

    private JPanel createAlbumsSection(List<AlbumDTO> albums) {
        JPanel section = GuiUtil.createPanel(new BorderLayout());

        // Header
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Albums", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);


        section.add(headerPanel, BorderLayout.NORTH);

        // Albums grid
        JPanel albumsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 5, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        // Add albums
        int limit = Math.min(5, albums.size());
        for (int i = 0; i < limit; i++) {
            AlbumDTO album = albums.get(i);
            albumsGrid.add(createAlbumCard(album));
        }

        section.add(albumsGrid, BorderLayout.CENTER);
        return section;
    }

    private JPanel createAlbumCard(AlbumDTO album) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create square album cover with rounded corners
        AsyncImageLabel coverLabel = GuiUtil.createAsyncImageLabel(120, 120, 15);
        coverLabel.startLoading();
        playerFacade.populateAlbumImage(album, coverLabel::setLoadedImage);


        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Album title and artist
        JLabel titleLabel = GuiUtil.createLabel(album.getTitle(), Font.BOLD, 12);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(album.getArtistName(), Font.PLAIN, 11);
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        card.add(coverLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        card.add(artistLabel);

        // Add hover effect
        GuiUtil.addHoverEffect(card);

        return card;
    }

    private JPanel createPlaylistsSection(List<PlaylistDTO> playlists) {
        JPanel section = GuiUtil.createPanel(new BorderLayout());

        // Header
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel headerLabel = GuiUtil.createLabel("Playlists", Font.BOLD, 18);
        headerPanel.add(headerLabel, BorderLayout.WEST);


        section.add(headerPanel, BorderLayout.NORTH);

        // Playlists grid
        JPanel playlistsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 5, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        // Add playlists
        int limit = Math.min(5, playlists.size());
        for (int i = 0; i < limit; i++) {
            PlaylistDTO playlist = playlists.get(i);
            playlistsGrid.add(createPlaylistCard(playlist));
        }

        section.add(playlistsGrid, BorderLayout.CENTER);
        return section;
    }

    private JPanel createPlaylistCard(PlaylistDTO playlist) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create playlist cover
        AsyncImageLabel coverLabel = GuiUtil.createAsyncImageLabel(120, 120, 15);
        coverLabel.startLoading();
        playerFacade.populateSongImage(playlist.getFirstSong(), coverLabel::setLoadedImage);
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Playlist name and song count
        JLabel nameLabel = GuiUtil.createLabel(playlist.getName(), Font.BOLD, 12);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String countText = playlist.getSongs().size() + " song" + (playlist.getSongs().size() != 1 ? "s" : "");
        JLabel countLabel = GuiUtil.createLabel(countText, Font.PLAIN, 11);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        card.add(coverLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(nameLabel);
        card.add(countLabel);

        // Add hover effect
        GuiUtil.addHoverEffect(card);

        // Add click handler
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                playerFacade.setCurrentPlaylist(playlist);
                if (!playlist.getSongs().isEmpty()) {
                    playerFacade.loadSong(playlist.getSongs().getFirst());
                }
            }
        });

        return card;
    }

    // Update individual tab panels
    private void updateSongsPanel(List<SongDTO> songs) {
        // Create header
        JLabel headerLabel = GuiUtil.createLabel("Songs", Font.BOLD, 18);
        songsPanel.add(headerLabel, "wrap");

        // Create songs list
        JPanel songsListPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow]", "[]0[]"));

        if (songs.isEmpty()) {
            JLabel noResultsLabel = GuiUtil.createLabel("No songs found for \"" + currentQuery + "\"", Font.ITALIC, 14);
            songsPanel.add(noResultsLabel, "wrap");
        } else {
            for (SongDTO song : songs) {
                JPanel songItem = createSongListItem(song);
                songsListPanel.add(songItem, "growx");
            }
            songsPanel.add(songsListPanel, "grow");
        }
    }

    private void updateArtistsPanel(List<ArtistDTO> artists) {
        // Create header
        JLabel headerLabel = GuiUtil.createLabel("Artists", Font.BOLD, 18);
        artistsPanel.add(headerLabel, "wrap");

        if (artists.isEmpty()) {
            JLabel noResultsLabel = GuiUtil.createLabel("No artists found for \"" + currentQuery + "\"", Font.ITALIC, 14);
            artistsPanel.add(noResultsLabel, "wrap");
            return;
        }

        // Create artists grid
        JPanel grid = GuiUtil.createPanel(new MigLayout(
                "wrap 4, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        for (ArtistDTO artist : artists) {
            grid.add(createArtistCard(artist));
        }

        artistsPanel.add(grid, "grow");
    }

    private void updateAlbumsPanel(List<AlbumDTO> albums) {
        // Create header
        JLabel headerLabel = GuiUtil.createLabel("Albums", Font.BOLD, 18);
        albumsPanel.add(headerLabel, "wrap");

        if (albums.isEmpty()) {
            JLabel noResultsLabel = GuiUtil.createLabel("No albums found for \"" + currentQuery + "\"", Font.ITALIC, 14);
            albumsPanel.add(noResultsLabel, "wrap");
            return;
        }

        // Create albums grid
        JPanel grid = GuiUtil.createPanel(new MigLayout(
                "wrap 4, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        for (AlbumDTO album : albums) {
            grid.add(createAlbumCard(album));
        }

        albumsPanel.add(grid, "grow");
    }

    private void updatePlaylistsPanel(List<PlaylistDTO> playlists) {
        // Create header
        JLabel headerLabel = GuiUtil.createLabel("Playlists", Font.BOLD, 18);
        playlistsPanel.add(headerLabel, "wrap");

        if (playlists.isEmpty()) {
            JLabel noResultsLabel = GuiUtil.createLabel("No playlists found for \"" + currentQuery + "\"", Font.ITALIC, 14);
            playlistsPanel.add(noResultsLabel, "wrap");
            return;
        }

        // Create playlists grid
        JPanel grid = GuiUtil.createPanel(new MigLayout(
                "wrap 4, fillx, gapx 20, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        for (PlaylistDTO playlist : playlists) {
            grid.add(createPlaylistCard(playlist));
        }

        playlistsPanel.add(grid, "grow");
    }

    public void updatePlayPauseButtons(SongDTO currentSong, boolean isPlaying) {
        SwingUtilities.invokeLater(() -> {
            playPauseButtonMap.forEach((song, button) -> {
                boolean isSameSong = false;

                if (currentSong != null && song != null) {
                    if (currentSong.getIsLocalFile() && song.getIsLocalFile()) {
                        isSameSong = currentSong.getLocalFilePath() != null &&
                                currentSong.getLocalFilePath().equals(song.getLocalFilePath());
                    } else if (!currentSong.getIsLocalFile() && !song.getIsLocalFile()) {
                        isSameSong = currentSong.getId() != null &&
                                song.getId() != null &&
                                currentSong.getId().equals(song.getId());
                    }
                }

                if (isSameSong) {
                    if (isPlaying) {
                        button.setIcon(GuiUtil.createColoredIcon(
                                AppConstant.PAUSE_ICON_PATH,
                                ThemeManager.getInstance().getTextColor(),
                                24, 24
                        ));
                    } else {
                        button.setIcon(GuiUtil.createColoredIcon(
                                AppConstant.PLAY_ICON_PATH,
                                ThemeManager.getInstance().getTextColor(),
                                24, 24
                        ));
                    }
                } else {
                    button.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH,
                            ThemeManager.getInstance().getTextColor(),
                            24, 24
                    ));
                }
            });
        });
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {

        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);

        revalidate();
        repaint();
    }
}