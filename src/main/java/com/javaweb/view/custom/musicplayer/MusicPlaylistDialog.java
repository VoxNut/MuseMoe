package com.javaweb.view.custom.musicplayer;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.FileUtil;
import com.javaweb.utils.FontUtil;
import com.javaweb.utils.GuiUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MusicPlaylistDialog extends JDialog {
    private final MusicPlayerGUI musicPlayerGUI;

    // store all the paths to be written to a txt file (when we load a playlist)
    private final List<JLabel> songPaths;

    private final Color dialogThemeColor;
    private final Color dialogTextColor;
    private JPanel songContainer;
    private JPanel mainPanel;

    public MusicPlaylistDialog(MusicPlayerGUI musicPlayerGUI, Color dialogThemeColor, Color dialogTextColor) throws IOException {
        this.musicPlayerGUI = musicPlayerGUI;
        this.dialogThemeColor = dialogThemeColor;
        this.dialogTextColor = dialogTextColor;

        songPaths = new LinkedList<>();

        getRootPane().putClientProperty("TitlePane.font", FontUtil.getSpotifyFont(Font.BOLD, 18));
        getRootPane().putClientProperty("JRootPane.titleBarBackground", GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        getRootPane().putClientProperty("JRootPane.titleBarForeground", dialogTextColor);

        // Optional: To ensure consistent inactive state colors
        getRootPane().putClientProperty("JRootPane.titleBarInactiveBackground", GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        getRootPane().putClientProperty("JRootPane.titleBarInactiveForeground", dialogTextColor);


        // Apply the changes to this frame's root pane
        SwingUtilities.updateComponentTreeUI(this.getContentPane());

        // configure dialog
        setTitle("Create Playlist");
        setSize(400, 400);
        setResizable(false);
        setModal(true); // this property makes it so that the dialog has to be closed to give focus
        setLocationRelativeTo(musicPlayerGUI);
        setLayout(new BorderLayout());

        addDialogComponents();
        applyTheme();
        ImageIcon spotifyIcon = GuiUtil.createImageIcon(AppConstant.SPOTIFY_LOGO_PATH, 30, 30);
        GuiUtil.changeIconColor(spotifyIcon, dialogTextColor);
        setIconImage(spotifyIcon.getImage());

    }

    private void addDialogComponents() {

        mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(400, 400));
        // container to hold each song path
        songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setOpaque(false);
        songContainer.setBounds((int) (getWidth() * 0.025), 10, (int) (getWidth() * 0.90), (int) (getHeight() * 0.75));
        mainPanel.add(songContainer);

        // add song button
        JButton addSongButton = new JButton("Add");
        addSongButton.setContentAreaFilled(true);
        addSongButton.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        addSongButton.setForeground(dialogTextColor);
        addSongButton.setBorderPainted(false);
        addSongButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 20));
        addSongButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongButton.addActionListener(e -> {
            // open file explorer
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setPreferredSize(AppConstant.FILE_CHOOSER_SIZE);
            jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
            jFileChooser.setCurrentDirectory(new File("src/main/java/com/javaweb/view/custom/musicplayer/audio"));
            int result = jFileChooser.showOpenDialog(MusicPlaylistDialog.this);

            File selectedFile = jFileChooser.getSelectedFile();
            if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {


                // Convert to relative path
                String relativePath = FileUtil.getRelativeFilePath(selectedFile);
                String songName = relativePath.substring(relativePath.lastIndexOf('\\') + 1, relativePath.lastIndexOf('.'));
                if (isSongAlreadyAdded(songName)) {
                    if (isSongAlreadyAdded(songName)) {
                        GuiUtil.showWarningMessageDialog(this, "Bài hát này đã được thêm vào playlist!");
                        return;
                    }
                }
                JLabel filePathLabel = new JLabel(songName);
                filePathLabel.setBorder(BorderFactory.createLineBorder(dialogTextColor));
                filePathLabel.setForeground(dialogTextColor);
                filePathLabel.setBackground(dialogThemeColor);
                // add to the list
                songPaths.add(filePathLabel);

                // add to container
                songContainer.add(filePathLabel);

                // refreshes dialog to show newly added JLabel
                songContainer.revalidate();
            }
        });
        mainPanel.add(addSongButton);

        // save playlist button
        JButton savePlaylistButton = new JButton("Save");
        savePlaylistButton.setBackground(GuiUtil.darkenColor(dialogThemeColor, 0.2f));
        savePlaylistButton.setForeground(dialogTextColor);
        savePlaylistButton.setBorderPainted(false);
        savePlaylistButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 20));
        savePlaylistButton.setContentAreaFilled(true);
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.addActionListener(e -> {
            try {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setPreferredSize(AppConstant.FILE_CHOOSER_SIZE);
                jFileChooser.setCurrentDirectory(new File("src/main/java/com/javaweb/view/custom/musicplayer/playlist"));
                int result = jFileChooser.showSaveDialog(MusicPlaylistDialog.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    // we use getSelectedFile() to get reference to the file that we are about to save
                    File selectedFile = jFileChooser.getSelectedFile();

                    // convert to .txt file if not done so already
                    // this will check to see if the file does not have the ".txt" file extension
                    if (!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")) {
                        selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                    }

                    // create the new file at the destinated directory
                    selectedFile.createNewFile();

                    // now we will write all the song paths into this file
                    FileWriter fileWriter = new FileWriter(selectedFile);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                    // iterate through our song paths list and write each string into the file
                    // each song will be written in their own row
                    for (JLabel songPath : songPaths) {
                        bufferedWriter.write("src\\main\\java\\com\\javaweb\\view\\custom\\musicplayer\\audio\\" + songPath.getText() + ".mp3" + "\n");
                    }
                    bufferedWriter.close();

                    // display success dialog
                    JOptionPane.showMessageDialog(MusicPlaylistDialog.this, "Tạo Playlist thành công!");

                    // close this dialog
                    MusicPlaylistDialog.this.dispose();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        mainPanel.add(savePlaylistButton);
        add(mainPanel, BorderLayout.CENTER);
    }


    private void applyTheme() {
        GuiUtil.setGradientBackground(mainPanel, dialogThemeColor, GuiUtil.darkenColor(dialogThemeColor, 0.2f), 0.5f, 0.5f, 0.5f);

    }

    private boolean isSongAlreadyAdded(String songName) {
        return songPaths.stream()
                .anyMatch(label -> label.getText().equals(songName));
    }


}
