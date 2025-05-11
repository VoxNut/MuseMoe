package com.javaweb.utils;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.SongDTO;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LocalSongManager {

    private static final Map<String, SongDTO> localSongCache = new HashMap<>();
    private static long lastScanTime = 0;
    private static final long SCAN_INTERVAL = 30000; // 30 seconds between rescans


    public static List<SongDTO> getDownloadedSongs() {
        long now = System.currentTimeMillis();

        // Only rescan if needed (first time or after interval)
        if (localSongCache.isEmpty() || now - lastScanTime > SCAN_INTERVAL) {
            scanDownloadDirectory();
            lastScanTime = now;
        }

        return localSongCache.values().stream()
                .sorted(Comparator.comparing(SongDTO::getDownloadDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }


    public static SongDTO getSongByFilename(String filename) {
        return localSongCache.get(filename);
    }


    public static boolean songExists(String title) {
        String sanitizedTitle = sanitizeFileName(title) + ".mp3";
        return localSongCache.containsKey(sanitizedTitle);
    }


    private static void scanDownloadDirectory() {
        log.info("Scanning download directory for local songs");
        localSongCache.clear();

        Path downloadDir = Paths.get(AppConstant.DEFAULT_DOWNLOAD_DIR);
        if (!Files.exists(downloadDir)) {
            try {
                Files.createDirectories(downloadDir);
                log.info("Created download directory at {}", downloadDir);
            } catch (IOException e) {
                log.error("Failed to create download directory: {}", e.getMessage());
                return;
            }
        }

        try {
            // Find all MP3 files in the directory
            List<File> mp3Files = Files.walk(downloadDir)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            // Process each MP3 file
            for (File file : mp3Files) {
                try {
                    SongDTO song = createSongFromFile(file);
                    if (song != null) {
                        localSongCache.put(file.getName(), song);
                    }
                } catch (Exception e) {
                    log.error("Error processing file {}: {}", file.getName(), e.getMessage());
                }
            }

            log.info("Found {} local songs in download directory", localSongCache.size());

        } catch (IOException e) {
            log.error("Error scanning download directory: {}", e.getMessage());
        }
    }


    private static SongDTO createSongFromFile(File file) {
        try {
            // Extract metadata with JAudioTagger
            org.jaudiotagger.audio.AudioFile audioFile = AudioFileIO.read(file);

            // Extract audio properties
            MP3AudioHeader audioHeader = (MP3AudioHeader) audioFile.getAudioHeader();
            SongDTO songDTO = new SongDTO();
            //Bit rate
            songDTO.setBitrate(audioHeader.getBitRateAsNumber());

            // Set basic audio properties
            int duration = audioHeader.getTrackLength();
            songDTO.setDuration(duration);
            songDTO.setLengthInMilliseconds(duration * 1000);


            // Calculate frame rate (estimation)
            double frameRate = audioHeader.getNumberOfFrames() / (duration * 1000.0);
            songDTO.setFrameRatePerMilliseconds(frameRate);
            songDTO.setFrame(audioHeader.getNumberOfFrames());

            // Get ID3 tag information
            Tag tag = audioFile.getTag();
            if (tag != null) {
                songDTO.setTitle(tag.getFirst(FieldKey.TITLE));
                songDTO.setSongArtist(tag.getFirst(FieldKey.ARTIST));
                songDTO.setSongAlbum(tag.getFirst(FieldKey.ALBUM));
                songDTO.setGenre(tag.getFirst(FieldKey.GENRE));
                songDTO.setSongLyrics(tag.getFirst(FieldKey.LYRICS));
                if (!StringUtils.isBlank(tag.getFirst(FieldKey.YEAR))) {
                    songDTO.setReleaseYear(Integer.valueOf(tag.getFirst(FieldKey.YEAR)));
                }
            }
            songDTO.setId(Long.valueOf(-Math.abs(file.getName().hashCode())));
            songDTO.setDownloadDate(new Date(file.lastModified()));

            songDTO.setLocalFilePath(file.getAbsolutePath());
            songDTO.setIsLocalFile(true);
            extractCoverArt(tag, songDTO);
            songDTO.setSongLength(formatDuration(songDTO));
            songDTO.setFrameRatePerMilliseconds(getFrameRatePerMilliseconds(songDTO));

            return songDTO;
        } catch (Exception e) {
            log.error("Error creating song from file {}: {}", file.getName(), e.getMessage());
            return null;
        }
    }

    public static String formatDuration(SongDTO songDTO) {
        long minutes = songDTO.getDuration() / 60;
        long remainingSeconds = songDTO.getDuration() % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private static double getFrameRatePerMilliseconds(SongDTO songDTO) {
        return (double) songDTO.getFrame() / songDTO.getLengthInMilliseconds();
    }

    /**
     * Gets a tag value with fallback
     */
    private static String getTagValue(Tag tag, FieldKey key, String defaultValue) {
        try {
            String value = tag.getFirst(key);
            return value != null && !value.isEmpty() ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }


    private static void extractCoverArt(Tag tag, SongDTO song) {
        try {
            if (tag.getFirstArtwork() != null) {
                Artwork artwork = tag.getFirstArtwork();
                byte[] imageData = artwork.getBinaryData();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
                song.setSongImage(image);
            }
        } catch (Exception e) {
            log.warn("Could not extract cover art: {}", e.getMessage());
        }
    }

    /**
     * Sanitizes a filename to remove invalid characters
     */
    public static String sanitizeFileName(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}