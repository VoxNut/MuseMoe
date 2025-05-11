package com.javaweb.utils;

import com.javaweb.App;
import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.impl.GoogleDriveService;
import com.javaweb.view.HomePage;
import com.javaweb.view.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Slf4j
public class SongDownloadUtil {

    private static GoogleDriveService googleDriveService;


    public static boolean hasDownloadPermission() {
        Set<String> roles = UserSessionManager.getInstance().getCurrentUser().getRoles();
        return roles.contains(AppConstant.ROLE_PREMIUM) ||
                roles.contains(AppConstant.ROLE_ARTIST) ||
                roles.contains(AppConstant.ROLE_ADMIN);
    }

    public static void downloadSong(JComponent parentComponent, SongDTO song) {
        if (!hasDownloadPermission()) {
            GuiUtil.showToast(parentComponent,
                    "You need a premium subscription to download songs");
            return;
        }

        if (song == null || song.getDriveFileId() == null) {
            GuiUtil.showToast(parentComponent, "This song is not available for download");
            return;
        }

        if (googleDriveService == null) {
            googleDriveService = App.getBean(GoogleDriveService.class);
            if (googleDriveService == null) {
                GuiUtil.showToast(parentComponent, "Could not initialize download service");
                return;
            }
        }

        Path downloadDir = Paths.get(AppConstant.DEFAULT_DOWNLOAD_DIR);
        ensureDirectoryExists(downloadDir);

        String sanitizedFileName = sanitizeFileName(song.getTitle()) + ".mp3";
        File defaultFile = downloadDir.resolve(sanitizedFileName).toFile();

        int option = GuiUtil.showDownloadLocationDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                defaultFile.getAbsolutePath(),
                "Download Location"
        );

        File targetFile;

        if (option == 0) {
            targetFile = defaultFile;
            if (targetFile.exists()) {
                int result = GuiUtil.showConfirmMessageDialog(parentComponent,
                        "File already exists. Do you want to overwrite it?",
                        "File Exists");
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        } else if (option == 1) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Song");

            fileChooser.setCurrentDirectory(downloadDir.toFile());
            fileChooser.setSelectedFile(new File(sanitizedFileName));

            int userSelection = fileChooser.showSaveDialog(parentComponent);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            targetFile = fileChooser.getSelectedFile();

            if (!targetFile.getName().toLowerCase().endsWith(".mp3")) {
                targetFile = new File(targetFile.getAbsolutePath() + ".mp3");
            }

            if (targetFile.exists()) {
                int result = JOptionPane.showConfirmDialog(parentComponent,
                        "File already exists. Do you want to overwrite it?",
                        "File Exists", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        } else {
            return;
        }

        downloadSongInBackground(parentComponent, song, targetFile);

        recordDownload(song);
    }

    private static void ensureDirectoryExists(Path directory) {
        if (Files.exists(directory)) {
            if (!Files.isDirectory(directory)) {
                log.error("Path exists but is not a directory: {}", directory);
                return;
            }
            return;
        }

        try {
            Files.createDirectories(directory);
            log.info("Created download directory: {}", directory);
        } catch (IOException e) {
            log.error("Failed to create download directory {}: {}", directory, e.getMessage(), e);
        }
    }


    private static void downloadSongInBackground(JComponent parentComponent, SongDTO song, File targetFile) {
        JDialog progressDialog = GuiUtil.createProgressDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                "Downloading Song",
                "Downloading " + song.getTitle() + "...");

        SwingWorker<Boolean, Integer> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    try (InputStream in = googleDriveService.getFileContent(song.getDriveFileId());
                         FileOutputStream out = new FileOutputStream(targetFile)) {

                        com.google.api.services.drive.model.File fileMetadata =
                                googleDriveService.getFileMetadata(song.getDriveFileId());

                        long fileSize = fileMetadata != null ? fileMetadata.getSize() : -1;
                        long downloadedSize = 0;

                        byte[] buffer = new byte[8192];
                        int bytesRead;

                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            downloadedSize += bytesRead;

                            if (fileSize > 0) {
                                int progress = (int) ((downloadedSize * 100) / fileSize);
                                publish(progress);
                            }
                        }
                    }

                    return true;
                } catch (Exception e) {
                    log.error("Error downloading song: {}", e.getMessage(), e);
                    return false;
                }
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latestProgress = chunks.get(chunks.size() - 1);
                progressDialog.setTitle("Downloading: " + latestProgress + "%");

                JProgressBar progressBar = GuiUtil.findFirstComponentByType(
                        progressDialog.getContentPane(),
                        JProgressBar.class,
                        bar -> true
                );

                if (progressBar != null) {
                    progressBar.setValue(latestProgress);
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        GuiUtil.showToast(parentComponent,
                                "Song downloaded successfully to:\n" + targetFile.getAbsolutePath(), 5000);
                        LocalSongManager.getDownloadedSongs();

                        if (parentComponent instanceof JComponent component &&
                                SwingUtilities.getWindowAncestor(component) instanceof HomePage homePage) {
                            homePage.refreshDownloadedSongsPanel();
                        }
                    } else {
                        GuiUtil.showToast(parentComponent,
                                "Failed to download the song. Please try again.");
                    }
                } catch (Exception e) {
                    GuiUtil.showToast(parentComponent,
                            "An error occurred while downloading: " + e.getMessage());
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private static void recordDownload(SongDTO song) {
        try {
            CommonApiUtil.createUserDownload(song);
            log.info("Download recorded for song ID: {}", song.getId());
        } catch (Exception e) {
            log.error("Failed to record download for song ID {}: {}", song.getId(), e.getMessage());
        }
    }

    private static String sanitizeFileName(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}