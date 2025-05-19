package com.javaweb.view.panel;

import com.javaweb.App;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.StringUtils;
import com.javaweb.view.components.AsyncImageLabel;
import com.javaweb.view.event.MusicPlayerFacade;
import com.javaweb.view.event.PlayerEvent;
import com.javaweb.view.event.PlayerEventListener;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class QueuePanel extends JPanel implements ThemeChangeListener, PlayerEventListener {
    private final MusicPlayerFacade playerFacade;

    // UI components
    private JPanel headerPanel;
    private JPanel nowPlayingPanel;
    private JPanel queuePanel;
    private JPanel upNextFromArtistPanel;
    private JScrollPane scrollPane;
    private JButton clearQueueButton;
    private JPanel queueContentPanel;
    private JPanel artistContentPanel;
    private JLabel artistHeaderLabel;

    // Currently playing song components
    private AsyncImageLabel currentSongImage;
    private JLabel currentSongTitle;
    private JLabel currentSongArtist;

    // Theme colors
    private Color backgroundColor;
    private Color textColor;
    private Color accentColor;

    public QueuePanel() {
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
        // Create main content panel with scroll
        JPanel contentPanel = createMainContentPanel();
        scrollPane = GuiUtil.createStyledScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMainContentPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, insets 30 30 30 30",
                "[grow, fill]",
                "[]15[]15[]15[]"
        ));

        // Create header section
        headerPanel = createHeaderPanel();
        panel.add(headerPanel, "growx");

        // Create now playing section
        nowPlayingPanel = createNowPlayingPanel();
        panel.add(nowPlayingPanel, "growx");

        // Create next in queue section
        queuePanel = createQueuePanel();
        panel.add(queuePanel, "growx");

        // Create next from artist section
        upNextFromArtistPanel = createUpNextFromArtistPanel();
        panel.add(upNextFromArtistPanel, "growx");

        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = GuiUtil.createPanel(new BorderLayout());

        JLabel titleLabel = GuiUtil.createLabel("Queue", Font.BOLD, 24);
        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createNowPlayingPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, gap 0",
                "[grow]",
                "[]10[]"
        ));

        // Section header
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel sectionLabel = GuiUtil.createLabel("Now playing", Font.BOLD, 16);
        headerPanel.add(sectionLabel, BorderLayout.WEST);

        panel.add(headerPanel, "growx");

        // Current song panel
        JPanel songPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, gap 15",
                "120![grow, fill]",
                "120!"
        ));

        // Song image
        currentSongImage = GuiUtil.createAsyncImageLabel(120, 120, 15);
        currentSongImage.startLoading();

        // Song info panel
        JPanel songInfoPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, gap 0",
                "[grow]",
                "[]5[]"
        ));

        currentSongTitle = GuiUtil.createLabel("No song playing", Font.BOLD, 22);
        currentSongArtist = GuiUtil.createLabel("", Font.PLAIN, 18);

        songInfoPanel.add(currentSongTitle);
        songInfoPanel.add(currentSongArtist);

        songPanel.add(currentSongImage);
        songPanel.add(songInfoPanel);

        panel.add(songPanel);

        return panel;
    }

    private JPanel createQueuePanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, gap 0",
                "[grow]",
                "[]10[]"
        ));

        // Section header with clear queue button
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel sectionLabel = GuiUtil.createLabel("Next in queue", Font.BOLD, 16);

        clearQueueButton = GuiUtil.createButton("Clear queue");
        clearQueueButton.addActionListener(e -> handleClearQueueButtonClick());

        headerPanel.add(sectionLabel, BorderLayout.WEST);
        headerPanel.add(clearQueueButton, BorderLayout.EAST);

        panel.add(headerPanel, "growx");

        // Create and store reference to content panel
        queueContentPanel = GuiUtil.createPanel();
        queueContentPanel.setLayout(new BoxLayout(queueContentPanel, BoxLayout.Y_AXIS));

        panel.add(queueContentPanel, "growx");

        return panel;
    }

    private JPanel createUpNextFromArtistPanel() {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, gap 0",
                "[grow]",
                "[]10[]"
        ));

        // Section header
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel sectionLabel = GuiUtil.createLabel("Next from: ", Font.BOLD, 16);
        artistHeaderLabel = sectionLabel; // Add this field to your class
        headerPanel.add(sectionLabel, BorderLayout.WEST);

        panel.add(headerPanel, "growx");

        // Create and store reference to content panel
        artistContentPanel = new JPanel();
        artistContentPanel.setLayout(new BoxLayout(artistContentPanel, BoxLayout.Y_AXIS));
        artistContentPanel.setOpaque(false);

        panel.add(artistContentPanel, "growx");

        return panel;
    }

    private JPanel createSongRow(SongDTO song, boolean isQueueItem, int position) {
        JPanel panel = GuiUtil.createPanel(new MigLayout(
                "fillx, gap 15",
                "120![grow, fill]",
                "120!"
        ));

        // Song image
        AsyncImageLabel songImage = GuiUtil.createAsyncImageLabel(120, 120, 15);
        songImage.startLoading();
        playerFacade.populateSongImage(song, songImage::setLoadedImage);

        // Song info panel
        JPanel songInfoPanel = GuiUtil.createPanel(new MigLayout(
                "fillx, wrap 1, gap 0",
                "[grow]push[]",
                "[]5[]"
        ));

        Font titleFont = FontUtil.getSpotifyFont(Font.BOLD, 22);
        JLabel titleLabel = GuiUtil.createLabel(
                StringUtils.getTruncatedTextByWidth(song.getTitle(), titleFont, 200),
                Font.BOLD, 22);
        titleLabel.setToolTipText(song.getTitle());

        String artistName = song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist";
        JLabel artistLabel = GuiUtil.createLabel(artistName, Font.PLAIN, 18);

        songInfoPanel.add(titleLabel);
        songInfoPanel.add(artistLabel);

        // For queue items, add position number or play button
        if (isQueueItem) {
            JLabel positionLabel = GuiUtil.createLabel(String.valueOf(position), Font.PLAIN, 16);

            JPanel positionPanel = GuiUtil.createPanel(new BorderLayout());
            positionPanel.add(positionLabel, BorderLayout.EAST);

            songInfoPanel.add(positionPanel, "east");
        }

        panel.add(songImage);
        panel.add(songInfoPanel);

        // Add hover effect
        GuiUtil.addHoverEffect(panel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (isQueueItem) {
                        playerFacade.playQueueFrom(position);
                        updateQueueView();
                    } else {
                        playerFacade.addToQueueNext(song);
                        updateQueueView();
                        GuiUtil.showToast(QueuePanel.this, "Added to play next");
                    }
                }
            }
        });

        return panel;
    }

    private void handleClearQueueButtonClick() {
        int result = GuiUtil.showConfirmMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Clear your queue?",
                "Confirm"
        );

        if (result == JOptionPane.YES_OPTION) {
            playerFacade.clearQueue();
            updateQueueView();
            GuiUtil.showToast(this, "Queue cleared");
        }
    }

    public void updateQueueView() {
        // Update current song
        SongDTO currentSong = playerFacade.getCurrentSong();
        if (currentSong != null) {
            currentSongTitle.setText(currentSong.getTitle());
            currentSongArtist.setText(currentSong.getSongArtist() != null ? currentSong.getSongArtist() : "Unknown Artist");
            playerFacade.populateSongImage(currentSong, currentSongImage::setLoadedImage);
        } else {
            currentSongTitle.setText("No song playing");
            currentSongArtist.setText("");
        }

        queueContentPanel.removeAll();

        List<SongDTO> queueSongs = playerFacade.getQueueSongs();

        if (queueSongs.isEmpty() || queueSongs.size() == 1) {
            JLabel emptyLabel = GuiUtil.createLabel("Your queue is empty", Font.ITALIC, 14);
            emptyLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            queueContentPanel.add(emptyLabel);
            clearQueueButton.setVisible(false);
        } else {
            clearQueueButton.setVisible(true);

            for (int i = 1; i < queueSongs.size(); i++) {
                JPanel songRow = createSongRow(queueSongs.get(i), true, i);
                songRow.setBorder(new EmptyBorder(0, 0, 10, 0));
                queueContentPanel.add(songRow);
            }
        }

        // Update next from artist
        updateUpNextFromArtistPanel(currentSong);

        // Refresh UI
        revalidate();
        repaint();
    }

    private void updateUpNextFromArtistPanel(SongDTO currentSong) {
        artistContentPanel.removeAll();

        if (currentSong == null || currentSong.getSongArtist() == null) {
            artistHeaderLabel.setText("Next from: -");
            JLabel emptyLabel = GuiUtil.createLabel("No artist recommendations available", Font.ITALIC, 14);
            emptyLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            artistContentPanel.add(emptyLabel);
            return;
        }

        artistHeaderLabel.setText("Next from: " + currentSong.getSongArtist());

        List<SongDTO> artistSongs = playerFacade.getSongsByArtist(currentSong.getSongArtist(), 10);

        List<SongDTO> queueSongs = playerFacade.getQueueSongs();
        List<SongDTO> filteredArtistSongs = artistSongs.stream()
                .filter(song -> !isSongInList(song, queueSongs))
                .collect(Collectors.toList());

        if (filteredArtistSongs.isEmpty()) {
            JLabel emptyLabel = GuiUtil.createLabel("No more songs from this artist", Font.ITALIC, 14);
            emptyLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            artistContentPanel.add(emptyLabel);
        } else {
            for (SongDTO song : filteredArtistSongs) {
                JPanel songRow = createSongRow(song, false, 0);
                songRow.setBorder(new EmptyBorder(0, 0, 10, 0));
                artistContentPanel.add(songRow);
            }
        }


        artistContentPanel.revalidate();
        artistContentPanel.repaint();
    }

    private boolean isSongInList(SongDTO song, List<SongDTO> songList) {
        return songList.stream().anyMatch(s -> isSameSong(s, song));
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

        // Update all colors
        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);

        // Update clear queue button
        clearQueueButton.setForeground(accentColor);

        repaint();
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {
        switch (event.type()) {
            case QUEUE_UPDATED -> {
                updateQueueView();
            }
        }
    }

    public void cleanup() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
        playerFacade.unsubscribeFromPlayerEvents(this);
    }
}