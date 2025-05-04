package com.javaweb.tools;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.SongEntity;
import com.javaweb.repository.SongRepository;
import com.javaweb.service.impl.GoogleDriveService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tool to update frame count and bitrate for songs that already have audio files
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SongMetadataUpdateTool {

    private final SongRepository songRepository;
    private final GoogleDriveService googleDriveService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int BATCH_SIZE = 50;

    /**
     * Main method to update song frame count and bitrate
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateSongMetadata() {
        log.info("Starting song frame count and bitrate update process");

        // Step 1: Get all songs that have streaming media
        List<SongEntity> songsToProcess = songRepository.findAllWithStreamingMedia();
        log.info("Found {} songs with audio files to process", songsToProcess.size());

        // Track statistics
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Process songs in batches to improve memory usage
        for (int i = 0; i < songsToProcess.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, songsToProcess.size());
            List<SongEntity> batch = songsToProcess.subList(i, endIndex);

            log.info("Processing batch {}/{} (songs {}-{})",
                    (i / BATCH_SIZE) + 1,
                    (songsToProcess.size() + BATCH_SIZE - 1) / BATCH_SIZE,
                    i + 1,
                    endIndex);

            // Process each song in the batch
            batch.forEach(song -> {
                try {
                    boolean updated = processSingleSong(song);
                    if (updated) {
                        updatedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Error processing song ID {}: {}", song.getId(), e.getMessage());
                    // Clear session after error to prevent cascading failures
                    clearSession();
                }
            });

            // Clear session after each batch
            clearSession();
        }

        log.info("Song metadata update completed: {} songs updated, {} errors",
                updatedCount.get(), errorCount.get());
    }

    /**
     * Process a single song to update its frame count and bitrate
     */
    @Transactional
    public boolean processSingleSong(SongEntity song) {
        if (song == null || song.getId() == null) {
            return false;
        }

        // Skip if the song already has frame count and bitrate
        if (song.getFrame() != null && song.getBitrate() != null) {
            log.debug("Song ID {} already has frame count and bitrate, skipping", song.getId());
            return false;
        }

        if (song.getStreamingMedia() == null || song.getStreamingMedia().getGoogleDriveId() == null) {
            log.warn("Song ID {} has no valid streaming media, skipping", song.getId());
            return false;
        }

        String driveFileId = song.getStreamingMedia().getGoogleDriveId();
        String songTitle = song.getTitle();

        try {
            // Get file metadata from Google Drive
            File driveFile = googleDriveService.getFileMetadata(driveFileId);
            if (driveFile == null) {
                log.warn("Could not find drive file for song '{}' (ID: {}), skipping",
                        songTitle, song.getId());
                return false;
            }

            // Extract technical metadata from the file
            AudioMetadata metadata = extractTechnicalMetadata(driveFileId);
            boolean updated = false;

            // Update frame count if needed
            if (metadata.frameCount > 0 && (song.getFrame() == null || song.getFrame() != metadata.frameCount)) {
                song.setFrame(metadata.frameCount);
                log.debug("Updated frame count for song '{}': {}", songTitle, metadata.frameCount);
                updated = true;
            }

            // Update bitrate if needed
            if (metadata.bitrate > 0 && (song.getBitrate() == null || song.getBitrate() != metadata.bitrate)) {
                song.setBitrate(metadata.bitrate);
                log.debug("Updated bitrate for song '{}': {}", songTitle, metadata.bitrate);
                updated = true;
            }

            // Check if duration needs updating too
            if (metadata.duration > 0 && (song.getDuration() == null || song.getDuration() != metadata.duration)) {
                song.setDuration(metadata.duration);
                log.debug("Updated duration for song '{}': {} seconds", songTitle, metadata.duration);
                updated = true;
            }

            // Save if updated
            if (updated) {
                songRepository.save(song);
                log.info("Updated technical metadata for song '{}' (ID: {})", songTitle, song.getId());
            }

            return updated;
        } catch (Exception e) {
            log.error("Error processing song '{}' (ID: {}): {}",
                    songTitle, song.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Extract technical metadata from an MP3 file in Google Drive
     */
    private AudioMetadata extractTechnicalMetadata(String fileId) throws Exception {
        AudioMetadata metadata = new AudioMetadata();

        // Create a temporary file to analyze with JAudioTagger
        Path tempFile = Files.createTempFile("song-", ".mp3");

        try (InputStream inputStream = googleDriveService.getFileContent(fileId);
             OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {

            // Download to temp file
            IOUtils.copy(inputStream, outputStream);

            // Extract metadata with JAudioTagger
            AudioFile audioFile = AudioFileIO.read(tempFile.toFile());

            // Extract audio properties
            MP3AudioHeader audioHeader = (MP3AudioHeader) audioFile.getAudioHeader();
            metadata.duration = audioHeader.getTrackLength();
            metadata.frameCount = audioHeader.getNumberOfFrames();
            metadata.bitrate = audioHeader.getBitRateAsNumber();

        } finally {
            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        }

        return metadata;
    }

    /**
     * Clear Hibernate session after errors
     */
    private void clearSession() {
        if (entityManager != null) {
            // Just clear the session to release memory
            entityManager.clear();
        }
    }

    /**
     * Class to store audio metadata during processing
     */
    private static class AudioMetadata {
        long frameCount;
        long bitrate;
        int duration;
    }

    /**
     * Runner class that can be used to execute this tool
     */
    @Profile("metadata-update-tool")
    @SpringBootApplication
    @EnableJpaRepositories(basePackages = "com.javaweb.repository")
    @ComponentScan(basePackages = {"com.javaweb"})
    public static class SongMetadataUpdateApplication {
        public static void main(String[] args) {
            disableJaudiotaggerLogging();
            System.setProperty("spring.profiles.active", "metadata-update-tool");
            ApplicationContext ctx = SpringApplication.run(SongMetadataUpdateApplication.class, args);
            SongMetadataUpdateTool tool = ctx.getBean(SongMetadataUpdateTool.class);
            tool.updateSongMetadata();
            System.exit(0);
        }

        @Bean
        public CommandLineRunner commandLineRunner(SongMetadataUpdateTool tool) {
            return args -> {
                tool.updateSongMetadata();
                System.exit(0);
            };
        }

        private static void disableJaudiotaggerLogging() {
            // Get the global logger
            Logger rootLogger = Logger.getLogger("");

            // Set the global logging level to WARNING or higher
            rootLogger.setLevel(Level.WARNING);

            // Also set the level for all handlers to ensure it's properly applied
            for (Handler handler : rootLogger.getHandlers()) {
                handler.setLevel(Level.WARNING);
            }

            // Specifically target the Jaudiotagger logger
            Logger jaudiotaggerLogger = Logger.getLogger("org.jaudiotagger");
            jaudiotaggerLogger.setLevel(Level.SEVERE); // Only show severe errors
        }
    }
}