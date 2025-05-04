package com.javaweb.tools;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.*;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import com.javaweb.repository.*;
import com.javaweb.service.StreamingMediaService;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class SongDatabaseUpdateTool {

    private final SongRepository songRepository;
    private final StreamingMediaRepository streamingMediaRepository;
    private final GoogleDriveService googleDriveService;
    private final StreamingMediaService streamingMediaService;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LyricsRepository lyricsRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static final String[] COMMON_ARTIST_PREFIXES = {"by ", "- ", "_ "};
    private static final Pattern FEATURING_PATTERN = Pattern.compile("(?i)\\(ft\\..*?\\)|\\(feat\\..*?\\)|feat\\..*?-|ft\\..*?-");

    @Transactional
    public void updateSongDatabase() {
        log.info("Starting enhanced song database update process");


        // Step 1: Get all files from the Google Drive music folder
        List<File> driveFiles = googleDriveService.listMusicFilesFromFolder();
        log.info("Found {} files in Google Drive music folder", driveFiles.size());

        // Map to track which files have been processed
        Map<String, Boolean> processedFiles = new HashMap<>();
        Map<String, AlbumEntity> albumCache = new HashMap<>();
        Map<String, ArtistEntity> artistCache = new HashMap<>();

        // Track album groupings (by artist + album name)
        Map<String, List<SongMetadata>> albumGroups = new HashMap<>();

        // Step 2: Process all files and extract metadata
        List<SongMetadata> songMetadataList = new ArrayList<>();

        for (File file : driveFiles) {
            String fileId = file.getId();
            String fileName = file.getName();

            try {
                // Extract metadata from the file
                SongMetadata metadata = extractMetadata(fileId, file);
                metadata.driveFile = file;

                // Group by album info for later processing
                if (StringUtils.isNotBlank(metadata.albumName) && StringUtils.isNotBlank(metadata.artistName)) {
                    String albumKey = (metadata.artistName + ":" + metadata.albumName).toLowerCase();
                    albumGroups.computeIfAbsent(albumKey, k -> new ArrayList<>()).add(metadata);
                }

                songMetadataList.add(metadata);
                processedFiles.put(fileId, true);

            } catch (Exception e) {
                log.error("Error extracting metadata from file '{}': {}", fileName, e.getMessage());
            }
        }

        log.info("Extracted metadata from {} files", songMetadataList.size());

        // Step 3: Create albums for groups of songs
        for (Map.Entry<String, List<SongMetadata>> entry : albumGroups.entrySet()) {
            String albumKey = entry.getKey();
            List<SongMetadata> songs = entry.getValue();

            if (songs.size() < 2) {
                continue; // Skip if fewer than 2 songs in the album
            }

            // Get first song to extract album info
            SongMetadata firstSong = songs.get(0);

            try {
                // Check if album already exists
                Optional<AlbumEntity> existingAlbum = albumRepository.findByTitle(firstSong.albumName);

                AlbumEntity album;
                if (existingAlbum.isPresent()) {
                    album = existingAlbum.get();
                    log.info("Using existing album '{}' for {} songs", album.getTitle(), songs.size());
                } else {
                    // Create new album
                    album = new AlbumEntity();
                    album.setTitle(firstSong.albumName);
                    album.setReleaseYear(firstSong.releaseYear != null ?
                            firstSong.releaseYear : CURRENT_YEAR);

                    // Try to get album artwork from first song with artwork
                    for (SongMetadata song : songs) {
                        if (song.artworkFile != null) {
                            // Create streaming media for album cover
                            StreamingMediaEntity coverArt = uploadAlbumArtwork(song.artworkFile, album.getTitle());
                            if (coverArt != null) {
                                album.setCoverArt(coverArt);
                                break;
                            }
                        }
                    }

                    // Associate with artist
                    ArtistEntity artist = getOrCreateArtist(firstSong.artistName, artistCache);
                    if (artist != null) {
                        album.setArtist(artist);
                    }

                    album = albumRepository.save(album);
                    log.info("Created new album '{}' with {} songs", album.getTitle(), songs.size());
                }

                // Cache album for later use
                albumCache.put(albumKey, album);

            } catch (Exception e) {
                log.error("Error creating album for '{}': {}", albumKey, e.getMessage());
            }
        }

        // Step 4: Create or update songs with proper metadata
        int createdCount = 0;
        int updatedCount = 0;

        for (SongMetadata metadata : songMetadataList) {
            try {
                // Get or create streaming media entity
                StreamingMediaEntity mediaEntity = streamingMediaService.getOrCreateStreamingMedia(
                        metadata.driveFile.getId(),
                        metadata.driveFile.getName(),
                        metadata.driveFile.getMimeType(),
                        metadata.driveFile.getSize(),
                        metadata.driveFile.getWebContentLink()
                );
                mediaEntity = streamingMediaRepository.save(mediaEntity);

                // Check if song exists by streaming media or by title + artist
                Optional<SongEntity> existingSongByMedia = songRepository.findByStreamingMediaId(mediaEntity.getId());
                Optional<SongEntity> existingSongByTitle = Optional.empty();

                if (StringUtils.isNotBlank(metadata.title) && StringUtils.isNotBlank(metadata.artistName)) {
                    existingSongByTitle = songRepository.findByTitleAndArtist(
                            metadata.title, metadata.artistName);
                }

                SongEntity song;
                boolean isNewSong = false;

                if (existingSongByMedia.isPresent()) {
                    song = existingSongByMedia.get();
                    log.info("Updating existing song '{}' with new metadata", song.getTitle());
                } else if (existingSongByTitle.isPresent()) {
                    song = existingSongByTitle.get();
                    song.setStreamingMedia(mediaEntity);
                    log.info("Associating existing song '{}' with audio file", song.getTitle());
                } else {
                    song = new SongEntity();
                    song.setStreamingMedia(mediaEntity);
                    song.setPlayCount(0);
                    song.setExplicitContent(0);
                    isNewSong = true;
                }

                // Update song with metadata
                updateSongWithMetadata(song, metadata, albumCache, artistCache);

                // Save song
                song = songRepository.save(song);

                // Create lyrics if available
                if (StringUtils.isNotBlank(metadata.lyrics)) {
                    createOrUpdateLyrics(song, metadata.lyrics);
                }

                if (isNewSong) {
                    createdCount++;
                    log.info("Created new song '{}' by '{}'", metadata.title, metadata.artistName);
                } else {
                    updatedCount++;
                }

            } catch (Exception e) {
                log.error("Error processing song '{}': {}",
                        metadata.driveFile.getName(), e.getMessage());
            }
        }

        log.info("Enhanced song database update completed: {} songs created, {} songs updated",
                createdCount, updatedCount);
    }

    /**
     * Extracts metadata from an MP3 file in Google Drive
     */
    private SongMetadata extractMetadata(String fileId, File driveFile) throws Exception {
        SongMetadata metadata = new SongMetadata();

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
            int duration = audioHeader.getTrackLength();
            metadata.duration = duration;
            metadata.frameCount = audioHeader.getNumberOfFrames();

            // Get ID3 tag information
            Tag tag = audioFile.getTag();

            if (tag != null) {
                // Core metadata
                metadata.title = cleanupString(tag.getFirst(FieldKey.TITLE));
                metadata.artistName = cleanupString(tag.getFirst(FieldKey.ARTIST));
                metadata.albumName = cleanupString(tag.getFirst(FieldKey.ALBUM));
                metadata.genre = cleanupString(tag.getFirst(FieldKey.GENRE));
                metadata.lyrics = tag.getFirst(FieldKey.LYRICS);

                // Try to parse year
                try {
                    String yearStr = tag.getFirst(FieldKey.YEAR);
                    if (StringUtils.isNotBlank(yearStr)) {
                        metadata.releaseYear = Integer.parseInt(yearStr);
                    }
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }

                // Extract artwork
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    metadata.artworkFile = Files.createTempFile("artwork-", ".jpg").toFile();
                    try (FileOutputStream fos = new FileOutputStream(metadata.artworkFile)) {
                        fos.write(imageData);
                    }
                }
            }

            // If no metadata was found or title is missing, try to extract from filename
            if (StringUtils.isBlank(metadata.title)) {
                extractMetadataFromFilename(driveFile.getName(), metadata);
            }

        } finally {
            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        }

        return metadata;
    }

    /**
     * Extracts metadata from the filename when tags are missing
     */
    private void extractMetadataFromFilename(String fileName, SongMetadata metadata) {
        // Remove extension
        String nameWithoutExt = FilenameUtils.removeExtension(fileName);

        // Try to extract artist and title if they contain separators
        // Common pattern: "Artist - Title" or "Artist_Title"
        String[] parts = null;

        if (nameWithoutExt.contains(" - ")) {
            parts = nameWithoutExt.split(" - ", 2);
        } else if (nameWithoutExt.contains("_")) {
            parts = nameWithoutExt.split("_", 2);
        } else if (nameWithoutExt.contains("-")) {
            parts = nameWithoutExt.split("-", 2);
        }

        if (parts != null && parts.length == 2) {
            // If we already have artist name, don't override
            if (StringUtils.isBlank(metadata.artistName)) {
                metadata.artistName = parts[0].trim();
            }

            metadata.title = parts[1].trim();
        } else {
            // Just use the whole filename as title
            metadata.title = nameWithoutExt.trim();
        }

        // Remove any "featuring" parts from title and move to artist field
        String title = metadata.title;
        if (title != null) {
            var matcher = FEATURING_PATTERN.matcher(title);
            if (matcher.find()) {
                String featuring = matcher.group();
                metadata.title = title.replace(featuring, "").trim();

                // Extract the featured artist
                String featuredArtist = featuring
                        .replaceAll("(?i)\\(ft\\.|\\(feat\\.|feat\\.|ft\\.", "")
                        .replace(")", "")
                        .replace("-", "")
                        .trim();

                if (StringUtils.isNotBlank(featuredArtist) &&
                        StringUtils.isNotBlank(metadata.artistName)) {
                    metadata.artistName += " feat. " + featuredArtist;
                }
            }
        }
    }

    /**
     * Updates a song entity with metadata
     */
    private void updateSongWithMetadata(SongEntity song, SongMetadata metadata,
                                        Map<String, AlbumEntity> albumCache,
                                        Map<String, ArtistEntity> artistCache) {
        // Set basic properties
        song.setTitle(metadata.title);
        song.setDuration(metadata.duration);
        song.setReleaseYear(metadata.releaseYear != null ? metadata.releaseYear : CURRENT_YEAR);

        // Associate with album if available
        if (StringUtils.isNotBlank(metadata.albumName) && StringUtils.isNotBlank(metadata.artistName)) {
            String albumKey = (metadata.artistName + ":" + metadata.albumName).toLowerCase();
            AlbumEntity album = albumCache.get(albumKey);

            if (album == null) {
                // Try to find by title
                Optional<AlbumEntity> existingAlbum = albumRepository.findByTitle(metadata.albumName);
                if (existingAlbum.isPresent()) {
                    album = existingAlbum.get();
                    albumCache.put(albumKey, album);
                }
            }

            if (album != null) {
                song.setAlbum(album);
            }
        }

        // Associate with artist
        if (StringUtils.isNotBlank(metadata.artistName)) {
            ArtistEntity artist = getOrCreateArtist(metadata.artistName, artistCache);
            if (artist != null && !song.getArtists().contains(artist)) {
                song.addArtist(artist);
            }

            // Check for multiple artists (separated by comma, &, and, +)
            String[] artistNames = metadata.artistName.split("(?i)(\\s*,\\s*|\\s+&\\s+|\\s+and\\s+|\\s*\\+\\s*)");
            if (artistNames.length > 1) {
                for (String artistName : artistNames) {
                    if (StringUtils.isNotBlank(artistName) && !artistName.equals(metadata.artistName)) {
                        ArtistEntity additionalArtist = getOrCreateArtist(artistName.trim(), artistCache);
                        if (additionalArtist != null && !song.getArtists().contains(additionalArtist)) {
                            song.addArtist(additionalArtist);
                        }
                    }
                }
            }
        }
    }


    private ArtistEntity getOrCreateArtist(String artistName, Map<String, ArtistEntity> artistCache) {
        if (StringUtils.isBlank(artistName)) {
            return null;
        }

        // Clean up artist name
        String cleanedName = artistName.trim();

        // Check cache first
        ArtistEntity artist = artistCache.get(cleanedName.toLowerCase());
        if (artist != null) {
            return artist;
        }

        // Try to find existing artist
        Optional<ArtistEntity> existingArtist = artistRepository.findByStageName(cleanedName);

        if (existingArtist.isPresent()) {
            artist = existingArtist.get();
        } else {
            try {
                // Create a user for the artist first
                UserEntity user = createUserForArtist(cleanedName);

                // Create new artist entity
                artist = new ArtistEntity();
                artist.setStageName(cleanedName);
                artist.setBio("Artist imported by system");
                artist.setUser(user);  // Link artist to user

                // Save the artist entity
                artist = artistRepository.save(artist);

                // Update the user with the artist role and backlink
                addArtistRoleToUser(user);
                user.setArtist(artist);
                userRepository.save(user);

                log.info("Created new artist '{}' with user account", cleanedName);
            } catch (Exception e) {
                log.error("Failed to create artist '{}': {}", cleanedName, e.getMessage(), e);
                return null;
            }
        }

        // Cache for future use
        artistCache.put(cleanedName.toLowerCase(), artist);
        return artist;
    }

    /**
     * Creates a user account for an artist
     */
    private UserEntity createUserForArtist(String artistName) {
        // Create a new user based on artist information
        UserEntity user = new UserEntity();

        // Generate username from artist name
        String username = artistName.toLowerCase()
                .replaceAll("[^a-z0-9]", "")  // Remove special characters
                .replaceAll("\\s+", "");      // Remove spaces

        // Ensure username is not empty
        if (username.isEmpty()) {
            username = "artist" + System.currentTimeMillis();
        }

        // Check if username already exists and modify if needed
        if (userRepository.existsByUsername(username)) {
            username = username + System.currentTimeMillis() % 1000;
        }

        // Generate email - ensure it's unique
        String email = username + "@musemoe.com";
        if (userRepository.existsByEmail(email)) {
            email = username + System.currentTimeMillis() % 1000 + "@musemoe.com";
        }

        // Set user properties
        user.setUsername(username);
        user.setFullName(artistName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Artist@" + System.currentTimeMillis() % 10000));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setLastLogin(LocalDateTime.now());

        // Add user role
        user.setRoles(Collections.singleton(roleRepository.findOneByCode(RoleType.FREE)));

        return userRepository.save(user);
    }

    /**
     * Add the artist role to a user
     */
    private void addArtistRoleToUser(UserEntity user) {
        RoleEntity artistRole = roleRepository.findOneByCode(RoleType.ARTIST);
        if (artistRole != null && !userHasRole(user, artistRole.getCode())) {
            Set<RoleEntity> roles = new HashSet<>(user.getRoles());
            roles.add(artistRole);
            user.setRoles(roles);
        }
    }

    /**
     * Check if a user has a specific role
     */
    private boolean userHasRole(UserEntity user, RoleType roleCode) {
        return user.getRoles().stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }

    /**
     * Creates or updates lyrics for a song
     */
    private void createOrUpdateLyrics(SongEntity song, String lyricsText) {
        if (StringUtils.isBlank(lyricsText)) {
            return;
        }

        LyricsEntity lyrics = null;

        // Check if lyrics already exist
        if (song.getLyrics() != null) {
            lyrics = song.getLyrics();
            lyrics.setContent(lyricsText);
        } else {
            // Create new lyrics
            lyrics = new LyricsEntity();
            lyrics.setContent(lyricsText);
            lyrics.setSong(song);
        }

        lyricsRepository.save(lyrics);
    }

    /**
     * Uploads album artwork to Google Drive
     */
    private StreamingMediaEntity uploadAlbumArtwork(java.io.File artworkFile, String albumName) {
        try {
            // Call the service to upload the file
            com.google.api.services.drive.model.File uploadedFile =
                    googleDriveService.uploadAlbumArtwork(artworkFile, albumName);

            if (uploadedFile == null) {
                return null;
            }

            // Create and save StreamingMediaEntity
            StreamingMediaEntity mediaEntity = streamingMediaService.getOrCreateStreamingMedia(
                    uploadedFile.getId(),
                    uploadedFile.getName(),
                    uploadedFile.getMimeType(),
                    uploadedFile.getSize(),
                    uploadedFile.getWebContentLink()
            );

            // Save the media entity
            return streamingMediaRepository.save(mediaEntity);

        } catch (Exception e) {
            log.error("Error uploading artwork for album '{}': {}", albumName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Cleans up a string by removing common prefixes and extra whitespace
     */
    private String cleanupString(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        String result = input.trim();

        // Remove common prefixes
        for (String prefix : COMMON_ARTIST_PREFIXES) {
            if (result.startsWith(prefix)) {
                result = result.substring(prefix.length()).trim();
            }
        }

        return result;
    }

    /**
     * Class to store song metadata during processing
     */
    private static class SongMetadata {
        String title;
        String artistName;
        String albumName;
        String genre;
        String lyrics;
        Integer releaseYear;
        Integer duration;
        long frameCount;
        File driveFile;
        java.io.File artworkFile;
    }

    /**
     * Runner class that can be used to execute this tool
     */
    @Profile("enhanced-db-update-tool")
    @SpringBootApplication
    @EnableJpaRepositories(basePackages = "com.javaweb.repository")
    @ComponentScan(basePackages = {"com.javaweb"})
    public static class EnhancedSongDatabaseUpdateApplication {
        public static void main(String[] args) {
            disableJaudiotaggerLogging();
            System.setProperty("spring.profiles.active", "enhanced-db-update-tool");
            ApplicationContext ctx = SpringApplication.run(EnhancedSongDatabaseUpdateApplication.class, args);
            SongDatabaseUpdateTool tool = ctx.getBean(SongDatabaseUpdateTool.class);
            tool.updateSongDatabase();
        }

        @Bean
        public CommandLineRunner commandLineRunner(SongDatabaseUpdateTool tool) {
            return args -> {
                tool.updateSongDatabase();
                System.exit(0);
            };
        }

        @Bean(name = "toolPasswordEncoder")
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
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