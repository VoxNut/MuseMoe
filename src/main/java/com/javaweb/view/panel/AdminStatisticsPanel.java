package com.javaweb.view.panel;

import com.javaweb.constant.AppConstant;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.*;
import com.javaweb.utils.*;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class AdminStatisticsPanel extends JPanel implements ThemeChangeListener {
    // Card names
    private static final String SUMMARY_CARD = "SUMMARY";
    private static final String SONGS_CARD = "SONGS";
    private static final String ARTISTS_CARD = "ARTISTS";
    private static final String ALBUMS_CARD = "ALBUMS";
    private static final String USERS_CARD = "USERS";
    private static final String PLAYLISTS_CARD = "PLAYLISTS";
    private static final String CHARTS_CARD = "CHARTS";

    // Main components
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel navigationPanel;

    // Card panels
    private JPanel summaryPanel;
    private JPanel songsPanel;
    private JPanel artistsPanel;
    private JPanel albumsPanel;
    private JPanel usersPanel;
    private JPanel playlistsPanel;
    private JPanel chartsPanel;

    // Theme colors
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    // Data tables
    private JTable songsTable;
    private JTable artistsTable;
    private JTable albumsTable;
    private JTable usersTable;
    private JTable playlistsTable;
    private JTable recentUserTable;


    // Table models
    private DefaultTableModel songsTableModel;
    private DefaultTableModel artistsTableModel;
    private DefaultTableModel albumsTableModel;
    private DefaultTableModel usersTableModel;
    private DefaultTableModel playlistsTableModel;
    private DefaultTableModel recentUserModel;

    // Filters
    private JComboBox<String> yearFilterComboBox;
    private JComboBox<String> genreFilterComboBox;
    private JComboBox<String> artistFilterComboBox;
    private JComboBox<String> roleFilterComboBox;
    private JTextField fromDateTextField;
    private JTextField toDateTextField;

    // Statistics labels
    private JLabel songCountLabel;
    private JLabel artistCountLabel;
    private JLabel albumCountLabel;
    private JLabel userCountLabel;
    private JLabel playlistCountLabel;
    private JLabel tagCountLabel;

    // Navigation buttons with active indicator
    private Map<String, JButton> navigationButtons = new HashMap<>();
    private String currentCard = SUMMARY_CARD;


    public AdminStatisticsPanel() {
        // Initialize theme colors
        this.backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        this.textColor = ThemeManager.getInstance().getTextColor();
        this.accentColor = ThemeManager.getInstance().getAccentColor();

        initComponents();
        loadInitialData();

        // Register for theme updates
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Create navigation panel
        createNavigationPanel();
        add(navigationPanel, BorderLayout.NORTH);

        // Create main content with CardLayout
        cardLayout = new CardLayout();
        contentPanel = GuiUtil.createPanel(cardLayout);

        // Create cards
        createSummaryPanel();
        createSongsPanel();
        createArtistsPanel();
        createAlbumsPanel();
        createUsersPanel();
        createPlaylistsPanel();
        createChartsPanel();

        // Add cards to content panel
        contentPanel.add(summaryPanel, SUMMARY_CARD);
        contentPanel.add(songsPanel, SONGS_CARD);
        contentPanel.add(artistsPanel, ARTISTS_CARD);
        contentPanel.add(albumsPanel, ALBUMS_CARD);
        contentPanel.add(usersPanel, USERS_CARD);
        contentPanel.add(playlistsPanel, PLAYLISTS_CARD);
        contentPanel.add(chartsPanel, CHARTS_CARD);

        // Create scrollable panel
        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Show initial card
        cardLayout.show(contentPanel, SUMMARY_CARD);
        updateNavigationButtonStates(SUMMARY_CARD);
    }


    private void createNavigationPanel() {
        navigationPanel = GuiUtil.createPanel(new MigLayout("fillx, insets 10 20 10 20", "[left][grow, right]", "[]"));
        navigationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GuiUtil.darkenColor(backgroundColor, 0.1f)));

        JLabel titleLabel = GuiUtil.createLabel("Admin Statistics Dashboard", Font.BOLD, 22);
        navigationPanel.add(titleLabel, "left");

        JPanel buttonsPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // Create navigation buttons
        String[] navItems = {SUMMARY_CARD, SONGS_CARD, ARTISTS_CARD, ALBUMS_CARD, USERS_CARD, PLAYLISTS_CARD, CHARTS_CARD};

        for (String navItem : navItems) {
            JButton navButton = createNavigationButton(navItem);
            buttonsPanel.add(navButton);
            navigationButtons.put(navItem, navButton);
        }

        navigationPanel.add(buttonsPanel, "right");
    }

    private JButton createNavigationButton(String text) {
        JButton button = GuiUtil.createButton(text);
        GuiUtil.styleButton(button, textColor, backgroundColor, accentColor);
        button.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

        // Add action to switch cards
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, text);
            currentCard = text;
            updateNavigationButtonStates(text);
        });

        return button;
    }

    private void updateNavigationButtonStates(String activeCard) {
        navigationButtons.forEach((card, button) -> {
            if (card.equals(activeCard)) {
                GuiUtil.styleButton(button, accentColor, textColor, accentColor);
                button.setFont(FontUtil.getSpotifyFont(Font.BOLD, 16));
            } else {
                GuiUtil.styleButton(button, textColor, backgroundColor, accentColor);
                button.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
            }
        });
    }

    private void createSummaryPanel() {
        summaryPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap, insets 20", "[grow, fill]", "[]20[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Summary Overview", Font.BOLD, 24);
        summaryPanel.add(headerLabel, "left");

        // Stats cards container
        JPanel statsCardsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap 3, gap 20", "[grow, fill][grow, fill][grow, fill]", "[][]"));

        // Create statistic cards for each entity
        statsCardsPanel.add(createStatCard("Songs", "0", AppConstant.SONG_ICON_PATH), "grow");
        statsCardsPanel.add(createStatCard("Artists", "0", AppConstant.ARTIST_ICON_PATH), "grow");
        statsCardsPanel.add(createStatCard("Albums", "0", AppConstant.ALBUM_COVER_ICON_PATH), "grow");
        statsCardsPanel.add(createStatCard("Users", "0", AppConstant.USER_ICON_PATH), "grow");
        statsCardsPanel.add(createStatCard("Playlists", "0", AppConstant.PLAYLIST_ICON_PATH), "grow");
        statsCardsPanel.add(createStatCard("Tags", "0", AppConstant.TAG_ICON_PATH), "grow");

        summaryPanel.add(statsCardsPanel, "grow");

        // Recent activity section
        JLabel recentActivityLabel = GuiUtil.createLabel("Recent Activity", Font.BOLD, 20);
        summaryPanel.add(recentActivityLabel, "left, gaptop 20");

        // Create recent activity panel with latest registrations, uploads, etc.
        JPanel recentActivityPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow]", "[]"));

        // Recent users table
        String[] recentUserColumns = {"User ID", "Username", "Email", "Registered Date"};
        recentUserModel = new DefaultTableModel(recentUserColumns, 0);
        recentUserTable = GuiUtil.createStyledTable(recentUserColumns, new int[]{50, 200, 300, 150});
        recentUserTable.setModel(recentUserModel);

        JScrollPane recentUserScroll = GuiUtil.createStyledScrollPane(recentUserTable);
        recentUserScroll.setPreferredSize(new Dimension(0, 400));

        recentActivityPanel.add(recentUserScroll, "grow");
        summaryPanel.add(recentActivityPanel, "grow");
    }

    private JPanel createStatCard(String title, String value, String iconPath) {
        JPanel card = GuiUtil.createPanel(new MigLayout("fillx, insets 15", "[]10[grow, right]", "[]10[]"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Icon on left
        JLabel iconLabel = GuiUtil.createIconLabel(iconPath, 48, accentColor);
        card.add(iconLabel, "cell 0 0 1 2");

        // Title on top right
        JLabel titleLabel = GuiUtil.createLabel(title, Font.BOLD, 16);
        card.add(titleLabel, "cell 1 0");

        // Value on bottom right
        JLabel valueLabel = GuiUtil.createLabel(value, Font.BOLD, 24);
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, "cell 1 1");

        // Store reference to update value later
        switch (title) {
            case "Songs" -> songCountLabel = valueLabel;
            case "Artists" -> artistCountLabel = valueLabel;
            case "Albums" -> albumCountLabel = valueLabel;
            case "Users" -> userCountLabel = valueLabel;
            case "Playlists" -> playlistCountLabel = valueLabel;
            case "Tags" -> tagCountLabel = valueLabel;
        }

        // Add hover effect
        GuiUtil.addHoverEffect(card);

        return card;
    }

    private void createSongsPanel() {
        songsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]10[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Songs Management", Font.BOLD, 24);
        songsPanel.add(headerLabel, "left");

        // Filters panel
        JPanel filtersPanel = GuiUtil.createPanel(new MigLayout("fillx", "[grow]10[][grow]10[][grow]10[][grow]10[]", "[]"));
        filtersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f), 1, true),
                "Filters",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FontUtil.getSpotifyFont(Font.BOLD, 14),
                textColor));


        JLabel searchLabel = GuiUtil.createLabel("Search Song:", Font.BOLD, 14);
        JTextField searchField = GuiUtil.createTextField(20);


        JPanel searchPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Year filter
        JLabel yearLabel = GuiUtil.createLabel("Year:", Font.BOLD, 14);
        yearFilterComboBox = GuiUtil.createComboBox();
        yearFilterComboBox.addItem("All Years");
        // Add years from 1950 to current year
        int currentYear = java.time.Year.now().getValue();
        for (int year = currentYear; year >= 1950; year--) {
            yearFilterComboBox.addItem(String.valueOf(year));
        }

        // Genre filter
        JLabel genreLabel = GuiUtil.createLabel("Genre:", Font.BOLD, 14);
        genreFilterComboBox = GuiUtil.createComboBox();
        genreFilterComboBox.addItem("All Genres");

        // Artist filter
        JLabel artistLabel = GuiUtil.createLabel("Artist:", Font.BOLD, 14);
        artistFilterComboBox = GuiUtil.createComboBox();
        artistFilterComboBox.addItem("All Artists");

        // Apply button
        JButton applyFilterButton = GuiUtil.createButton("Apply Filters");
        applyFilterButton.addActionListener(e -> applySongsFilters());

        // Add components to filters panel
        filtersPanel.add(searchPanel, "right");
        filtersPanel.add(yearLabel, "right");
        filtersPanel.add(yearFilterComboBox);
        filtersPanel.add(genreLabel, "right");
        filtersPanel.add(genreFilterComboBox);
        filtersPanel.add(artistLabel, "right");
        filtersPanel.add(artistFilterComboBox);
        filtersPanel.add(applyFilterButton);

        songsPanel.add(filtersPanel, "growx");

        // Create songs table
        String[] songColumns = {"ID", "Title", "Artist", "Album", "Year", "Duration", "Play Count"};
        songsTableModel = new DefaultTableModel(songColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(songsTableModel);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.isBlank()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
                }
            }
        });

        songsTable = GuiUtil.createStyledTable(songColumns, new int[]{50, 250, 150, 150, 80, 100, 100});
        songsTable.setModel(songsTableModel);

        // Add table sorting
        songsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(songsTable);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        GuiUtil.applyModernScrollBar(scrollPane);

        songsPanel.add(scrollPane, "grow");

        // Add action buttons panel
        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton refreshButton = GuiUtil.createButton("Refresh Data");
        refreshButton.addActionListener(e -> loadSongsData());

        JButton exportButton = GuiUtil.createButton("Export to CSV");
        exportButton.addActionListener(e -> exportTableToCSV(songsTable, "songs_export.csv"));

        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);

        songsPanel.add(actionPanel, "left");
    }

    private void createArtistsPanel() {
        artistsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Artists Management", Font.BOLD, 24);
        artistsPanel.add(headerLabel, "left");

        JLabel searchLabel = GuiUtil.createLabel("Search Artist:", Font.BOLD, 14);
        JTextField searchField = GuiUtil.createTextField(20);


        JPanel searchPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        artistsPanel.add(searchPanel, "growx, wrap");

        // Create artists table
        String[] artistColumns = {"ID", "Stage Name", "Bio", "Song Count", "Album Count", "Listener Count"};
        artistsTableModel = new DefaultTableModel(artistColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(artistsTableModel);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.isBlank()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
                }
            }
        });

        artistsTable = GuiUtil.createStyledTable(artistColumns, new int[]{50, 200, 300, 100, 100, 100});
        artistsTable.setModel(artistsTableModel);

        // Add table sorting
        artistsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(artistsTable);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        GuiUtil.applyModernScrollBar(scrollPane);

        artistsPanel.add(scrollPane, "grow");

        // Add action buttons panel
        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton refreshButton = GuiUtil.createButton("Refresh Data");
        refreshButton.addActionListener(e -> loadArtistsData());

        JButton exportButton = GuiUtil.createButton("Export to CSV");
        exportButton.addActionListener(e -> exportTableToCSV(artistsTable, "artists_export.csv"));

        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);

        artistsPanel.add(actionPanel, "left");
    }

    private void createAlbumsPanel() {
        albumsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Albums Management", Font.BOLD, 24);
        albumsPanel.add(headerLabel, "left");

        JLabel searchLabel = GuiUtil.createLabel("Search Album:", Font.BOLD, 14);
        JTextField searchField = GuiUtil.createTextField(20);


        JPanel searchPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        albumsPanel.add(searchPanel, "growx, wrap");

        // Create albums table
        String[] albumColumns = {"ID", "Title", "Artist", "Year", "Song Count", "Total Duration"};
        albumsTableModel = new DefaultTableModel(albumColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(albumsTableModel);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.isBlank()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
                }
            }
        });

        albumsTable = GuiUtil.createStyledTable(albumColumns, new int[]{50, 250, 200, 80, 100, 100});
        albumsTable.setModel(albumsTableModel);

        albumsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(albumsTable);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        GuiUtil.applyModernScrollBar(scrollPane);

        albumsPanel.add(scrollPane, "grow");

        // Add action buttons panel
        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton refreshButton = GuiUtil.createButton("Refresh Data");
        refreshButton.addActionListener(e -> loadAlbumsData());

        JButton exportButton = GuiUtil.createButton("Export to CSV");
        exportButton.addActionListener(e -> exportTableToCSV(albumsTable, "albums_export.csv"));

        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);

        albumsPanel.add(actionPanel, "left");
    }

    private void createUsersPanel() {
        usersPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]10[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Users Management", Font.BOLD, 24);
        usersPanel.add(headerLabel, "left");

        // Filters panel
        JPanel filtersPanel = GuiUtil.createPanel(new MigLayout("fillx", "[grow]10[][grow]10[][grow]10[]", "[]"));
        filtersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f), 1, true),
                "Registration Date Filter",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FontUtil.getSpotifyFont(Font.BOLD, 14),
                textColor));

        JLabel searchLabel = GuiUtil.createLabel("Search User:", Font.BOLD, 14);
        JTextField searchField = GuiUtil.createTextField(20);


        JPanel searchPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Date range pickers
        JLabel fromLabel = GuiUtil.createLabel("From (yyyy-MM-dd):", Font.BOLD, 14);
        fromDateTextField = GuiUtil.createTextField(20);
        fromDateTextField.setToolTipText("Enter date in format: yyyy-MM-dd");

        JLabel toLabel = GuiUtil.createLabel("To (yyyy-MM-dd):", Font.BOLD, 14);
        toDateTextField = GuiUtil.createTextField(20);
        toDateTextField.setToolTipText("Enter date in format: yyyy-MM-dd");

        JLabel roleLabel = GuiUtil.createLabel("Role:", Font.BOLD, 14);
        roleFilterComboBox = GuiUtil.createComboBox();
        roleFilterComboBox.addItem("All Roles");
        for (RoleType rt : RoleType.values()) {
            roleFilterComboBox.addItem(rt.getRoleCode());
        }
        filtersPanel.add(roleLabel, "right");
        filtersPanel.add(roleFilterComboBox);

        // Apply button
        JButton applyFilterButton = GuiUtil.createButton("Apply Filter");
        applyFilterButton.addActionListener(e -> applyUserDateFilter());

        // Add components to filters panel
        filtersPanel.add(searchPanel, "right");
        filtersPanel.add(fromLabel, "right");
        filtersPanel.add(fromDateTextField);
        filtersPanel.add(toLabel, "right");
        filtersPanel.add(toDateTextField);
        filtersPanel.add(applyFilterButton);

        usersPanel.add(filtersPanel, "growx");

        // Create users table
        String[] userColumns = {"ID", "Username", "Email", "Registration Date", "Last Login", "Role", "Status"};
        usersTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(usersTableModel);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.isBlank()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
                }
            }
        });


        usersTable = GuiUtil.createStyledTable(userColumns, new int[]{50, 150, 250, 150, 150, 100, 100});
        usersTable.setModel(usersTableModel);

        // Add table sorting
        usersTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        GuiUtil.applyModernScrollBar(scrollPane);

        usersPanel.add(scrollPane, "grow");

        // Add action buttons panel
        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton refreshButton = GuiUtil.createButton("Refresh Data");
        refreshButton.addActionListener(e -> loadUsersData());

        JButton exportButton = GuiUtil.createButton("Export to CSV");
        exportButton.addActionListener(e -> exportTableToCSV(usersTable, "users_export.csv"));

        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);

        usersPanel.add(actionPanel, "left");
    }

    private void createPlaylistsPanel() {
        playlistsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Playlists Management", Font.BOLD, 24);
        playlistsPanel.add(headerLabel, "left");

        JLabel searchLabel = GuiUtil.createLabel("Search Playlist:", Font.BOLD, 14);
        JTextField searchField = GuiUtil.createTextField(20);


        JPanel searchPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        playlistsPanel.add(searchPanel, "growx, wrap");

        // Create playlists table
        String[] playlistColumns = {"ID", "Name", "Creator", "Song Count", "Created Date", "Updated Date"};
        playlistsTableModel = new DefaultTableModel(playlistColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(playlistsTableModel);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                if (text.isBlank()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
                }
            }
        });


        playlistsTable = GuiUtil.createStyledTable(playlistColumns, new int[]{50, 250, 150, 100, 150, 150});
        playlistsTable.setModel(playlistsTableModel);

        // Add table sorting
        playlistsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(playlistsTable);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        GuiUtil.applyModernScrollBar(scrollPane);

        playlistsPanel.add(scrollPane, "grow");

        // Add action buttons panel
        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton refreshButton = GuiUtil.createButton("Refresh Data");
        refreshButton.addActionListener(e -> loadPlaylistsData());

        JButton exportButton = GuiUtil.createButton("Export to CSV");
        exportButton.addActionListener(e -> exportTableToCSV(playlistsTable, "playlists_export.csv"));

        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);

        playlistsPanel.add(actionPanel, "left");
    }

    private void createChartsPanel() {
        chartsPanel = GuiUtil.createPanel(new MigLayout("fillx, wrap", "[grow,fill]", "[]10[]10[]"));

        // Header
        JLabel headerLabel = GuiUtil.createLabel("Statistical Charts", Font.BOLD, 24);
        chartsPanel.add(headerLabel, "left");

        // Create chart containers
        JPanel topRowPanel = GuiUtil.createPanel(new GridLayout(1, 2, 20, 0));
        JPanel bottomRowPanel = GuiUtil.createPanel(new GridLayout(1, 2, 20, 0));

        // Create popular songs chart
        JPanel popularSongsPanel = createChartPanel("Most Played Songs", this::createPopularSongsChart);
        topRowPanel.add(popularSongsPanel);

        // Create popular tags chart
        JPanel popularTagsPanel = createChartPanel("Top Tags Distribution", this::createPopularTagsChart);
        topRowPanel.add(popularTagsPanel);

        // Create users registration chart
        JPanel usersRegistrationPanel = createChartPanel("User Registrations by Month", this::createUserRegistrationsChart);
        bottomRowPanel.add(usersRegistrationPanel);

        // Create songs by year chart
        JPanel songsByYearPanel = createChartPanel("Songs by Release Year", this::createSongsByYearChart);
        bottomRowPanel.add(songsByYearPanel);

        chartsPanel.add(topRowPanel, "grow");
        chartsPanel.add(bottomRowPanel, "grow");

        // Add refresh button
        JButton refreshButton = GuiUtil.createButton("Refresh All Charts");
        refreshButton.addActionListener(e -> refreshAllCharts());

        JPanel actionPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(refreshButton);

        chartsPanel.add(actionPanel);
    }

    private JPanel createChartPanel(String title, Consumer<JPanel> chartCreator) {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = GuiUtil.createLabel(title, Font.BOLD, 16);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Content panel where the chart will be drawn
        JPanel contentPanel = GuiUtil.createPanel(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(0, 300));

        // Add loading indicator initially
        JLabel loadingLabel = GuiUtil.createLabel("Loading chart data...", Font.ITALIC, 14);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(loadingLabel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Schedule chart creation (we'll replace the loading label with the actual chart)
        SwingUtilities.invokeLater(() -> chartCreator.accept(contentPanel));

        return panel;
    }

    private void createPopularSongsChart(JPanel container) {
        try {
            // Clear the container
            container.removeAll();

            // Create dataset for top 10 most played songs
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Fetch data using CommonApiUtil
            List<SongDTO> topSongs = CommonApiUtil.findTopSongByPlayCount(10);
            if (topSongs != null && !topSongs.isEmpty()) {
                for (SongDTO song : topSongs) {
                    dataset.addValue(song.getPlayCount(), "Plays", StringUtils.getTruncatedText(song.getTitle(), 15));
                }

                // Create the chart
                JFreeChart chart = GuiUtil.createBarChart(
                        "", "Song", "Play Count", dataset,
                        PlotOrientation.HORIZONTAL);

                // Create chart panel
                ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setOpaque(false);
                chartPanel.setPreferredSize(new Dimension(400, 300));

                container.add(chartPanel, BorderLayout.CENTER);
            } else {
                // No data available
                JLabel noDataLabel = GuiUtil.createLabel("No song play data available", Font.ITALIC, 14);
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                container.add(noDataLabel, BorderLayout.CENTER);
            }

            container.revalidate();
            container.repaint();
        } catch (Exception e) {
            log.error("Error creating popular songs chart", e);
            container.removeAll();
            container.add(GuiUtil.createErrorLabel("Failed to load chart: " + e.getMessage()), BorderLayout.CENTER);
            container.revalidate();
            container.repaint();
        }
    }

    private void createPopularTagsChart(JPanel container) {
        try {
            // Clear the container
            container.removeAll();

            // Create dataset for top tags
            DefaultPieDataset dataset = new DefaultPieDataset();

            // Fetch tag data using CommonApiUtil
            Map<String, Integer> tagCounts = CommonApiUtil.fetchTopTags(10);
            if (tagCounts != null && !tagCounts.isEmpty()) {
                for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                    dataset.setValue(entry.getKey(), entry.getValue());
                }

                // Create the pie chart
                JFreeChart chart = GuiUtil.createPieChart("", dataset);

                // Create chart panel
                ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setOpaque(false);
                chartPanel.setPreferredSize(new Dimension(400, 300));

                container.add(chartPanel, BorderLayout.CENTER);
            } else {
                // No data available
                JLabel noDataLabel = GuiUtil.createLabel("No tag data available", Font.ITALIC, 14);
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                container.add(noDataLabel, BorderLayout.CENTER);
            }

            container.revalidate();
            container.repaint();
        } catch (Exception e) {
            log.error("Error creating popular tags chart", e);
            container.removeAll();
            container.add(GuiUtil.createErrorLabel("Failed to load chart: " + e.getMessage()), BorderLayout.CENTER);
            container.revalidate();
            container.repaint();
        }
    }

    private void createUserRegistrationsChart(JPanel container) {
        try {
            // Clear the container
            container.removeAll();

            // Create dataset for user registrations by month
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Fetch registration data using CommonApiUtil
            Map<String, Integer> registrationsByMonth = fetchUserRegistrationsByMonth();
            if (registrationsByMonth != null && !registrationsByMonth.isEmpty()) {
                for (Map.Entry<String, Integer> entry : registrationsByMonth.entrySet()) {
                    dataset.addValue(entry.getValue(), "Registrations", entry.getKey());
                }

                // Create the line chart
                JFreeChart chart = GuiUtil.createLineChart(
                        "", "Month", "New Users", dataset,
                        PlotOrientation.VERTICAL);

                // Create chart panel
                ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setOpaque(false);
                chartPanel.setPreferredSize(new Dimension(400, 300));

                container.add(chartPanel, BorderLayout.CENTER);
            } else {
                // No data available
                JLabel noDataLabel = GuiUtil.createLabel("No registration data available", Font.ITALIC, 14);
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                container.add(noDataLabel, BorderLayout.CENTER);
            }

            container.revalidate();
            container.repaint();
        } catch (Exception e) {
            log.error("Error creating user registrations chart", e);
            container.removeAll();
            container.add(GuiUtil.createErrorLabel("Failed to load chart: " + e.getMessage()), BorderLayout.CENTER);
            container.revalidate();
            container.repaint();
        }
    }

    private void createSongsByYearChart(JPanel container) {
        try {
            // Clear the container
            container.removeAll();

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            Map<Integer, Integer> songsByYear = fetchSongCountByYear();
            if (songsByYear != null && !songsByYear.isEmpty()) {
                List<Map.Entry<Integer, Integer>> sortedEntries = songsByYear.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toList());

                sortedEntries.forEach(entry ->
                        dataset.addValue(entry.getValue(), "Songs", entry.getKey().toString()));

                // Create the bar chart
                JFreeChart chart = GuiUtil.createBarChart(
                        "", "Year", "Number of Songs", dataset,
                        PlotOrientation.VERTICAL);

                CategoryPlot plot = chart.getCategoryPlot();
                CategoryAxis domainAxis = plot.getDomainAxis();

                domainAxis.setCategoryLabelPositions(
                        CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

                domainAxis.setTickLabelFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 10));

                if (songsByYear.size() > 15) {
                    domainAxis.setCategoryLabelPositions(
                            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));

                    BarRenderer renderer = (BarRenderer) plot.getRenderer();
                    renderer.setItemMargin(0.05);
                }

                // Create chart panel
                ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setOpaque(false);
                chartPanel.setPreferredSize(new Dimension(400, 300));

                container.add(chartPanel, BorderLayout.CENTER);
            } else {
                // No data available
                JLabel noDataLabel = GuiUtil.createLabel("No song year data available", Font.ITALIC, 14);
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                container.add(noDataLabel, BorderLayout.CENTER);
            }

            container.revalidate();
            container.repaint();
        } catch (Exception e) {
            log.error("Error creating songs by year chart", e);
            container.removeAll();
            container.add(GuiUtil.createErrorLabel("Failed to load chart: " + e.getMessage()), BorderLayout.CENTER);
            container.revalidate();
            container.repaint();
        }
    }

    private void loadInitialData() {
        // Load all data
        loadSummaryData();
        loadSongsData();
        loadArtistsData();
        loadAlbumsData();
        loadUsersData();
        loadPlaylistsData();
        loadRecentUsers();

        // Load filter options
        loadFilterOptions();
    }

    private void loadSummaryData() {
        SwingWorker<Map<String, Integer>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Integer> doInBackground() {
                Map<String, Integer> counts = new HashMap<>();
                try {
                    counts.put("songs", CommonApiUtil.fetchAllSongs().size());
                    counts.put("artists", CommonApiUtil.fetchAllArtists().size());
                    counts.put("albums", CommonApiUtil.fetchAllAlbums().size());
                    counts.put("users", CommonApiUtil.fetchAllUsers().size());
                    counts.put("playlists", CommonApiUtil.fetchAllPlaylists().size());
                    counts.put("tags", CommonApiUtil.fetchAllTags().size());
                } catch (Exception e) {
                    log.error("Error loading summary data", e);
                }
                return counts;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> counts = get();

                    // Update summary labels with formatting for large numbers
                    songCountLabel.setText(NumberFormatUtil.formatWithCommas(counts.get("songs")));
                    artistCountLabel.setText(NumberFormatUtil.formatWithCommas(counts.get("artists")));
                    albumCountLabel.setText(NumberFormatUtil.formatWithCommas(counts.get("albums")));
                    userCountLabel.setText(NumberFormatUtil.formatWithCommas(counts.get("users")));
                    playlistCountLabel.setText(NumberFormatUtil.formatWithCommas(counts.get("playlists")));
                    tagCountLabel.setText(NumberFormatUtil.formatWithCommas(counts.get("tags")));

                } catch (Exception e) {
                    log.error("Error setting summary data", e);
                }
            }
        };

        worker.execute();
    }

    private void loadSongsData() {
        SwingWorker<List<SongDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SongDTO> doInBackground() {
                try {
                    // Get filter values
                    String yearFilter = yearFilterComboBox.getSelectedItem().toString();
                    String genreFilter = genreFilterComboBox.getSelectedItem().toString();
                    String artistFilter = artistFilterComboBox.getSelectedItem().toString();

                    // Apply filters if not "All"
                    if ("All Years".equals(yearFilter) && "All Genres".equals(genreFilter) && "All Artists".equals(artistFilter)) {
                        return CommonApiUtil.fetchAllSongs(); // No filter
                    } else {
                        Integer year = "All Years".equals(yearFilter) ? null : Integer.parseInt(yearFilter);
                        String genre = "All Genres".equals(genreFilter) ? null : genreFilter;
                        Long artistId = null;
                        if (!"All Artists".equals(artistFilter)) {
                            artistId = CommonApiUtil.searchArtists(artistFilter).stream()
                                    .filter(a -> a.getStageName().equals(artistFilter))
                                    .findFirst()
                                    .map(ArtistDTO::getId)
                                    .orElse(null);
                        }

                        return CommonApiUtil.findSongsByFilter(year, genre, artistId);
                    }
                } catch (Exception e) {
                    log.error("Error loading songs data", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<SongDTO> songs = get();

                    // Clear existing data
                    songsTableModel.setRowCount(0);

                    if (songs != null) {
                        // Add data to table
                        for (SongDTO song : songs) {
                            songsTableModel.addRow(new Object[]{
                                    song.getId(),
                                    song.getTitle(),
                                    song.getSongArtist(),
                                    song.getSongAlbum(),
                                    song.getReleaseYear(),
                                    StringUtils.formatMinSecDuration(song.getDuration()),
                                    NumberFormatUtil.formatWithCommas(song.getPlayCount())
                            });
                        }
                    }
                } catch (Exception e) {
                    log.error("Error setting songs data", e);
                }
            }
        };

        worker.execute();
    }

    private void loadArtistsData() {
        SwingWorker<List<ArtistDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ArtistDTO> doInBackground() {
                try {
                    return CommonApiUtil.fetchAllArtists();
                } catch (Exception e) {
                    log.error("Error loading artists data", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<ArtistDTO> artists = get();

                    // Clear existing data
                    artistsTableModel.setRowCount(0);

                    if (artists != null) {
                        // Add data to table
                        for (ArtistDTO artist : artists) {
                            // Truncate bio text to prevent overly long display
                            String truncatedBio = artist.getBio() != null ?
                                    StringUtils.getTruncatedText(artist.getBio(), 100) : "";

                            artistsTableModel.addRow(new Object[]{
                                    artist.getId(),
                                    artist.getStageName(),
                                    truncatedBio,
                                    artist.getSongCount() != null ? artist.getSongCount() : 0,
                                    artist.getAlbumCount() != null ? artist.getAlbumCount() : 0,
                                    NumberFormatUtil.formatWithCommas(artist.getFollowerCount())
                            });
                        }
                    }
                } catch (Exception e) {
                    log.error("Error setting artists data", e);
                }
            }
        };

        worker.execute();
    }

    private void loadAlbumsData() {
        SwingWorker<List<AlbumDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AlbumDTO> doInBackground() {
                try {
                    return CommonApiUtil.fetchAllAlbums();
                } catch (Exception e) {
                    log.error("Error loading albums data", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<AlbumDTO> albums = get();

                    // Clear existing data
                    albumsTableModel.setRowCount(0);

                    if (albums != null) {
                        // Add data to table
                        for (AlbumDTO album : albums) {
                            albumsTableModel.addRow(new Object[]{
                                    album.getId(),
                                    album.getTitle(),
                                    album.getArtistName(),
                                    album.getReleaseYear(),
                                    album.getSongDTOS() != null ? album.getSongDTOS().size() : 0,
                                    StringUtils.formatMinSecDuration(album.getTotalDuration())
                            });
                        }
                    }
                } catch (Exception e) {
                    log.error("Error setting albums data", e);
                }
            }
        };

        worker.execute();
    }

    private void loadUsersData() {
        SwingWorker<List<UserDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserDTO> doInBackground() {
                try {
                    Date fromDate = null;
                    Date toDate = null;

                    // Parse date from text fields
                    String fromDateText = fromDateTextField.getText().trim();
                    String toDateText = toDateTextField.getText().trim();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false);

                    if (!fromDateText.isEmpty()) {
                        try {
                            fromDate = dateFormat.parse(fromDateText);
                        } catch (Exception e) {
                            log.error("Invalid from date format: {}", fromDateText, e);
                            SwingUtilities.invokeLater(() -> GuiUtil.showToast(AdminStatisticsPanel.this,
                                    "Invalid date format. Use yyyy-MM-dd"));
                        }
                    }

                    if (!toDateText.isEmpty()) {
                        try {
                            toDate = dateFormat.parse(toDateText);
                            Calendar c = Calendar.getInstance();
                            c.setTime(toDate);
                            c.add(Calendar.DATE, 1);
                            toDate = c.getTime();
                        } catch (Exception e) {
                            log.error("Invalid to date format: {}", toDateText, e);
                            SwingUtilities.invokeLater(() -> GuiUtil.showToast(AdminStatisticsPanel.this,
                                    "Invalid date format. Use yyyy-MM-dd"));
                        }
                    }

                    return CommonApiUtil.fetchUsersByFilter(fromDate, toDate, "All Roles".equals(roleFilterComboBox.getSelectedItem().toString()) ? null : roleFilterComboBox.getSelectedItem().toString());

                } catch (Exception e) {
                    log.error("Error loading users data", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<UserDTO> users = get();

                    // Clear existing data
                    usersTableModel.setRowCount(0);

                    if (users != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                        // Add data to table
                        for (UserDTO user : users) {
                            String createdAt = user.getCreatedDate() != null ?
                                    dateFormat.format(user.getCreatedDate()) : "";
                            String lastLogin = user.getLastLoginAt() != null ?
                                    dateFormat.format(user.getLastLoginAt()) : "";

                            usersTableModel.addRow(new Object[]{
                                    user.getId(),
                                    user.getUsername(),
                                    user.getEmail(),
                                    createdAt,
                                    lastLogin,
                                    user.determineUserRole(),
                                    user.getAccountStatus()
                            });
                        }
                    }
                } catch (Exception e) {
                    log.error("Error setting users data", e);
                }
            }
        };

        worker.execute();
    }


    private void loadRecentUsers() {
        SwingWorker<List<UserDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserDTO> doInBackground() {
                try {
                    List<UserDTO> allUsers = CommonApiUtil.fetchAllUsers();

                    allUsers.sort((u1, u2) -> {
                        if (u1.getCreatedDate() == null) return 1;
                        if (u2.getCreatedDate() == null) return -1;
                        return u2.getCreatedDate().compareTo(u1.getCreatedDate());
                    });

                    // Return top 10 or less
                    return allUsers.stream()
                            .limit(20)
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.error("Error loading recent users", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<UserDTO> recentUsers = get();
                    // Clear existing data
                    recentUserModel.setRowCount(0);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    // Add recent users
                    if (recentUsers != null) {
                        for (UserDTO user : recentUsers) {
                            String createdAt = user.getCreatedDate() != null ?
                                    dateFormat.format(user.getCreatedDate()) : "";

                            recentUserModel.addRow(new Object[]{
                                    user.getId(),
                                    user.getUsername(),
                                    user.getEmail(),
                                    createdAt
                            });
                        }
                    }
                } catch (Exception e) {
                    log.error("Error displaying recent users", e);
                }
            }
        };

        worker.execute();
    }

    private void loadPlaylistsData() {
        SwingWorker<List<PlaylistDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PlaylistDTO> doInBackground() {
                try {
                    return CommonApiUtil.fetchAllPlaylists();
                } catch (Exception e) {
                    log.error("Error loading playlists data", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<PlaylistDTO> playlists = get();

                    // Clear existing data
                    playlistsTableModel.setRowCount(0);

                    if (playlists != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                        // Add data to table
                        for (PlaylistDTO playlist : playlists) {
                            String createdAt = playlist.getCreatedDate() != null ?
                                    dateFormat.format(playlist.getCreatedDate()) : "";
                            String updatedAt = playlist.getUpdateDate() != null ?
                                    dateFormat.format(playlist.getUpdateDate()) : "";

                            playlistsTableModel.addRow(new Object[]{
                                    playlist.getId(),
                                    playlist.getName(),
                                    playlist.getCreatedBy(),
                                    playlist.getSongs() != null ? playlist.getSongs().size() : 0,
                                    createdAt,
                                    updatedAt
                            });
                        }
                    }
                } catch (Exception e) {
                    log.error("Error setting playlists data", e);
                }
            }
        };

        worker.execute();
    }

    private void loadFilterOptions() {
        // Load genre options
        SwingWorker<List<String>, Void> genreWorker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                try {
                    return CommonApiUtil.fetchAllTags().stream().map(TagDTO::getName).collect(Collectors.toList());
                } catch (Exception e) {
                    log.error("Error loading genre filter options", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<String> genres = get();

                    if (genres != null) {
                        for (String genre : genres) {
                            genreFilterComboBox.addItem(genre);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error setting genre filter options", e);
                }
            }
        };

        // Load artist options
        SwingWorker<List<ArtistDTO>, Void> artistWorker = new SwingWorker<>() {
            @Override
            protected List<ArtistDTO> doInBackground() {
                try {
                    return CommonApiUtil.fetchAllArtists();
                } catch (Exception e) {
                    log.error("Error loading artist filter options", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<ArtistDTO> artists = get();

                    if (artists != null) {
                        for (ArtistDTO artist : artists) {
                            artistFilterComboBox.addItem(artist.getStageName());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error setting artist filter options", e);
                }
            }
        };

        genreWorker.execute();
        artistWorker.execute();
    }

    private void applySongsFilters() {
        loadSongsData();
    }

    private void applyUserDateFilter() {
        loadUsersData();
    }

    private void refreshAllCharts() {
        // Get all chart containers from the charts panel
        if (chartsPanel != null) {
            // Find all panels that contain charts
            Component[] topRowComponents = ((Container) chartsPanel.getComponent(1)).getComponents();
            Component[] bottomRowComponents = ((Container) chartsPanel.getComponent(2)).getComponents();

            // For each chart panel, find the content panel and refresh the chart
            for (Component comp : topRowComponents) {
                if (comp instanceof JPanel) {
                    // The content panel is the second component (index 1) in the chart panel
                    Component contentPanel = ((Container) comp).getComponent(1);
                    if (contentPanel instanceof JPanel) {
                        // Find which chart this is and refresh it
                        String title = ((JLabel) ((Container) comp).getComponent(0)).getText();
                        refreshChart(title, (JPanel) contentPanel);
                    }
                }
            }

            for (Component comp : bottomRowComponents) {
                if (comp instanceof JPanel) {
                    Component contentPanel = ((Container) comp).getComponent(1);
                    if (contentPanel instanceof JPanel) {
                        String title = ((JLabel) ((Container) comp).getComponent(0)).getText();
                        refreshChart(title, (JPanel) contentPanel);
                    }
                }
            }
        }
    }

    private void refreshChart(String title, JPanel container) {
        switch (title) {
            case "Most Played Songs" -> createPopularSongsChart(container);
            case "Top Tags Distribution" -> createPopularTagsChart(container);
            case "User Registrations by Month" -> createUserRegistrationsChart(container);
            case "Songs by Release Year" -> createSongsByYearChart(container);
        }
    }

    private void exportTableToCSV(JTable table, String filename) {
        try {
            // Create file chooser for selecting save location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CSV File");
            fileChooser.setSelectedFile(new File(filename));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Ensure filename ends with .csv
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }

                CSVExporter.exportTableToCSV(table, file);
                GuiUtil.showSuccessMessageDialog(this, "Data exported successfully to " + file.getName());
            }
        } catch (Exception e) {
            log.error("Error exporting table to CSV", e);
            GuiUtil.showErrorMessageDialog(this, "Error exporting to CSV: " + e.getMessage());
        }
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        // Update navigation button states
        updateNavigationButtonStates(currentCard);

        // Repaint the entire panel
        repaint();
    }


    public Map<Integer, Integer> fetchSongCountByYear() {
        try {
            List<SongDTO> allSongs = CommonApiUtil.fetchAllSongs();

            Map<Integer, Integer> songsByYear = new TreeMap<>();

            if (allSongs != null && !allSongs.isEmpty()) {
                Map<Integer, List<SongDTO>> songsGroupedByYear = allSongs.stream()
                        .filter(song -> song.getReleaseYear() != null)
                        .collect(Collectors.groupingBy(
                                SongDTO::getReleaseYear,
                                Collectors.toList()
                        ));

                songsGroupedByYear.forEach((year, songs) -> {
                    songsByYear.put(year, songs.size());
                });

                if (songsByYear.isEmpty()) {
                    songsByYear.put(Calendar.getInstance().get(Calendar.YEAR), 0);
                }
            }

            return songsByYear;
        } catch (Exception e) {
            return Map.of(Calendar.getInstance().get(Calendar.YEAR), 0);
        }
    }

    public Map<String, Integer> fetchUserRegistrationsByMonth() {
        try {
            List<UserDTO> allUsers = CommonApiUtil.fetchAllUsers();

            Map<String, Integer> registrationsByMonth = new LinkedHashMap<>();

            if (allUsers != null && !allUsers.isEmpty()) {
                // SimpleDateFormat with explicit English locale
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);

                // Group users by month of registration
                Map<String, List<UserDTO>> usersByMonth = allUsers.stream()
                        .filter(user -> user.getCreatedDate() != null)
                        .collect(Collectors.groupingBy(
                                user -> monthFormat.format(user.getCreatedDate()),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

                List<String> sortedMonths = new ArrayList<>(usersByMonth.keySet());
                Collections.sort(sortedMonths, (m1, m2) -> {
                    try {
                        // Use the same English locale here
                        Date date1 = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH).parse(m1);
                        Date date2 = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH).parse(m2);
                        return date1.compareTo(date2);
                    } catch (Exception e) {
                        return 0;
                    }
                });

                for (String month : sortedMonths) {
                    registrationsByMonth.put(month, usersByMonth.get(month).size());
                }
            }

            return registrationsByMonth;
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }
}