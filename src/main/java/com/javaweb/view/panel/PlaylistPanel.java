package com.javaweb.view.panel;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PlaylistPanel extends ListThemeablePanel {
    private final JList<SongDTO> songList;
    private final DefaultListModel<SongDTO> listModel;
    private final JLabel titleLabel;
    private final JButton selectButton;
    private final JButton cancelButton;
    private final JPanel buttonPanel;


    public PlaylistPanel(List<SongDTO> songs) {
        this.textColor = ThemeManager.getInstance().getTextColor();
        this.backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        this.accentColor = ThemeManager.getInstance().getAccentColor();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 300));

        // Title
        titleLabel = new JLabel("Select a Song from Playlist");
        titleLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Song list
        listModel = new DefaultListModel<>();
        songs.forEach(listModel::addElement);

        songList = new JList<>(listModel);
        songList.setCellRenderer(new SongListCellRenderer());
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        GuiUtil.applyModernScrollBar(scrollPane, backgroundColor, accentColor);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons

        buttonPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        selectButton = GuiUtil.createButton("Select");
        selectButton.addActionListener(e -> {
            SongDTO selectedSong = songList.getSelectedValue();
            if (selectedSong != null) {
                firePropertyChange("songSelected", null, selectedSong);
            }
        });

        cancelButton = GuiUtil.createButton("Cancel");
        cancelButton.addActionListener(e -> firePropertyChange("cancel", null, null));

        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        ThemeManager.getInstance().addThemeChangeListener(this);

        onThemeChanged(backgroundColor, textColor, accentColor);
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        super.onThemeChanged(backgroundColor, textColor, accentColor);
        titleLabel.setForeground(textColor);
        buttonPanel.setBackground(backgroundColor);
    }


    // Custom renderer for song list
    private class SongListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            SongDTO song = (SongDTO) value;
            String displayText = String.format("%s - %s", song.getSongTitle(), song.getSongArtist());
            JLabel label = (JLabel) super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus);
            label.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));

            // Add padding and rounded corners
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(2, 0, 2, 0),
                    BorderFactory.createCompoundBorder(
                            isSelected ?
                                    BorderFactory.createLineBorder(accentColor, 1, true) :
                                    BorderFactory.createLineBorder(backgroundColor, 1, true),
                            BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    )
            ));

            // Remove focus border
            if (cellHasFocus) {
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 0, 2, 0),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(accentColor, 1, true),
                                BorderFactory.createEmptyBorder(5, 10, 5, 10)
                        )
                ));
            }

            return label;
        }
    }

    public void cleanup() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}