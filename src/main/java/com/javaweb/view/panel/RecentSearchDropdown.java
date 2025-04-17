package com.javaweb.view.panel;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

public class RecentSearchDropdown extends ListThemeablePanel {
    private final JList<SongDTO> songList;
    private final DefaultListModel<SongDTO> listModel;
    private final Consumer<SongDTO> onSongSelected;
    private final JScrollPane scrollPane;
    private final JWindow popupWindow;
    private AWTEventListener clickOutsideListener;
    private final JTextField parentTextField;
    private final JLabel headerLabel;
    private final JLabel historyLabel;
    private int hoveredIndex = -1;
    private CellRendererPane rendererPane;
    private JLabel noteIcon;
    private final JButton clearButton;

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        super.onThemeChanged(backgroundColor, textColor, accentColor);

        headerLabel.setForeground(textColor);
        GuiUtil.changeLabelIconColor(historyLabel, textColor);
        clearButton.setForeground(textColor);
    }

    public RecentSearchDropdown(JTextField parent, List<SongDTO> recentSongs, Consumer<SongDTO> onSongSelected) {

        this.parentTextField = parent;
        this.onSongSelected = onSongSelected;

        setLayout(new BorderLayout());

        // Create visually appealing header with icon
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Add recent icon
        historyLabel = new JLabel(GuiUtil.createImageIcon(AppConstant.HISTORY_ICON_PATH, 16, 16));
        historyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        headerPanel.add(historyLabel, BorderLayout.WEST);

        // Create styled header label
        headerLabel = new JLabel("Recent Songs");
        headerLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        headerLabel.setForeground(textColor);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Create list model and populate with recent songs
        listModel = new DefaultListModel<>();
        for (SongDTO song : recentSongs) {
            listModel.addElement(song);
        }

        // Create the list with custom renderer
        songList = new JList<>(listModel);
        songList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = songList.locationToIndex(e.getPoint());
                if (index != hoveredIndex) {
                    hoveredIndex = index;
                    songList.repaint();
                }
            }
        });

        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredIndex != -1) {
                    hoveredIndex = -1;
                    songList.repaint();
                }
            }
        });
        songList.setCellRenderer(new ModernSongCellRenderer());
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setFixedCellHeight(60); // Fixed height for each row

        // Remove default selection border
        songList.setFocusable(false);

        // Mouse listener for selection
        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = songList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        SongDTO selectedSong = listModel.getElementAt(index);
                        hidePopup();
                        if (onSongSelected != null) {
                            onSongSelected.accept(selectedSong);
                        }
                    }
                }
            }
        });

        // Key listener to handle keyboard navigation
        songList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int index = songList.getSelectedIndex();
                    if (index >= 0) {
                        SongDTO selectedSong = listModel.getElementAt(index);
                        hidePopup();
                        if (onSongSelected != null) {
                            onSongSelected.accept(selectedSong);
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePopup();
                }
            }
        });

        // Add scrollpane to make list scrollable
        scrollPane = new JScrollPane(songList);
        GuiUtil.applyModernScrollBar(scrollPane, backgroundColor, accentColor);
        add(scrollPane, BorderLayout.CENTER);

        // Add a subtle footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create clear history button
        clearButton = GuiUtil.createButton("Clear History");
        clearButton.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 11));


        // Add clear action
        clearButton.addActionListener(e -> {
            listModel.clear();
            hidePopup();
        });

        footerPanel.add(clearButton);
        add(footerPanel, BorderLayout.SOUTH);

        // Create popup window with rounded corners
        popupWindow = new JWindow(SwingUtilities.getWindowAncestor(parent)) {
            @Override
            public void paint(Graphics g) {
                // Create shadow effect
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
                g2.dispose();

                super.paint(g);
            }
        };

        popupWindow.setContentPane(this);
        popupWindow.setFocusableWindowState(false);

        // Register for theme changes
        ThemeManager.getInstance().addThemeChangeListener(this);
        onThemeChanged(backgroundColor, textColor, accentColor);
    }

    public void showPopup(JTextField textField) {
        if (listModel.isEmpty()) {
            return;
        }

        // Remove any existing global listener to prevent duplicate listeners
        if (clickOutsideListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(clickOutsideListener);
            clickOutsideListener = null;
        }

        // Setup and add the global listener
        clickOutsideListener = event -> {
            if (event instanceof MouseEvent mouseEvent) {
                if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
                    try {
                        Point p = mouseEvent.getLocationOnScreen();
                        Rectangle popupBounds = null;
                        Rectangle textFieldBounds = null;

                        // Safely get popup bounds
                        if (popupWindow.isVisible()) {
                            try {
                                popupBounds = new Rectangle(popupWindow.getLocationOnScreen(),
                                        popupWindow.getSize());
                            } catch (IllegalComponentStateException e) {
                                // Window not visible or disposed
                                hidePopup();
                                return;
                            }
                        }

                        // Safely get text field bounds
                        try {
                            if (textField.isShowing()) {
                                textFieldBounds = new Rectangle(textField.getLocationOnScreen(),
                                        textField.getSize());
                            }
                        } catch (IllegalComponentStateException e) {
                            // TextField not visible
                            hidePopup();
                            return;
                        }

                        // Check if click is outside both components
                        if ((popupBounds == null || !popupBounds.contains(p)) &&
                                (textFieldBounds == null || !textFieldBounds.contains(p))) {
                            hidePopup();
                        }
                    } catch (Exception e) {
                        // Handle any unexpected exceptions
                        hidePopup();
                    }
                }
            }
        };

        // Add the listener
        Toolkit.getDefaultToolkit().addAWTEventListener(clickOutsideListener, AWTEvent.MOUSE_EVENT_MASK);

        try {
            // Position the popup window
            if (textField.isShowing()) {
                Point locationOnScreen = textField.getLocationOnScreen();
                popupWindow.setLocation(locationOnScreen.x, locationOnScreen.y + textField.getHeight() + 3);
                popupWindow.setSize(textField.getWidth(), Math.min(320, listModel.getSize() * 60 + 80));
                popupWindow.setVisible(true);

                // Add slight fade-in effect
                SwingUtilities.invokeLater(() -> {
                    songList.requestFocusInWindow();
                });
            }
        } catch (IllegalComponentStateException e) {
            // Component not showing on screen, ignore
        }
    }

    public void hidePopup() {
        if (popupWindow != null) {
            popupWindow.setVisible(false);
        }

        if (clickOutsideListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(clickOutsideListener);
            clickOutsideListener = null;
        }
    }

    public void updateSongs(List<SongDTO> recentSongs) {
        listModel.clear();
        for (SongDTO song : recentSongs) {
            listModel.addElement(song);
        }
    }

    public void cleanup() {
        ThemeManager.getInstance().removeThemeChangeListener(this);
        hidePopup();
    }

    // Custom cell renderer for song items with modern design
    private class ModernSongCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            SongDTO song = (SongDTO) value;

            // Create a panel for each cell with BorderLayout
            JPanel cellPanel = new JPanel();
            cellPanel.setLayout(new BorderLayout(12, 0));

            // Set background based on selection/hover state
            boolean isHovered = (index == hoveredIndex);

            if (isSelected) {
                cellPanel.setBackground(accentColor);
            } else if (isHovered) {
                cellPanel.setBackground(GuiUtil.darkenColor(backgroundColor, 0.1f));
            } else {
                cellPanel.setBackground(backgroundColor);
            }

            // Add indicator panel on the left (music note for selected items)
            JPanel indicatorPanel = new JPanel();
            indicatorPanel.setOpaque(false);
            indicatorPanel.setPreferredSize(new Dimension(24, 24));

            if (isHovered) {
                noteIcon = new JLabel(GuiUtil.createImageIcon(AppConstant.MUSIC_NOTE_ICON_PATH, 46, 46));
                GuiUtil.changeLabelIconColor(noteIcon, textColor);
                indicatorPanel.add(noteIcon);
            }

            cellPanel.add(indicatorPanel, BorderLayout.WEST);

            // Song image
            JLabel imageLabel = new JLabel();
            if (song.getSongImage() != null) {
                imageLabel.setIcon(GuiUtil.createRoundedCornerImageIcon(song.getSongImage(), 10, 46, 46));
            } else {
                imageLabel.setIcon(GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 46, 46));
            }


            imageLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            cellPanel.add(imageLabel, BorderLayout.CENTER);

            // Song info panel
            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            infoPanel.setOpaque(false);

            // Song title
            JLabel titleLabel = new JLabel(song.getSongTitle());
            titleLabel.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
            titleLabel.setForeground(isSelected ? backgroundColor : textColor);

            // Song artist
            JLabel artistLabel = new JLabel(song.getSongArtist());
            artistLabel.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 12));
            artistLabel.setForeground(isSelected ?
                    GuiUtil.darkenColor(backgroundColor, 0.1f) :
                    GuiUtil.darkenColor(textColor, 0.2f));

            infoPanel.add(titleLabel);
            infoPanel.add(artistLabel);
            cellPanel.add(infoPanel, BorderLayout.EAST);

            // Add padding
            cellPanel.setBorder(
                    GuiUtil.createCompoundBorder(isSelected ? accentColor :
                            isHovered ? GuiUtil.darkenColor(backgroundColor, 0.1f) :
                                    backgroundColor, 1, 5, 10, 5, 10)
            );
            return cellPanel;
        }
    }

}