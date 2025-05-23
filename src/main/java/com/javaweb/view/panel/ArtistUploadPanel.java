package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.StringUtils;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ArtistUploadPanel extends JPanel implements ThemeChangeListener {

    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    // Navigation components
    private JPanel navigationPanel;
    private JButton albumButton;
    private JButton singleButton;

    // Content panels with CardLayout
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JScrollPane albumUploadPanel;
    private JScrollPane songUploadPanel;
    private JPanel albumInfoPanel;
    private JPanel songSelectionPanel;

    // Album form fields
    private JTextField albumTitleField;
    private JTextField albumYearField;
    private AsyncImageLabel albumCoverPreview;
    private File selectedAlbumCover;

    // Song upload fields
    private JList<File> songFilesList;
    private DefaultListModel<File> songsListModel;
    private HashMap<File, SongDTO> songMetadataMap;
    private JTextField songTitleField;
    private JTextArea songLyricsArea;

    // Search components
    private JTextField searchField;
    private Timer searchDelayTimer;
    private static final int SEARCH_DELAY = 500;
    private JPanel searchResultsPanel;
    private JPanel searchBarWrapper;

    // Current artist info
    private ArtistDTO currentArtist;
    private AlbumDTO selectedAlbum;

    // Artists selection components
    private JList<ArtistDTO> selectedArtistsJList;
    private DefaultListModel<ArtistDTO> selectedArtistsModel;
    private List<ArtistDTO> availableArtists = new ArrayList<>();
    private JPanel artistSelectionPanel;

    // Constants for card layout
    private static final String ALBUM_PANEL = "ALBUM";
    private static final String SINGLE_PANEL = "SINGLE";
    private String currentTab = ALBUM_PANEL;

    public MusicPlayerFacade playerFacade;

    public ArtistUploadPanel() {
        backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        textColor = ThemeManager.getInstance().getTextColor();
        accentColor = ThemeManager.getInstance().getAccentColor();

        currentArtist = CommonApiUtil.findArtistById(UserSessionManager.getInstance().getCurrentUser().getArtistId());

        playerFacade = App.getBean(MusicPlayerFacade.class);

        songMetadataMap = new HashMap<>();
        selectedArtistsModel = new DefaultListModel<>();

        if (currentArtist != null) {
            selectedArtistsModel.addElement(currentArtist);
        }

        initializeComponents();
        loadAvailableArtists();

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Navigation panel
        createNavigationPanel();

        // Content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = GuiUtil.createPanel(cardLayout);

        // Create content panels
        albumUploadPanel = createAlbumUploadPanel();
        songUploadPanel = createSongUploadPanel();

        // Add panels to card layout
        contentPanel.add(albumUploadPanel, ALBUM_PANEL);
        contentPanel.add(songUploadPanel, SINGLE_PANEL);

        // Main panel combining navigation and content
        JPanel mainPanel = GuiUtil.createPanel(new BorderLayout());
        mainPanel.add(navigationPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Set initial view
        updateSelectedTab(ALBUM_PANEL);
    }

    private void loadAvailableArtists() {
        SwingWorker<List<ArtistDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ArtistDTO> doInBackground() {
                return CommonApiUtil.searchArtists("");
            }

            @Override
            protected void done() {
                try {
                    availableArtists = get();
                    if (availableArtists != null && !availableArtists.isEmpty()) {
                        // Remove current artist from selection options if it exists
                        if (currentArtist != null) {
                            availableArtists.removeIf(artist -> artist.getId().equals(currentArtist.getId()));
                        }
                    }
                } catch (Exception e) {
                    log.error("Error loading available artists", e);
                }
            }
        };
        worker.execute();
    }

    private void createNavigationPanel() {
        navigationPanel = GuiUtil.createPanel(new MigLayout("insets 5 20 5 20, fillx", "", "[]"));
        navigationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GuiUtil.darkenColor(backgroundColor, 0.1f)));

        JPanel buttonPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);

        albumButton = createTabButton("Upload Album", ALBUM_PANEL);
        singleButton = createTabButton("Upload Single", SINGLE_PANEL);

        buttonPanel.add(albumButton);
        buttonPanel.add(singleButton);

        navigationPanel.add(buttonPanel, "left");
    }

    private JButton createTabButton(String text, String tabName) {
        JButton button = GuiUtil.createButton(text);
        button.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14f));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, tabName);
            currentTab = tabName;
            updateSelectedTab(tabName);
        });

        return button;
    }

    private void updateSelectedTab(String selectedTab) {
        // Reset button styling
        GuiUtil.styleButton(albumButton, backgroundColor, textColor, accentColor);
        GuiUtil.styleButton(singleButton, backgroundColor, textColor, accentColor);

        // Style the selected button
        JButton selectedButton = selectedTab.equals(ALBUM_PANEL) ? albumButton : singleButton;
        GuiUtil.styleButton(selectedButton, accentColor, backgroundColor, textColor);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, insets 20 20 10 20",
                "[grow][]",
                "[]10[]"));

        JLabel titleLabel = GuiUtil.createLabel("Artist Studio", Font.BOLD, 32);
        titleLabel.setForeground(accentColor);

        JLabel subtitleLabel = GuiUtil.createLabel("Share your music with the world", Font.PLAIN, 16);
        subtitleLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        JPanel leftPanel = GuiUtil.createPanel(new MigLayout("wrap 1", "[]", "[]0[]"));
        leftPanel.add(titleLabel);
        leftPanel.add(subtitleLabel);

        panel.add(leftPanel, "cell 0 0, growx, wrap");

        // Add search bar
        JPanel searchPanel = createSearchPanel();
        panel.add(searchPanel, "cell 0 1, growx, span 2");

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = GuiUtil.createPanel(new MigLayout("fillx", "[grow]", "[]"));

        // Search bar with wrapper for styling
        searchBarWrapper = GuiUtil.createPanel(new BorderLayout());
        searchBarWrapper.setBorder(GuiUtil.createCompoundBorder(2));
        searchBarWrapper.setPreferredSize(new Dimension(0, 40));

        // Search icon
        JButton lookupIcon = GuiUtil.changeButtonIconColor(AppConstant.LOOKUP_ICON_PATH, 20, 20);
        lookupIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        lookupIcon.addActionListener(e -> performSearch(searchField.getText()));
        GuiUtil.setSmartTooltip(lookupIcon, "Search for artists and albums");

        searchBarWrapper.add(lookupIcon, BorderLayout.WEST);

        // Search field
        searchField = GuiUtil.createInputField("Search for artists or albums...", 20);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        searchDelayTimer = new Timer(SEARCH_DELAY, e -> performSearch(searchField.getText()));
        searchDelayTimer.setRepeats(false);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (searchDelayTimer.isRunning()) {
                    searchDelayTimer.restart();
                } else {
                    searchDelayTimer.start();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchDelayTimer.stop();
                    performSearch(searchField.getText());
                }
            }
        });

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                if (searchField.getText().equals("Search for artists or albums...")) {
                    searchField.setText("");
                    searchField.setForeground(ThemeManager.getInstance().getTextColor());
                }
            }

            @Override
            public void focusLost(FocusEvent evt) {
                if (searchField.getText().isEmpty() || searchField.getText().trim().isEmpty()) {
                    searchField.setText("Search for artists or albums...");
                    searchField.setForeground(GuiUtil.darkenColor(ThemeManager.getInstance().getTextColor(), 0.3f));
                }
            }
        });

        searchBarWrapper.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBarWrapper, "growx");

        // Create panel for search results
        searchResultsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 1", "[grow]", "[]"));
        searchResultsPanel.setBorder(BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f)));
        searchResultsPanel.setVisible(false);

        JPanel containerPanel = GuiUtil.createPanel(new BorderLayout());
        containerPanel.add(searchBarWrapper, BorderLayout.NORTH);
        containerPanel.add(searchResultsPanel, BorderLayout.CENTER);

        searchPanel.add(containerPanel, "growx");

        return searchPanel;
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResultsPanel.setVisible(false);
            return;
        }

        SwingWorker<SearchResults, Void> worker = new SwingWorker<>() {
            @Override
            protected SearchResults doInBackground() {
                SearchResults results = new SearchResults();

                // Search for artists
                results.artists = CommonApiUtil.searchArtists(query);

                // Search for albums
                results.albums = CommonApiUtil.searchAlbums(query);

                return results;
            }

            @Override
            protected void done() {
                try {
                    SearchResults results = get();
                    showSearchResults(query, results);
                } catch (Exception e) {
                    log.error("Error performing search", e);
                }
            }
        };

        worker.execute();
    }

    private static class SearchResults {
        List<ArtistDTO> artists = new ArrayList<>();
        List<AlbumDTO> albums = new ArrayList<>();

        boolean hasResults() {
            return !(artists.isEmpty() && albums.isEmpty());
        }
    }

    private void showSearchResults(String query, SearchResults results) {
        searchResultsPanel.removeAll();

        if (!results.hasResults()) {
            JLabel noResultsLabel = GuiUtil.createLabel("No results found for \"" + query + "\"", Font.ITALIC, 14);
            noResultsLabel.setForeground(GuiUtil.darkenColor(textColor, 0.2f));
            searchResultsPanel.add(noResultsLabel, "pad 10");
        } else {
            // Add header for artists section if there are artists
            if (!results.artists.isEmpty()) {
                JLabel artistsHeader = GuiUtil.createLabel("Artists", Font.BOLD, 16);
                artistsHeader.setForeground(accentColor);
                searchResultsPanel.add(artistsHeader, "pad 5 5 0 5");

                for (ArtistDTO artist : results.artists.subList(0, Math.min(5, results.artists.size()))) {
                    JPanel artistPanel = createSearchResultArtistPanel(artist);
                    searchResultsPanel.add(artistPanel, "growx, pad 2");
                }

                // Add separator
                if (!results.albums.isEmpty()) {
                    searchResultsPanel.add(new JSeparator(), "growx, pad 10 5 5 5");
                }
            }

            // Add header for albums section if there are albums
            if (!results.albums.isEmpty()) {
                JLabel albumsHeader = GuiUtil.createLabel("Albums", Font.BOLD, 16);
                albumsHeader.setForeground(accentColor);
                searchResultsPanel.add(albumsHeader, "pad 5 5 0 5");

                for (AlbumDTO album : results.albums.subList(0, Math.min(5, results.albums.size()))) {
                    JPanel albumPanel = createSearchResultAlbumPanel(album);
                    searchResultsPanel.add(albumPanel, "growx, pad 2");
                }
            }
        }

        searchResultsPanel.setVisible(true);
        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();
    }

    private JPanel createSearchResultArtistPanel(ArtistDTO artist) {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(GuiUtil.lightenColor(backgroundColor, 0.1f));

        JLabel artistNameLabel = GuiUtil.createLabel(artist.getStageName(), Font.BOLD, 14);

        panel.add(artistNameLabel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(GuiUtil.lightenColor(backgroundColor, 0.2f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(GuiUtil.lightenColor(backgroundColor, 0.1f));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Add artist to collaborators if not already added
                if (!containsArtist(selectedArtistsModel, artist)) {
                    selectedArtistsModel.addElement(artist);
                }

                // Hide search results
                searchResultsPanel.setVisible(false);
                searchField.setText("");
            }
        });

        return panel;
    }

    private JPanel createSearchResultAlbumPanel(AlbumDTO album) {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(GuiUtil.lightenColor(backgroundColor, 0.1f));

        JPanel infoPanel = GuiUtil.createPanel(new MigLayout("insets 0, fillx", "[]", "[]0[]"));

        JLabel albumNameLabel = GuiUtil.createLabel(album.getTitle(), Font.BOLD, 14);
        JLabel artistNameLabel = GuiUtil.createLabel(album.getArtistName(), Font.PLAIN, 12);
        artistNameLabel.setForeground(GuiUtil.darkenColor(textColor, 0.2f));

        infoPanel.add(albumNameLabel, "wrap");
        infoPanel.add(artistNameLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(GuiUtil.lightenColor(backgroundColor, 0.2f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(GuiUtil.lightenColor(backgroundColor, 0.1f));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Set the selected album
                selectedAlbum = album;

                // Update the album info
                albumTitleField.setText(album.getTitle());
                if (album.getReleaseYear() != null) {
                    albumYearField.setText(String.valueOf(album.getReleaseYear()));
                }

                // Load album cover if available
                if (album.getImageId() != null) {
                    playerFacade.populateAlbumImage(album, image -> albumCoverPreview.setLoadedImage(image));
                }

                // Load artist
                ArtistDTO artist = new ArtistDTO();
                artist.setId(album.getArtistId());
                artist.setStageName(album.getArtistName());

                // Clear and set artist
                selectedArtistsModel.clear();
                selectedArtistsModel.addElement(artist);

                // Hide search results
                searchResultsPanel.setVisible(false);
                searchField.setText("");

                // Show album panel
                cardLayout.show(contentPanel, ALBUM_PANEL);
                updateSelectedTab(ALBUM_PANEL);
            }
        });

        return panel;
    }

    private JScrollPane createAlbumUploadPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, insets 20",
                "[grow]",
                "[]20[]"));

        // Info panel
        albumInfoPanel = createAlbumInfoPanel();
        panel.add(albumInfoPanel, "grow, wrap");

        // Song selection panel
        songSelectionPanel = createSongSelectionPanel();
        panel.add(songSelectionPanel, "grow");

        return GuiUtil.createStyledScrollPane(panel);
    }

    private JPanel createAlbumInfoPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, insets 0",
                "[250!,center]20[grow]",
                "[]"));

        // Album cover panel (left side)
        JPanel coverPanel = GuiUtil.createPanel(new MigLayout("wrap 1", "[]", "[]15[]"));

        albumCoverPreview = new AsyncImageLabel(250, 250, 15);
        albumCoverPreview.setLoadedImage(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH));

        JButton selectCoverButton = GuiUtil.createButton("Choose Album Cover");
        GuiUtil.styleButton(selectCoverButton, backgroundColor, textColor, accentColor);
        selectCoverButton.addActionListener(e -> selectAlbumCoverImage());

        coverPanel.add(albumCoverPreview, "center");
        coverPanel.add(selectCoverButton, "center");

        // Album details panel (right side)
        JPanel detailsPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 2, insets 0",
                "[][grow, fill]",
                "[]15[]15[]"));

        JLabel titleLabel = GuiUtil.createLabel("Album Title:", Font.BOLD, 14);
        albumTitleField = GuiUtil.createTextField(20);

        JLabel yearLabel = GuiUtil.createLabel("Release Year:", Font.BOLD, 14);
        albumYearField = GuiUtil.createTextField(String.valueOf(new Date().getYear() + 1900), 20);

        detailsPanel.add(titleLabel, "right");
        detailsPanel.add(albumTitleField);
        detailsPanel.add(yearLabel, "right");
        detailsPanel.add(albumYearField);

        // Add artists selection panel
        artistSelectionPanel = createArtistSelectionPanel();
        detailsPanel.add(artistSelectionPanel, "span 2, grow");

        panel.add(coverPanel, "cell 0 0");
        panel.add(detailsPanel, "cell 1 0, grow");

        return panel;
    }

    private JPanel createArtistSelectionPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 0",
                "[grow]",
                "[]5[]"));

        JLabel titleLabel = GuiUtil.createLabel("Collaborative Artists", Font.BOLD, 14);
        titleLabel.setForeground(accentColor);

        // List of selected artists
        selectedArtistsJList = GuiUtil.createStyledList(selectedArtistsModel);
        selectedArtistsJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof ArtistDTO artist) {
                    label.setText(artist.getStageName());

                    // Make the text bold for current artist
                    if (currentArtist != null && artist.getId().equals(currentArtist.getId())) {
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                    }
                }

                return label;
            }
        });
        // Add context menu to remove artists
        selectedArtistsJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = selectedArtistsJList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        // Don't allow removing the current artist
                        selectedArtistsJList.setSelectedIndex(index);
                        JPopupMenu popupMenu = showArtistContextMenu(selectedArtistsJList.getSelectedValue());
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(selectedArtistsJList);
        scrollPane.setPreferredSize(new Dimension(0, 100));

        // Add all components
        panel.add(titleLabel);
        panel.add(scrollPane, "grow");

        return panel;
    }

    private JPopupMenu showArtistContextMenu(ArtistDTO artist) {

        JPopupMenu contextMenu = GuiUtil.createPopupMenu(backgroundColor, textColor);

        JMenuItem removeItem = GuiUtil.createMenuItem("Remove Artist");
        removeItem.addActionListener(event -> selectedArtistsModel.removeElement(artist));

        contextMenu.add(removeItem);

        GuiUtil.registerPopupMenuForThemeUpdates(contextMenu);
        return contextMenu;
    }

    private boolean containsArtist(DefaultListModel<ArtistDTO> model, ArtistDTO artist) {
        for (int i = 0; i < model.getSize(); i++) {
            ArtistDTO existing = model.getElementAt(i);
            if (existing.getId().equals(artist.getId())) {
                return true;
            }
        }
        return false;
    }

    private JPanel createSongSelectionPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, insets 0",
                "[grow]",
                "[]10[]"));

        JPanel headerPanel = GuiUtil.createPanel(new MigLayout(
                "fillx",
                "[][grow, right]",
                "[]"));

        JLabel songsLabel = GuiUtil.createLabel("Songs", Font.BOLD, 22);

        JPanel buttonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addSongsButton = GuiUtil.createButton("Add Songs");
        GuiUtil.styleButton(addSongsButton, backgroundColor, textColor, accentColor);
        addSongsButton.addActionListener(e -> selectMusicFiles());

        JButton uploadAlbumButton = GuiUtil.createButton("Upload Album");
        uploadAlbumButton.addActionListener(e -> uploadAlbum());

        buttonsPanel.add(addSongsButton);
        buttonsPanel.add(uploadAlbumButton);

        headerPanel.add(songsLabel);
        headerPanel.add(buttonsPanel);

        // Song files list
        songsListModel = new DefaultListModel<>();
        songFilesList = GuiUtil.createStyledList(songsListModel);
        songFilesList.setCellRenderer(new SongFileCellRenderer());
        songFilesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    File selectedFile = songFilesList.getSelectedValue();
                    if (selectedFile != null) {
                        showSongDetailsDialog(selectedFile);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popupMenu = showSongContextMenu(e);
                    popupMenu.show(songFilesList, e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(songFilesList);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        panel.add(headerPanel, "grow, wrap");
        panel.add(scrollPane, "grow");

        return panel;
    }

    private JScrollPane createSongUploadPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 20",
                "[grow]",
                "[]15[]15[]"));

        // Title
        JLabel titleHeader = GuiUtil.createLabel("Upload Single Track", Font.BOLD, 24);
        titleHeader.setForeground(accentColor);
        panel.add(titleHeader, "wrap, gapbottom 20");

        // Artist selection panel for single track
        JPanel singleArtistPanel = createArtistSelectionPanel();
        panel.add(singleArtistPanel, "grow, wrap");

        // File selection panel
        JPanel fileSelectionPanel = GuiUtil.createPanel(new MigLayout(
                "fillx",
                "[][grow, fill][]",
                "[]"));

        JLabel fileLabel = GuiUtil.createLabel("Song File:", Font.BOLD, 14);

        AtomicReference<File> selectedSongFile = new AtomicReference<>();
        JLabel fileNameLabel = GuiUtil.createLabel("No file selected", Font.ITALIC, 14);
        fileNameLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

        JButton selectFileButton = GuiUtil.createButton("Choose File");
        GuiUtil.styleButton(selectFileButton, backgroundColor, textColor, accentColor);
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "mp3", "wav", "ogg"));

            fileChooser.setCurrentDirectory(new File("D:\\MuseMoe resources\\audio"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileNameLabel.setText(file.getName());
                fileNameLabel.setForeground(textColor);
                selectedSongFile.set(file);
            }
        });

        JButton uploadButton = GuiUtil.createButton("Upload Song");
        GuiUtil.styleButton(uploadButton, accentColor, Color.WHITE, Color.WHITE);
        uploadButton.addActionListener(e -> {
            if (selectedSongFile.get() == null) {
                GuiUtil.showWarningMessageDialog(this, "Please select a song file to upload.");
                return;
            }

            List<Long> artistIds = new ArrayList<>();
            for (int i = 0; i < selectedArtistsModel.getSize(); i++) {
                artistIds.add(selectedArtistsModel.getElementAt(i).getId());
            }

            if (artistIds.isEmpty()) {
                GuiUtil.showWarningMessageDialog(this, "Please select at least one artist.");
                return;
            }

            uploadSingleSong(selectedSongFile.get(), artistIds);
        });

        fileSelectionPanel.add(fileLabel, "");
        fileSelectionPanel.add(fileNameLabel, "grow");
        fileSelectionPanel.add(selectFileButton, "");

        panel.add(fileSelectionPanel, "growx, wrap");
        panel.add(uploadButton, "center");

        return GuiUtil.createStyledScrollPane(panel);
    }

    private void selectAlbumCoverImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));
        fileChooser.setCurrentDirectory(new File("D:\\MuseMoe resources\\imgs\\album_cover"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                // Load and display image preview
                BufferedImage image = ImageIO.read(selectedFile);
                if (image != null) {
                    albumCoverPreview.setLoadedImage(image);
                    selectedAlbumCover = selectedFile;
                }
            } catch (Exception e) {
                log.error("Error loading album cover image", e);
                GuiUtil.showErrorMessageDialog(this, "Unable to load image. Please try another file.");
            }
        }
    }

    private void selectMusicFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "mp3", "wav", "ogg"));
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setCurrentDirectory(new File("D:\\MuseMoe resources\\audio"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if (!containsFile(songsListModel, file)) {
                    songsListModel.addElement(file);

                    // Create default metadata for the file
                    SongDTO metadata = new SongDTO();
                    metadata.setTitle(file.getName().replaceFirst("[.][^.]+$", ""));
                    songMetadataMap.put(file, metadata);
                }
            }
        }
    }

    private boolean containsFile(DefaultListModel<File> model, File file) {
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).getAbsolutePath().equals(file.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    private void showSongDetailsDialog(File songFile) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Song Details", true);
        GuiUtil.styleTitleBar(dialog, backgroundColor, textColor);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 2, insets 15",
                "[][grow, fill]",
                "[]10[]10[]"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = GuiUtil.createLabel("Song Title:", Font.BOLD, 14);
        JTextField titleField = GuiUtil.createTextField(StringUtils.getTruncatedText(
                songFile.getName().replaceFirst("[.][^.]+$", ""), 50), 50);

        JLabel trackLabel = GuiUtil.createLabel("Track Number:", Font.BOLD, 14);
        JSpinner trackSpinner = new JSpinner(new SpinnerNumberModel(songsListModel.indexOf(songFile) + 1, 1, 100, 1));
        trackSpinner.setEditor(new JSpinner.NumberEditor(trackSpinner, "#"));

        JLabel lyricsLabel = GuiUtil.createLabel("Lyrics:", Font.BOLD, 14);
        JTextArea lyricsArea = GuiUtil.createTextArea("", FontUtil.getSpotifyFont(Font.PLAIN, 14));
        lyricsArea.setRows(5);
        lyricsArea.setLineWrap(true);
        lyricsArea.setWrapStyleWord(true);

        SongDTO metadata = songMetadataMap.get(songFile);
        if (metadata != null) {
            titleField.setText(metadata.getTitle());
            lyricsArea.setText(metadata.getSongLyrics());
        }

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(lyricsArea);

        contentPanel.add(titleLabel, "right");
        contentPanel.add(titleField);
        contentPanel.add(trackLabel, "right");
        contentPanel.add(trackSpinner);
        contentPanel.add(lyricsLabel, "right, top");
        contentPanel.add(scrollPane, "grow");

        JPanel buttonPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = GuiUtil.createButton("Save");
        GuiUtil.styleButton(saveButton, backgroundColor, textColor, accentColor);
        saveButton.addActionListener(e -> {
            // Save metadata
            SongDTO songMetadata = new SongDTO();
            songMetadata.setTitle(titleField.getText());
            songMetadata.setSongLyrics(lyricsArea.getText());
            songMetadataMap.put(songFile, songMetadata);
            dialog.dispose();
            songFilesList.repaint();  // Refresh list to show updated metadata
        });

        JButton cancelButton = GuiUtil.createButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, "span 2, right");

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private JPopupMenu showSongContextMenu(MouseEvent e) {
        int index = songFilesList.locationToIndex(e.getPoint());
        songFilesList.setSelectedIndex(index);
        File selectedFile = songFilesList.getSelectedValue();

        JPopupMenu contextMenu = GuiUtil.createPopupMenu(backgroundColor, textColor);

        JMenuItem editItem = GuiUtil.createMenuItem("Edit Song Details");
        editItem.addActionListener(event -> showSongDetailsDialog(selectedFile));

        JMenuItem removeItem = GuiUtil.createMenuItem("Remove Song");
        removeItem.addActionListener(event -> {
            songsListModel.removeElement(selectedFile);
            songMetadataMap.remove(selectedFile);
        });

        contextMenu.add(editItem);
        contextMenu.add(removeItem);

        GuiUtil.registerPopupMenuForThemeUpdates(contextMenu);
        contextMenu.show(songFilesList, e.getX(), e.getY());
        return contextMenu;
    }

    private void uploadAlbum() {
        // Validate fields
        if (albumTitleField.getText().trim().isEmpty()) {
            GuiUtil.showWarningMessageDialog(this, "Please enter an album title.");
            return;
        }

        if (songsListModel.isEmpty()) {
            GuiUtil.showWarningMessageDialog(this, "Please add at least one song to the album.");
            return;
        }

//        if (selectedAlbum == null) {
//            GuiUtil.showWarningMessageDialog(this, "Please select an album cover.");
//            return;
//        }

        // Validate artists
        if (selectedArtistsModel.size() == 0) {
            GuiUtil.showWarningMessageDialog(this, "Please select at least one artist.");
            return;
        }

        // Show progress dialog
        JDialog progressDialog = GuiUtil.createProgressDialog(
                SwingUtilities.getWindowAncestor(this),
                "Uploading Album",
                "Uploading album " + albumTitleField.getText());

        JProgressBar progressBar = GuiUtil.findFirstComponentByType(
                progressDialog.getContentPane(),
                JProgressBar.class,
                bar -> true
        );

        if (progressBar != null) {
            progressBar.setStringPainted(true);
        }

        SwingWorker<Boolean, Integer> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    publish(10);

                    Long albumId;

                    if (selectedAlbum == null) {
                        // Prepare album data
                        AlbumRequestDTO album = new AlbumRequestDTO();
                        album.setTitle(albumTitleField.getText().trim());
                        album.setReleaseYear(Integer.parseInt(albumYearField.getText().trim()));

                        // Use the first artist as primary artist for the album
                        Long primaryArtistId = selectedArtistsModel.getElementAt(0).getId();
                        album.setArtistId(primaryArtistId);

                        // Upload album cover
                        publish(20);
                        String coverFileName = album.getTitle() + ".jpg";
                        MockMultipartFile coverFile = new MockMultipartFile(
                                coverFileName,
                                coverFileName,
                                Files.probeContentType(selectedAlbumCover.toPath()),
                                Files.readAllBytes(selectedAlbumCover.toPath())
                        );
                        album.setAlbumCover(coverFile);

                        // Create album in database
                        publish(30);
                        AlbumDTO createdAlbum = CommonApiUtil.createAlbum(album);
                        if (createdAlbum == null || createdAlbum.getId() == null) {
                            return false;
                        }

                        albumId = createdAlbum.getId();
                    } else {
                        albumId = selectedAlbum == null ? null : selectedAlbum.getId();
                    }


                    // Prepare artist IDs for songs
                    List<Long> artistIds = new ArrayList<>();
                    for (int i = 0; i < selectedArtistsModel.getSize(); i++) {
                        artistIds.add(selectedArtistsModel.getElementAt(i).getId());
                    }

                    // Upload songs
                    SongRequestDTO songRequest = new SongRequestDTO();

                    List<MultipartFile> songFiles = new ArrayList<>();
                    int total = songsListModel.getSize();

                    for (int i = 0; i < total; i++) {
                        File songFile = songsListModel.getElementAt(i);
                        // Upload song file
                        int progress = 30 + (i * 60 / total);
                        publish(progress);

                        String songFileName = songFile.getName();
                        MockMultipartFile multipartFile = new MockMultipartFile(
                                songFileName,
                                songFileName,
                                Files.probeContentType(songFile.toPath()),
                                Files.readAllBytes(songFile.toPath())
                        );

                        // Add to list
                        songFiles.add(multipartFile);
                    }

                    songRequest.setAlbumId(albumId);
                    songRequest.setArtistIds(artistIds);
                    songRequest.setMp3Files(songFiles);
                    // Save songs
                    publish(90);
                    boolean success = CommonApiUtil.createSongsForAlbum(songRequest);
                    publish(100);

                    return success;
                } catch (Exception e) {
                    log.error("Error uploading album", e);
                    return false;
                }
            }

            @Override
            protected void process(List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                if (progressBar != null) {
                    progressBar.setValue(latest);
                    progressBar.setString(latest + "%");
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        GuiUtil.showSuccessMessageDialog(
                                SwingUtilities.getWindowAncestor(ArtistUploadPanel.this),
                                "Album uploaded successfully!");

                        // Clear form
                        albumTitleField.setText("");
                        albumYearField.setText(String.valueOf(new Date().getYear() + 1900));
                        albumCoverPreview.setLoadedImage(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH));
                        selectedAlbumCover = null;
                        songsListModel.clear();
                        songMetadataMap.clear();

                        // Keep only the current artist selected
                        if (currentArtist != null) {
                            selectedArtistsModel.clear();
                            selectedArtistsModel.addElement(currentArtist);
                        }
                    } else {
                        GuiUtil.showErrorMessageDialog(
                                SwingUtilities.getWindowAncestor(ArtistUploadPanel.this),
                                "Failed to upload album. Please try again later.");
                    }
                } catch (Exception e) {
                    log.error("Error completing album upload", e);
                    GuiUtil.showErrorMessageDialog(
                            SwingUtilities.getWindowAncestor(ArtistUploadPanel.this),
                            "An unexpected error occurred. Please try again later.");
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private void uploadSingleSong(File songFile, List<Long> artistIds) {
        // Show progress dialog
        JDialog progressDialog = GuiUtil.createProgressDialog(
                SwingUtilities.getWindowAncestor(this),
                "Uploading Song",
                "Uploading " + songFile.getName());

        JProgressBar progressBar = GuiUtil.findFirstComponentByType(
                progressDialog.getContentPane(),
                JProgressBar.class,
                bar -> true
        );

        if (progressBar != null) {
            progressBar.setStringPainted(true);
        }

        SwingWorker<Boolean, Integer> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    // Create song DTO
                    SongRequestDTO songRequest = new SongRequestDTO();
                    songRequest.setArtistIds(artistIds);

                    // Upload song file
                    publish(30);
                    String songFileName = songFile.getName();
                    MockMultipartFile multipartFile = new MockMultipartFile(
                            songFileName,
                            songFileName,
                            Files.probeContentType(songFile.toPath()),
                            Files.readAllBytes(songFile.toPath())
                    );

                    songRequest.setMp3Files(List.of(multipartFile));

                    // Save song
                    publish(80);
                    boolean success = CommonApiUtil.createSong(songRequest);
                    publish(100);

                    return success;
                } catch (Exception e) {
                    log.error("Error uploading single song", e);
                    return false;
                }
            }

            @Override
            protected void process(List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                if (progressBar != null) {
                    progressBar.setValue(latest);
                    progressBar.setString(latest + "%");
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        GuiUtil.showSuccessMessageDialog(
                                SwingUtilities.getWindowAncestor(ArtistUploadPanel.this),
                                "Song uploaded successfully!");
                    } else {
                        GuiUtil.showErrorMessageDialog(
                                SwingUtilities.getWindowAncestor(ArtistUploadPanel.this),
                                "Failed to upload song. Please try again later.");
                    }
                } catch (Exception e) {
                    log.error("Error completing song upload", e);
                    GuiUtil.showErrorMessageDialog(
                            SwingUtilities.getWindowAncestor(ArtistUploadPanel.this),
                            "An unexpected error occurred. Please try again later.");
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        // Update component colors
        setBackground(backgroundColor);

        // Update search bar styling
        if (searchBarWrapper != null) {
            searchBarWrapper.setBorder(GuiUtil.createCompoundBorder(2));
        }

        // Update panels
        SwingUtilities.updateComponentTreeUI(this);
    }

    // Custom cell renderer for song files
    private class SongFileCellRenderer extends JPanel implements ListCellRenderer<File> {
        private final JLabel fileNameLabel;
        private final JLabel metadataLabel;

        public SongFileCellRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel textPanel = GuiUtil.createPanel(new MigLayout("insets 0, gap 0", "[]", "[]0[]"));

            fileNameLabel = GuiUtil.createLabel("", Font.BOLD, 14);
            metadataLabel = GuiUtil.createLabel("", Font.PLAIN, 12);
            metadataLabel.setForeground(GuiUtil.darkenColor(textColor, 0.3f));

            textPanel.add(fileNameLabel, "wrap");
            textPanel.add(metadataLabel);

            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends File> list, File file,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            // Set file name
            fileNameLabel.setText(file.getName());

            // Set metadata if available
            SongDTO metadata = songMetadataMap.get(file);
            if (metadata != null && metadata.getTitle() != null) {
                String metaText = metadata.getTitle();
                metadataLabel.setText(metaText);
            } else {
                metadataLabel.setText("No metadata");
            }

            // Handle selection
            if (isSelected) {
                setBackground(GuiUtil.lightenColor(accentColor, 0.7f));
                fileNameLabel.setForeground(textColor);
            } else {
                setBackground(index % 2 == 0 ? backgroundColor : GuiUtil.lightenColor(backgroundColor, 0.05f));
                fileNameLabel.setForeground(textColor);
            }

            return this;
        }
    }
}