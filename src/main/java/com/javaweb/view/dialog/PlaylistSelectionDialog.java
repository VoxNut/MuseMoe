package com.javaweb.view.dialog;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.CommonApiUtil;
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
import java.util.Collections;
import java.util.List;

@Slf4j
public class PlaylistSelectionDialog extends JDialog implements ThemeChangeListener {

    private final SongDTO song;
    private final MusicPlayerFacade playerFacade;
    private Color textColor = ThemeManager.getInstance().getTextColor();
    private Color backgroundColor = ThemeManager.getInstance().getBackgroundColor();
    private Color accentColor = ThemeManager.getInstance().getAccentColor();

    public PlaylistSelectionDialog(Component parent, SongDTO song) {
        super((Window) parent, "Add to Playlist", Dialog.ModalityType.APPLICATION_MODAL);
        this.song = song;
        this.playerFacade = com.javaweb.App.getBean(MusicPlayerFacade.class);

        ThemeManager.getInstance().addThemeChangeListener(this);
        GuiUtil.styleTitleBar(this, backgroundColor, textColor);

        initComponents();
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.accentColor = accentColor;

        GuiUtil.styleTitleBar(this, GuiUtil.darkenColor(backgroundColor, 0.1), textColor);


        GuiUtil.updatePanelColors(getContentPane(), backgroundColor, textColor, accentColor);

        repaint();
    }

    private void initComponents() {
        // Set dialog background color
        getContentPane().setBackground(ThemeManager.getInstance().getBackgroundColor());

        // Main layout
        setLayout(new BorderLayout(10, 10));

        // Header panel
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        JLabel titleLabel = GuiUtil.createLabel("Select a playlist", Font.BOLD, 18);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton createPlaylistButton = GuiUtil.createButton("Create New");
        createPlaylistButton.addActionListener(e -> createNewPlaylist());
        headerPanel.add(createPlaylistButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Playlists panel
        JPanel playlistsPanel = GuiUtil.createPanel();
        playlistsPanel.setLayout(new MigLayout("fillx, wrap 1", "[fill]", "[]5[]"));

        // Get user playlists
        List<PlaylistDTO> playlists = CommonApiUtil.fetchPlaylistByUserId();

        if (playlists.isEmpty()) {
            JLabel emptyLabel = GuiUtil.createLabel("You don't have any playlists yet", Font.ITALIC, 14);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            playlistsPanel.add(emptyLabel, "alignx center");
        } else {
            for (PlaylistDTO playlist : playlists) {
                playlistsPanel.add(createPlaylistItem(playlist), "growx");
            }
        }

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(playlistsPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with cancel button
        JPanel bottomPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = GuiUtil.createButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        bottomPanel.add(cancelButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlaylistItem(PlaylistDTO playlist) {
        JPanel panel = GuiUtil.createPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Playlist cover
        AsyncImageLabel coverLabel = new AsyncImageLabel(40, 40, 15);
        coverLabel.startLoading();

        if (!playlist.getSongs().isEmpty()) {
            playerFacade.populateSongImage(playlist.getFirstSong(), coverLabel::setLoadedImage);
        } else {
            coverLabel.setLoadedImage(GuiUtil.createBufferImage(AppConstant.DEFAULT_COVER_PATH));
        }

        // Playlist info
        JPanel infoPanel = GuiUtil.createPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = GuiUtil.createLabel(playlist.getName(), Font.BOLD, 14);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel songsLabel = GuiUtil.createLabel(playlist.getSongs().size() + " songs", Font.PLAIN, 12);
        songsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(songsLabel);

        panel.add(coverLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);

        // Hover effect
        GuiUtil.addHoverEffect(panel);

        // Click handler
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                playlist.setSongIds(Collections.singletonList(song.getId()));
                boolean success = CommonApiUtil.addSongToPlaylist(playlist);
                if (success) {
                    GuiUtil.showToast(PlaylistSelectionDialog.this,
                            "Added to " + playlist.getName());
                    dispose();
                } else {
                    GuiUtil.showToast(PlaylistSelectionDialog.this,
                            "Failed to add song to playlist");
                }
            }
        });

        return panel;
    }

    private void createNewPlaylist() {
        String name = GuiUtil.showStyledInputDialog(this, "Enter playlist name:", "Create Playlist", null);

        if (name != null && !name.trim().isEmpty()) {
            try {
                PlaylistDTO newPlaylist = CommonApiUtil.createPlaylist(name.trim(), song.getId());
                if (newPlaylist != null) {
                    GuiUtil.showToast(this, "Created playlist and added song");
                    dispose();
                } else {
                    GuiUtil.showToast(this, "Failed to create playlist");
                }
            } catch (Exception e) {
                log.error("Error creating playlist", e);
                GuiUtil.showErrorMessageDialog(this, "Error creating playlist");
            }
        }
    }

    @Override
    public void dispose() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
        super.dispose();
    }
}