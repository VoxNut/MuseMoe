package com.javaweb.view.panel;

import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PlaylistSelectionPanel extends ListThemeablePanel {
    private final JList<PlaylistDTO> playlistList;
    private final DefaultListModel<PlaylistDTO> listModel;
    private final JLabel titleLabel;
    private final JButton selectButton;
    private final JButton cancelButton;
    private final JPanel buttonPanel;


    public PlaylistSelectionPanel(List<PlaylistDTO> playlists) {
        this.textColor = ThemeManager.getInstance().getTextColor();
        this.backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        this.accentColor = ThemeManager.getInstance().getAccentColor();


        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 300));

        // Title
        titleLabel = new JLabel("Select a Playlist");
        titleLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Playlist list
        listModel = new DefaultListModel<>();
        playlists.forEach(listModel::addElement);

        playlistList = new JList<>(listModel);
        playlistList.setCellRenderer(new PlaylistListCellRenderer());
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistList.setFixedCellHeight(56);
        playlistList.setSelectionBackground(accentColor);
        playlistList.setBorder(null);
        JScrollPane scrollPane = new JScrollPane(playlistList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        GuiUtil.applyModernScrollBar(scrollPane, backgroundColor, accentColor);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        buttonPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        selectButton = GuiUtil.createButton("Select");
        selectButton.addActionListener(e -> {
            PlaylistDTO selectedPlaylist = playlistList.getSelectedValue();
            if (selectedPlaylist != null) {
                firePropertyChange("playlistSelected", null, selectedPlaylist);
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


    // Custom renderer for playlist list
    private class PlaylistListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            PlaylistDTO playlist = (PlaylistDTO) value;

            // Create a custom panel for each cell to have more control over layout
            JPanel cellPanel = GuiUtil.createPanel(new BorderLayout(10, 0));
            cellPanel.setOpaque(true);

            // Set appropriate background and border based on selection state
            if (isSelected) {
                cellPanel.setBackground(accentColor);
                cellPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 2, 2, 2),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(accentColor.brighter(), 1, true),
                                BorderFactory.createEmptyBorder(6, 8, 6, 8)
                        )
                ));
            } else {
                cellPanel.setBackground(backgroundColor);
                cellPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 2, 2, 2),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.1f), 1, true),
                                BorderFactory.createEmptyBorder(6, 8, 6, 8)
                        )
                ));

                // Add hover effect to list items
                cellPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        cellPanel.setBackground(GuiUtil.darkenColor(backgroundColor, 0.1f));
                        cellPanel.repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        cellPanel.setBackground(backgroundColor);
                        cellPanel.repaint();
                    }
                });
            }

            // Create a mini playlist icon/indicator
            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int size = Math.min(getWidth(), getHeight()) - 4;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    // Draw playlist icon
                    g2.setColor(isSelected ? textColor : accentColor);
                    g2.fillRoundRect(x, y, size, size, 6, 6);

                    // Draw "lines" to suggest a playlist
                    g2.setColor(isSelected ? accentColor : textColor);
                    int lineWidth = (int) (size * 0.7);
                    int lineHeight = size / 8;
                    int lineX = x + (size - lineWidth) / 2;
                    int lineStartY = y + size / 4;

                    for (int i = 0; i < 3; i++) {
                        g2.fillRoundRect(lineX, lineStartY + i * (lineHeight + 2), lineWidth, lineHeight, 2, 2);
                    }

                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(24, 24);
                }
            };
            iconPanel.setOpaque(false);

            // Add the icon panel
            cellPanel.add(iconPanel, BorderLayout.WEST);

            // Create the info panel with playlist name and song count
            JPanel infoPanel = GuiUtil.createPanel(new GridLayout(2, 1, 0, 2));

            // Playlist name
            JLabel nameLabel = GuiUtil.createLabel(playlist.getName());
            nameLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));

            // Song count with subtle styling
            String songCountText = playlist.getSongs().size() + " song" + (playlist.getSongs().size() != 1 ? "s" : "");
            JLabel countLabel = new JLabel(songCountText);
            countLabel.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 12));
            countLabel.setForeground(isSelected ?
                    new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 200) :
                    GuiUtil.darkenColor(PlaylistSelectionPanel.this.textColor, 0.3f));

            infoPanel.add(nameLabel);
            infoPanel.add(countLabel);

            cellPanel.add(infoPanel, BorderLayout.CENTER);

            return cellPanel;
        }
    }

    public void cleanup() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}