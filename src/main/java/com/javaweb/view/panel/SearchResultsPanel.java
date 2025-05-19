package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.enums.PlaylistSourceType;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.StringUtils;
import com.javaweb.view.HomePage;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SearchResultsPanel extends JPanel implements ThemeChangeListener, PlayerEventListener {
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
    private String currentTab = "ALL";
    private final Map<String, JButton> playPauseButtonMap = new HashMap<>();

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        switch (event.type()) {
            case PLAYBACK_STARTED -> {
                SongDTO currentSong = (SongDTO) event.data();
                updatePlayPauseButtons(currentSong, true);
            }
            case PLAYBACK_PAUSED -> {
                SongDTO currentSong = (SongDTO) event.data();
                updatePlayPauseButtons(currentSong, false);
            }
        }
    }

    private String createButtonKey(SongDTO song, String suffix, String location) {
        String base = "remote:" + song.getId();
        return base + ":" + suffix + ":" + location;
    }

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
        add(scrollPane, BorderLayout.CENTER);

        ThemeManager.getInstance().addThemeChangeListener(this);
        onThemeChanged(
                ThemeManager.getInstance().getBackgroundColor(),
                ThemeManager.getInstance().getTextColor(),
                ThemeManager.getInstance().getAccentColor()
        );
        playerFacade.subscribeToPlayerEvents(this);

        updateSelectedTab(currentTab);

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
    }

    private void updateSelectedTab(String selectedTab) {
        Color backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        Color accentColor = ThemeManager.getInstance().getAccentColor();
        Color textColor = ThemeManager.getInstance().getTextColor();

        GuiUtil.styleButton(allButton, backgroundColor, textColor, accentColor);
        GuiUtil.styleButton(songsButton, backgroundColor, textColor, accentColor);
        GuiUtil.styleButton(playlistsButton, backgroundColor, textColor, accentColor);
        GuiUtil.styleButton(albumsButton, backgroundColor, textColor, accentColor);
        GuiUtil.styleButton(artistsButton, backgroundColor, textColor, accentColor);

        JButton selectedButton;
        switch (selectedTab) {
            case "SONGS" -> selectedButton = songsButton;
            case "PLAYLISTS" -> selectedButton = playlistsButton;
            case "ALBUMS" -> selectedButton = albumsButton;
            case "ARTISTS" -> selectedButton = artistsButton;
            default -> selectedButton = allButton;
        }

        GuiUtil.styleButton(selectedButton, accentColor, backgroundColor, textColor);

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

        // Show the all tab
        cardLayout.show(contentPanel, "ALL");
        updateSelectedTab("ALL");

        if (playerFacade.getCurrentSong() != null) {
            updatePlayPauseButtons(playerFacade.getCurrentSong(), !playerFacade.isPaused());
        }

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

            JPanel topSection = GuiUtil.createPanel(new MigLayout(
                    "fill, insets 0, gap 0!",
                    "[30%!][70%!]",
                    "[]"));

            topSection.add(topResultPanel, "width 30%!, spany, growy");
            topSection.add(songsListPanel, "width 70%!, spany, growy");

            allPanel.add(topSection, "growx, wrap");

        }

        // Artists section (if available)
        if (!artists.isEmpty()) {
            JPanel artistsSection = createArtistsSection(artists);
            allPanel.add(artistsSection, "grow, wrap");
        }

        // Albums section (if available)
        if (!albums.isEmpty()) {
            JPanel albumsSection = createAlbumsSection(albums);
            allPanel.add(albumsSection, "grow, wrap");
        }

        // Playlists section (if available)
        if (!playlists.isEmpty()) {
            JPanel playlistsSection = createPlaylistsSection(playlists);
            allPanel.add(playlistsSection, "grow, wrap");
        }
    }

    private JPanel createTopResultPanel(List<SongDTO> songs, List<ArtistDTO> artists, List<AlbumDTO> albums) {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, insets 0 0 10 20",
                "[]",
                "[]10[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Top result", Font.BOLD, 18);
        panel.add(headerLabel, "wrap");

        // Content card
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
            panel.add(resultCard, "grow");
        }

        return panel;
    }

    private JPanel createTopSongCard(SongDTO song) {
        JPanel card = GuiUtil.createPanel(new BorderLayout(20, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        AsyncImageLabel coverLabel = new AsyncImageLabel(250, 250, 15);
        coverLabel.startLoading();
        playerFacade.populateSongImage(song, coverLabel::setLoadedImage);

        JPanel coverPanel = GuiUtil.createPanel(new BorderLayout());
        coverPanel.setPreferredSize(new Dimension(250, 250));
        coverPanel.add(coverLabel, BorderLayout.CENTER);

        JPanel infoPanel = GuiUtil.createPanel(new BorderLayout(0, 10));

        JPanel textPanel = GuiUtil.createPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        Font titleFont = FontUtil.getSpotifyFont(Font.BOLD, 22);
        Font artistFont = FontUtil.getSpotifyFont(Font.PLAIN, 18);

        JLabel titleLabel = GuiUtil.createLabel(
                StringUtils.getTruncatedTextByWidth(song.getTitle(), titleFont, 150),
                Font.BOLD, 22);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = GuiUtil.createLabel(
                song.getSongArtist() != null ?
                        StringUtils.getTruncatedTextByWidth(song.getSongArtist(), artistFont) :
                        "Unknown Artist",
                Font.PLAIN, 18);
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(artistLabel);

        JPanel controlButtonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        // Create play button
        JButton playPauseButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 24, 24);


        playPauseButton.addActionListener(e -> {
            SongDTO currentSong = playerFacade.getCurrentSong();

            if (isSameSong(currentSong, song)) {
                if (playerFacade.isPaused()) {
                    playerFacade.playCurrentSong();
                } else {
                    playerFacade.pauseSong();
                }
            } else {
                if (song.getIsLocalFile()) {
                    playerFacade.loadLocalSong(song);
                } else {
                    AlbumDTO albumDTO = CommonApiUtil.fetchAlbumContainsThisSong(song.getId());
                    playerFacade.loadSongWithContext(song, playerFacade.convertSongListToPlaylist(albumDTO.getSongDTOS(), albumDTO.getTitle()), PlaylistSourceType.ALBUM);
                }
            }
        });


        String key = createButtonKey(song, "topSong", "topResult");
        playPauseButtonMap.put(key, playPauseButton);

        controlButtonsPanel.add(playPauseButton);


        infoPanel.add(textPanel, BorderLayout.NORTH);
        infoPanel.add(controlButtonsPanel, BorderLayout.WEST);

        // Add main components to card
        card.add(coverPanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        GuiUtil.addHoverEffect(card);
        GuiUtil.addSongContextMenu(card, song);

        coverLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Component clickedComponent = SwingUtilities.getDeepestComponentAt(card, e.getX(), e.getY());
                    // Only navigate if not clicking on play button
                    if (!(clickedComponent instanceof JButton)) {
                        log.info("Song clicked: {}", song.getTitle());
                        HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SearchResultsPanel.this);
                        homePage.navigateToSongDetailsView(song);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    GuiUtil.addSongContextMenu(coverLabel, song);
                }
            }
        });

        return card;
    }

    private JPanel createTopArtistCard(ArtistDTO artist) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create artist profile image
        AsyncImageLabel profileLabel = new AsyncImageLabel(150, 150, 15, true);
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


        artistLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Component clickedComponent = SwingUtilities.getDeepestComponentAt(card, e.getX(), e.getY());
                    // Only navigate if not clicking on play button
                    if (!(clickedComponent instanceof JButton)) {
                        log.info("Artist clicked: {}", artist.getStageName());
                        HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SearchResultsPanel.this);
                        homePage.navigateToArtistView(artist);
                    }
                }
            }
        });

        return card;
    }

    private void createContentPanels() {
        // Create tab panels
        allPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[fill]", "[]10[]"));
        songsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[fill]", "[]10[]"));
        playlistsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[fill]", "[]10[]"));
        albumsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[fill]", "[]10[]"));
        artistsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[fill]", "[]10[]"));

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
        AsyncImageLabel coverLabel = new AsyncImageLabel(150, 150, 15);
        coverLabel.startLoading();
        playerFacade.populateAlbumImage(album, coverLabel::setLoadedImage);
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font titleFont = FontUtil.getSpotifyFont(Font.BOLD, 14);


        // Create album details
        JLabel titleLabel = GuiUtil.createLabel(StringUtils.getTruncatedTextByWidth(album.getTitle(), titleFont), Font.BOLD, 14);
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


        coverLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Component clickedComponent = SwingUtilities.getDeepestComponentAt(card, e.getX(), e.getY());
                    // Only navigate if not clicking on play button
                    if (!(clickedComponent instanceof JButton)) {
                        log.info("Album clicked: {}", album.getTitle());
                        HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SearchResultsPanel.this);
                        homePage.navigateToAlbumView(album);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    GuiUtil.addAlbumContextMenu(coverLabel, album);
                }
            }
        });

        return card;
    }

    private JPanel createSongsListPanel(List<SongDTO> songs) {
        JPanel panel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JLabel headerLabel = GuiUtil.createLabel("Songs", Font.BOLD, 18);
        panel.add(headerLabel, BorderLayout.NORTH);

        // Create songs list
        JPanel songsListPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow]", "[]0[]"));

        // Show up to 4 songs in ALL tab
        int limit = Math.min(6, songs.size());
        for (int i = 0; i < limit; i++) {
            SongDTO song = songs.get(i);
            JPanel songItem = createSongListItem(song, "allTab");
            songsListPanel.add(songItem, "grow");
        }

        panel.add(songsListPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSongListItem(SongDTO song, String location) {
        JPanel songPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
        songPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel controlButtonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        JButton playPauseButton = GuiUtil.changeButtonIconColor(AppConstant.PLAY_ICON_PATH, 24, 24);

        playPauseButton.addActionListener(e -> {
            SongDTO currentSong = playerFacade.getCurrentSong();

            if (isSameSong(currentSong, song)) {
                if (playerFacade.isPaused()) {
                    playerFacade.playCurrentSong();
                } else {
                    playerFacade.pauseSong();
                }
            } else {
                if (song.getIsLocalFile()) {
                    playerFacade.loadLocalSong(song);
                } else {
                    AlbumDTO albumDTO = CommonApiUtil.fetchAlbumContainsThisSong(song.getId());
                    playerFacade.loadSongWithContext(song, playerFacade.convertSongListToPlaylist(albumDTO.getSongDTOS(), albumDTO.getTitle()), PlaylistSourceType.ALBUM);
                }
            }
        });

        String key = createButtonKey(song, "songItem", location);
        playPauseButtonMap.put(key, playPauseButton);


        controlButtonsPanel.add(playPauseButton);

        JPanel infoPanel = GuiUtil.createPanel(new BorderLayout());
        String title = song.getTitle();
        String artist = song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist";

        JPanel labelPanel = GuiUtil.createPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));

        JLabel songLabel = GuiUtil.createLabel(title, Font.BOLD, 14);
        JLabel artistLabel = GuiUtil.createLabel(artist, Font.PLAIN, 12);

        labelPanel.add(songLabel);
        labelPanel.add(artistLabel);
        infoPanel.add(labelPanel, BorderLayout.CENTER);

        JPanel durationPanel = GuiUtil.createPanel(new BorderLayout());
        durationPanel.setPreferredSize(new Dimension(50, 30));

        JLabel durationLabel = GuiUtil.createLabel(song.getSongLength(), Font.BOLD, 14,
                ThemeManager.getInstance().getAccentColor());
        durationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        durationPanel.add(durationLabel, BorderLayout.CENTER);

        songPanel.add(controlButtonsPanel, BorderLayout.WEST);
        songPanel.add(infoPanel, BorderLayout.CENTER);
        songPanel.add(durationPanel, BorderLayout.EAST);

        GuiUtil.addHoverEffect(songPanel);
        GuiUtil.addSongContextMenu(songPanel, song);

        songPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Component clickedComponent = SwingUtilities.getDeepestComponentAt(songPanel, e.getX(), e.getY());
                    if (!(clickedComponent instanceof JButton)) {
                        log.info("Song clicked: {}", song.getTitle());
                        HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SearchResultsPanel.this);
                        homePage.navigateToSongDetailsView(song);
                    }
                }
            }
        });

        return songPanel;
    }

    private boolean isSameSong(SongDTO song1, SongDTO song2) {
        if (song1 == null || song2 == null) return false;
        if (!song1.getIsLocalFile() && !song2.getIsLocalFile()) {
            return song1.getId() != null &&
                    song2.getId() != null &&
                    song1.getId().equals(song2.getId());
        }
        return false;
    }


    private JPanel createArtistsSection(List<ArtistDTO> artists) {
        JPanel section = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JLabel headerLabel = GuiUtil.createLabel("Artists", Font.BOLD, 18);
        section.add(headerLabel, "wrap");

        JPanel artistsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 5, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                ""));


        int limit = Math.min(8, artists.size());
        for (int i = 0; i < limit; i++) {
            ArtistDTO artist = artists.get(i);
            artistsGrid.add(createArtistCard(artist), "aligny top");
        }

        section.add(artistsGrid);
        return section;
    }

    private JPanel createArtistCard(ArtistDTO artist) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create circular artist profile image
        AsyncImageLabel profileLabel = new AsyncImageLabel(150, 150, 15, true);
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

        profileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    log.info("Artist clicked: {}", artist.getStageName());
                    HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SearchResultsPanel.this);
                    if (homePage != null) {
                        homePage.navigateToArtistView(artist);
                    }
                }
            }
        });

        return card;
    }

    private JPanel createAlbumsSection(List<AlbumDTO> albums) {
        JPanel section = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        JLabel headerLabel = GuiUtil.createLabel("Albums", Font.BOLD, 18);
        section.add(headerLabel, "wrap");

        JPanel albumsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 5, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                ""));

        // Add albums
        int limit = Math.min(8, albums.size());
        for (int i = 0; i < limit; i++) {
            AlbumDTO album = albums.get(i);
            albumsGrid.add(createAlbumCard(album));
        }

        section.add(albumsGrid, "grow");
        return section;
    }

    private JPanel createAlbumCard(AlbumDTO album) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Create square album cover with rounded corners
        AsyncImageLabel coverLabel = new AsyncImageLabel(150, 150, 15);
        coverLabel.startLoading();
        playerFacade.populateAlbumImage(album, coverLabel::setLoadedImage);

        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font titleFont = FontUtil.getSpotifyFont(Font.BOLD, 12);


        // Album title and artist
        JLabel titleLabel = GuiUtil.createLabel(StringUtils.getTruncatedTextByWidth(album.getTitle(), titleFont), Font.BOLD, 12);
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

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    log.info("Album clicked: {}", album.getTitle());

                    // Navigate to album view
                    HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SearchResultsPanel.this);
                    homePage.navigateToAlbumView(album);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    GuiUtil.addAlbumContextMenu(coverLabel, album);
                }
            }
        });

        return card;
    }

    private JPanel createPlaylistsSection(List<PlaylistDTO> playlists) {
        JPanel section = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        // Header with MigLayout
        JLabel headerLabel = GuiUtil.createLabel("Playlists", Font.BOLD, 18);
        section.add(headerLabel, "wrap");

        // Playlists grid using MigLayout
        JPanel playlistsGrid = GuiUtil.createPanel(new MigLayout(
                "wrap 8, fillx, gapx 5, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                ""));

        // Add playlists
        int limit = Math.min(8, playlists.size());
        for (int i = 0; i < limit; i++) {
            PlaylistDTO playlist = playlists.get(i);
            playlistsGrid.add(createPlaylistCard(playlist));
        }

        section.add(playlistsGrid, "grow");
        return section;
    }

    private JPanel createPlaylistCard(PlaylistDTO playlist) {
        JPanel card = GuiUtil.createPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        AsyncImageLabel coverLabel = new AsyncImageLabel(150, 150, 15);
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Fix alignment
        coverLabel.startLoading();

        if (!playlist.getSongs().isEmpty() && playlist.getFirstSong() != null) {
            playerFacade.populateSongImage(playlist.getFirstSong(), coverLabel::setLoadedImage);
        } else {
            coverLabel.setLoadedImage(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH));
        }

        JPanel textPanel = GuiUtil.createPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.setOpaque(false);

        // Playlist name with truncation
        Font nameFont = FontUtil.getSpotifyFont(Font.BOLD, 12);
        JLabel nameLabel = GuiUtil.createLabel(
                StringUtils.getTruncatedTextByWidth(playlist.getName(), nameFont),
                Font.BOLD, 12);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Song count with consistent styling
        String countText = playlist.getSongs().size() + " song" + (playlist.getSongs().size() != 1 ? "s" : "");
        JLabel countLabel = GuiUtil.createLabel(countText, Font.PLAIN, 11);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components with consistent spacing
        card.add(coverLabel);
        card.add(Box.createVerticalStrut(8)); // Consistent spacing

        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2)); // Small space between name and count
        textPanel.add(countLabel);
        card.add(textPanel);

        // Add hover effect with clearer focus
        GuiUtil.addHoverEffect(card);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HomePage homePage = (HomePage) SwingUtilities.getWindowAncestor(SearchResultsPanel.this);
                homePage.navigateToPlaylistView(playlist, PlaylistSourceType.USER_PLAYLIST);
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
                JPanel songItem = createSongListItem(song, "songsTab");
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
                "wrap 8, fillx, gapx 5, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
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
                "wrap 8, fillx, gapx 5, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
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
                "wrap 8, fillx, gapx 5, gapy 20",
                "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
                "[]"
        ));

        for (PlaylistDTO playlist : playlists) {
            grid.add(createPlaylistCard(playlist));
        }

        playlistsPanel.add(grid, "grow");
    }


    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {

        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);

        updateSelectedTab(currentTab);

        revalidate();
        repaint();
    }

    private void updatePlayPauseButtons(SongDTO currentSong, boolean isPlaying) {
        if (currentSong == null) return;
        String songIdentifier = "remote:" + currentSong.getId();

        for (Map.Entry<String, JButton> entry : playPauseButtonMap.entrySet()) {
            String key = entry.getKey();
            JButton button = entry.getValue();
            if (key.startsWith(songIdentifier + ":")) {
                if (isPlaying) {
                    button.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.PAUSE_ICON_PATH,
                            ThemeManager.getInstance().getTextColor(),
                            24, 24));
                    button.setRolloverIcon(GuiUtil.createColoredIcon(
                            AppConstant.PAUSE_ICON_PATH,
                            GuiUtil.lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f),
                            24, 24)
                    );
                } else {
                    button.setIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH,
                            ThemeManager.getInstance().getTextColor(),
                            24, 24));
                    button.setRolloverIcon(GuiUtil.createColoredIcon(
                            AppConstant.PLAY_ICON_PATH,
                            GuiUtil.lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f),
                            24, 24)
                    );
                }
            } else {
                button.setIcon(GuiUtil.createColoredIcon(
                        AppConstant.PLAY_ICON_PATH,
                        ThemeManager.getInstance().getTextColor(),
                        24, 24));
                button.setRolloverIcon(GuiUtil.createColoredIcon(
                        AppConstant.PLAY_ICON_PATH,
                        GuiUtil.lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f),
                        24, 24)
                );
            }
        }
    }

}