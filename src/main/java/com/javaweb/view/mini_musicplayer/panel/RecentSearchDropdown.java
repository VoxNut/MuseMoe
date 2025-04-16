package com.javaweb.view.mini_musicplayer.panel;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

public class RecentSearchDropdown extends JPanel implements ThemeChangeListener {
    private final Color backgroundColor;
    private final Color textColor;
    private final Color accentColor;

    private final JList<SongDTO> songList;
    private final DefaultListModel<SongDTO> listModel;
    private final Consumer<SongDTO> onSongSelected;
    private final JScrollPane scrollPane;
    private final JWindow popupWindow;
    private AWTEventListener clickOutsideListener;
    private final JTextField parentTextField;

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        setBackground(backgroundColor);
        setBorder(GuiUtil.createCompoundBorder(textColor, 1));

        songList.setBackground(backgroundColor);
        songList.setForeground(textColor);
        songList.setSelectionBackground(GuiUtil.darkenColor(backgroundColor, 0.1f));
    }

    public RecentSearchDropdown(JTextField parent, List<SongDTO> recentSongs, Consumer<SongDTO> onSongSelected) {
        backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        textColor = ThemeManager.getInstance().getTextColor();
        accentColor = ThemeManager.getInstance().getAccentColor();


        this.parentTextField = parent;
        this.onSongSelected = onSongSelected;

        setLayout(new BorderLayout());

        // Create header label
        JLabel headerLabel = GuiUtil.createLabel("Recent songs", Font.BOLD, 14);
        headerLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(headerLabel, BorderLayout.NORTH);

        // Create list model and populate with recent songs
        listModel = new DefaultListModel<>();
        for (SongDTO song : recentSongs) {
            listModel.addElement(song);
        }

        // Create the list with custom renderer
        songList = new JList<>(listModel);
        songList.setCellRenderer(new RecentSongCellRenderer());
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setFixedCellHeight(60); // Fixed height for each row

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
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = accentColor;
                this.trackColor = backgroundColor;
            }
        });
        add(scrollPane, BorderLayout.CENTER);

        // Create popup window
        popupWindow = new JWindow(SwingUtilities.getWindowAncestor(parent));
        popupWindow.setContentPane(this);
        popupWindow.setFocusableWindowState(false);
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
                popupWindow.setLocation(locationOnScreen.x, locationOnScreen.y + textField.getHeight());
                popupWindow.setSize(textField.getWidth(), Math.min(300, listModel.getSize() * 60 + 40));
                popupWindow.setVisible(true);
                SwingUtilities.invokeLater(songList::requestFocusInWindow);
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

    // Custom cell renderer for song items
    private class RecentSongCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            SongDTO song = (SongDTO) value;

            // Create a panel for each cell with BorderLayout
            JPanel cellPanel = new JPanel(new BorderLayout(10, 0));
            cellPanel.setOpaque(true);

            // Set background based on selection state
            if (isSelected) {
                cellPanel.setBackground(accentColor);
            } else {
                cellPanel.setBackground(backgroundColor);

                // Add hover effect
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

            // Song image on the left
            JLabel imageLabel = new JLabel();
            if (song.getSongImage() != null) {
                imageLabel.setIcon(GuiUtil.createRoundedCornerImageIcon(song.getSongImage(), 20, 50, 50));
            } else {
                imageLabel.setIcon(GuiUtil.createImageIcon(AppConstant.DEFAULT_COVER_PATH, 50, 50));
            }
            imageLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

            // Song info panel on the right
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setOpaque(false);

            // Song title
            JLabel titleLabel = GuiUtil.createLabel(song.getSongTitle(), Font.BOLD, 14);
            titleLabel.setForeground(isSelected ? backgroundColor : textColor);

            // Song artist
            JLabel artistLabel = GuiUtil.createLabel(song.getSongArtist(), Font.PLAIN, 12);
            artistLabel.setForeground(isSelected ?
                    GuiUtil.darkenColor(backgroundColor, 0.1f) :
                    GuiUtil.darkenColor(textColor, 0.2f));

            infoPanel.add(titleLabel);
            infoPanel.add(artistLabel);

            // Add components to cell panel
            cellPanel.add(imageLabel, BorderLayout.WEST);
            cellPanel.add(infoPanel, BorderLayout.CENTER);

            // Add padding
            cellPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            return cellPanel;
        }
    }
}