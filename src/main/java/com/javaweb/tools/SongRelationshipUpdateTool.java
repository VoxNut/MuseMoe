package com.javaweb.tools;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.*;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import com.javaweb.repository.*;
import com.javaweb.service.PasswordService;
import com.javaweb.service.StreamingMediaService;
import com.javaweb.service.impl.GoogleDriveService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.transaction.annotation.Propagation;
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

/**
 * Tool to update relationships for songs that already have audio files but lack artist/album connections
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SongRelationshipUpdateTool {

    private final SongRepository songRepository;
    private final StreamingMediaRepository streamingMediaRepository;
    private final GoogleDriveService googleDriveService;
    private final StreamingMediaService streamingMediaService;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LyricsRepository lyricsRepository;
    private final PasswordService passwordService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static final String[] COMMON_ARTIST_PREFIXES = {"by ", "- ", "_ "};
    private static final Pattern FEATURING_PATTERN = Pattern.compile("(?i)\\(ft\\..*?\\)|\\(feat\\..*?\\)|feat\\..*?-|ft\\..*?-");

    // Cache for processed data
    private final Map<String, AlbumEntity> albumCache = new HashMap<>();
    private final Map<String, ArtistEntity> artistCache = new HashMap<>();
    private final Map<Long, SongMetadata> songMetadataCache = new HashMap<>();
    // Track albums to update cover art
    private final Map<String, List<SongMetadata>> albumGroups = new HashMap<>();

    /**
     * Main method to update song relationships
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateSongRelationships() {
        log.info("Starting song relationship update process");

        // Step 1: Get all songs that have streaming media but might be missing other relationships
        List<SongEntity> songsToProcess = songRepository.findAllWithStreamingMedia();
        log.info("Found {} songs with audio files to process", songsToProcess.size());

        // Step 2: Extract metadata from each song
        extractMetadataFromAllSongs(songsToProcess);
        log.info("Extracted metadata from {} songs", songMetadataCache.size());

        // Step 3: Process album groupings and create albums
        createAndAssociateAlbums();
        log.info("Processed album groupings");

        // Step 4: Process each song individually to update relationships
        int updatedCount = 0;
        for (SongEntity song : songsToProcess) {
            try {
                boolean updated = processSingleSong(song);
                if (updated) {
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("Error processing song ID {}: {}", song.getId(), e.getMessage());
                // Clear session after error to prevent cascading failures
                clearSession();
            }
        }

        log.info("Song relationship update completed: {} songs updated", updatedCount);

        // Step 5: Associate artists with their profile pictures (NEW)
        associateArtistProfilePictures();

        // Step 6: Create albums for songs without album associations (NEW)
        createAlbumsForSongsWithoutAlbum();

        // Step 7: Update existing albums with proper metadata (NEW)
        updateExistingAlbums();

    }

    /**
     * Extract metadata from all songs in the database
     */
    private void extractMetadataFromAllSongs(List<SongEntity> songs) {
        for (SongEntity song : songs) {
            try {
                if (song.getStreamingMedia() == null || song.getStreamingMedia().getGoogleDriveId() == null) {
                    log.warn("Song ID {} has no valid streaming media, skipping", song.getId());
                    continue;
                }

                String driveFileId = song.getStreamingMedia().getGoogleDriveId();

                // Get file metadata from Google Drive
                File driveFile = googleDriveService.getFileMetadata(driveFileId);
                if (driveFile == null) {
                    log.warn("Could not find drive file for song ID {}, skipping", song.getId());
                    continue;
                }

                // Extract metadata from the file
                SongMetadata metadata = extractMetadata(driveFileId, driveFile);
                metadata.driveFile = driveFile;
                metadata.songId = song.getId();

                // Store in cache
                songMetadataCache.put(song.getId(), metadata);

                // Group by album info for later processing
                if (StringUtils.isNotBlank(metadata.albumName) && StringUtils.isNotBlank(metadata.artistName)) {
                    String albumKey = (metadata.artistName + ":" + metadata.albumName).toLowerCase();
                    albumGroups.computeIfAbsent(albumKey, k -> new ArrayList<>()).add(metadata);
                }

            } catch (Exception e) {
                log.error("Error extracting metadata for song ID {}: {}", song.getId(), e.getMessage());
            }
        }
    }

    /**
     * Create albums and associate them with artists
     */
    @Transactional
    public void createAndAssociateAlbums() {
        for (Map.Entry<String, List<SongMetadata>> entry : albumGroups.entrySet()) {
            String albumKey = entry.getKey();
            List<SongMetadata> songs = entry.getValue();

            if (songs.isEmpty()) {
                continue;
            }

            // Get first song to extract album info
            SongMetadata firstSong = songs.getFirst();

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
                            StreamingMediaEntity coverArt = uploadAlbumArtwork(song.artworkFile, album.getTitle());
                            if (coverArt != null) {
                                album.setCoverArt(coverArt);
                                break;
                            }
                        }
                    }

                    // Associate with artist
                    ArtistEntity artist = getOrCreateArtist(firstSong.artistName);
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
    }

    /**
     * Process a single song to update its relationships
     */
    @Transactional
    public boolean processSingleSong(SongEntity song) {
        if (song == null || song.getId() == null) {
            return false;
        }

        Long songId = song.getId();
        SongMetadata metadata = songMetadataCache.get(songId);

        if (metadata == null) {
            log.warn("No metadata available for song ID {}", songId);
            return false;
        }

        boolean updated = false;

        try {
            // Update song with basic metadata
            if (StringUtils.isNotBlank(metadata.title) && !metadata.title.equals(song.getTitle())) {
                song.setTitle(metadata.title);
                updated = true;
            }

            if (metadata.duration != null &&
                    (song.getDuration() == null || !song.getDuration().equals(metadata.duration))) {
                song.setDuration(metadata.duration);
                updated = true;
            }

            if (metadata.releaseYear != null &&
                    (song.getReleaseYear() == null || !song.getReleaseYear().equals(metadata.releaseYear))) {
                song.setReleaseYear(metadata.releaseYear);
                updated = true;
            }

            // Update album association
            if (StringUtils.isNotBlank(metadata.albumName) && StringUtils.isNotBlank(metadata.artistName)) {
                String albumKey = (metadata.artistName + ":" + metadata.albumName).toLowerCase();
                AlbumEntity album = albumCache.get(albumKey);

                if (album != null &&
                        (song.getAlbum() == null || !song.getAlbum().getId().equals(album.getId()))) {
                    song.setAlbum(album);
                    updated = true;
                }
            }

            // Update artist associations
            if (StringUtils.isNotBlank(metadata.artistName)) {
                // Primary artist
                ArtistEntity artist = getOrCreateArtist(metadata.artistName);
                if (artist != null && !songHasArtist(song, artist)) {
                    song.addArtist(artist);
                    updated = true;
                }

                // Check for multiple artists (separated by comma, &, and, +)
                String[] artistNames = metadata.artistName.split("(?i)(\\s*,\\s*|\\s+&\\s+|\\s+and\\s+|\\s*\\+\\s*)");
                if (artistNames.length > 1) {
                    for (String artistName : artistNames) {
                        if (StringUtils.isNotBlank(artistName) && !artistName.equals(metadata.artistName)) {
                            ArtistEntity additionalArtist = getOrCreateArtist(artistName.trim());
                            if (additionalArtist != null && !songHasArtist(song, additionalArtist)) {
                                song.addArtist(additionalArtist);
                                updated = true;
                            }
                        }
                    }
                }
            }

            // Create or update lyrics if available
            if (StringUtils.isNotBlank(metadata.lyrics) &&
                    (song.getLyrics() == null || !metadata.lyrics.equals(song.getLyrics().getContent()))) {
                try {
                    createOrUpdateLyrics(song, metadata.lyrics);
                    updated = true;
                } catch (Exception e) {
                    log.error("Error updating lyrics for song ID {}: {}", songId, e.getMessage());
                }
            }

            // Save if updated
            if (updated) {
                songRepository.save(song);
                log.info("Updated relationships for song '{}' (ID: {})", song.getTitle(), song.getId());
            }

            return updated;
        } catch (Exception e) {
            log.error("Error processing song ID {}: {}", songId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a song already has an artist
     */
    private boolean songHasArtist(SongEntity song, ArtistEntity artist) {
        return song.getArtists().stream()
                .anyMatch(a -> a.getId().equals(artist.getId()));
    }

    /**
     * Extract metadata from an MP3 file in Google Drive
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
            metadata.frame = audioHeader.getNumberOfFrames();
            metadata.bitrate = audioHeader.getBitRateAsNumber();
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
     * Get or create an artist entity and ensure it has a user account
     */
    @Transactional
    public ArtistEntity getOrCreateArtist(String artistName) {
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

            // Verify artist has a user account
            if (artist.getUser() == null) {
                try {
                    UserEntity user = createUserForArtist(cleanedName);
                    artist.setUser(user);
                    user.setArtist(artist);
                    artist = artistRepository.save(artist);
                    userRepository.save(user);
                } catch (Exception e) {
                    log.error("Error creating user for existing artist '{}': {}", cleanedName, e.getMessage());
                }
            }
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
     * Creates a user account for an artist
     */
    @Transactional
    public UserEntity createUserForArtist(String artistName) {
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
        user.setPassword(passwordService.encodePassword("1233321"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setLastLogin(LocalDateTime.now());

        // Add user role
        RoleEntity freeRole = roleRepository.findOneByCode(RoleType.FREE);
        if (freeRole != null) {
            Set<RoleEntity> roles = new HashSet<>();
            roles.add(freeRole);
            user.setRoles(roles);
        }

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
    @Transactional
    public void createOrUpdateLyrics(SongEntity song, String lyricsText) {
        if (StringUtils.isBlank(lyricsText) || song.getId() == null) {
            return;
        }

        try {
            LyricsEntity lyrics;

            // First check if lyrics already exist by querying directly
            Optional<LyricsEntity> existingLyrics = lyricsRepository.findBySongId(song.getId());

            if (existingLyrics.isPresent()) {
                lyrics = existingLyrics.get();
                lyrics.setContent(lyricsText);
                lyricsRepository.save(lyrics);
                log.debug("Updated lyrics for song: {}", song.getTitle());
            } else {
                lyrics = new LyricsEntity();
                lyrics.setContent(lyricsText);
                lyrics.setSong(song);
                lyricsRepository.save(lyrics);
                log.debug("Added new lyrics for song: {}", song.getTitle());
            }
        } catch (Exception e) {
            log.error("Error updating lyrics for song '{}': {}", song.getTitle(), e.getMessage());
            throw e;
        }
    }

    /**
     * Associates artists with their profile pictures from Google Drive
     * This method should be called after all artists have been created
     */
    @Transactional
    public void associateArtistProfilePictures() {
        log.info("Starting to associate artists with profile pictures");

        // Step 1: Get all artists from database
        List<ArtistEntity> artists = artistRepository.findAll();
        log.info("Found {} artists to check for profile pictures", artists.size());

        // Step 2: Get all profile pictures from Google Drive
        List<File> profilePictures;
        try {
            profilePictures = googleDriveService.listArtistProfileImages();
            log.info("Found {} profile pictures in Google Drive", profilePictures.size());
        } catch (Exception e) {
            log.error("Failed to retrieve artist profile pictures from Google Drive: {}", e.getMessage());
            return;
        }

        // Step 3: Create a map of profile pictures by name (without extension) for faster lookup
        Map<String, File> profilePictureMap = new HashMap<>();
        for (File picture : profilePictures) {
            // Extract filename without extension
            String filename = picture.getName();
            String filenameWithoutExt = filename;
            if (filename.contains(".")) {
                filenameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
            }

            // Store profile picture in map (lowercase for case-insensitive matching)
            profilePictureMap.put(filenameWithoutExt.toLowerCase(), picture);
        }

        // Step 4: Process each artist to find and associate matching profile picture
        int updatedCount = 0;
        for (ArtistEntity artist : artists) {
            String stageName = artist.getStageName();
            if (StringUtils.isBlank(stageName)) {
                continue;
            }

            // Try multiple variations of the name to find matches
            List<String> namesToTry = generateNameVariationsToTry(stageName);

            boolean found = false;
            for (String nameVariation : namesToTry) {
                File profilePic = profilePictureMap.get(nameVariation.toLowerCase());
                if (profilePic != null) {
                    try {
                        // Create StreamingMediaEntity for the profile picture
                        StreamingMediaEntity mediaEntity = streamingMediaService.getOrCreateStreamingMedia(
                                profilePic.getId(),
                                profilePic.getName(),
                                profilePic.getMimeType(),
                                profilePic.getSize(),
                                profilePic.getWebContentLink()
                        );

                        // Associate with artist
                        artist.setProfilePic(mediaEntity);
                        artistRepository.save(artist);

                        log.info("Associated profile picture '{}' with artist '{}'",
                                profilePic.getName(), artist.getStageName());
                        updatedCount++;
                        found = true;
                        break;
                    } catch (Exception e) {
                        log.error("Error associating profile picture with artist {}: {}",
                                artist.getStageName(), e.getMessage());
                    }
                }
            }

            if (!found) {
                log.debug("No profile picture found for artist: {}", artist.getStageName());
            }
        }

        log.info("Successfully associated {} artists with profile pictures", updatedCount);
    }

    /**
     * Creates albums for songs without album associations
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createAlbumsForSongsWithoutAlbum() {
        log.info("Starting to create albums for songs without album associations");

        // Find songs with streaming media but no album
        List<SongEntity> orphanedSongs = songRepository.findSongsWithoutAlbum();
        log.info("Found {} songs with streaming media but no album", orphanedSongs.size());

        if (orphanedSongs.isEmpty()) {
            log.info("No orphaned songs found. Skipping album creation.");
            return;
        }

        // Group songs by artist
        Map<ArtistEntity, List<SongEntity>> songsByArtist = new HashMap<>();
        for (SongEntity song : orphanedSongs) {
            if (song.getArtists() == null || song.getArtists().isEmpty()) {
                log.warn("Song '{}' (ID: {}) has no artists, can't assign to album",
                        song.getTitle(), song.getId());
                continue;
            }

            // Use the first artist as the primary artist for album grouping
            ArtistEntity primaryArtist = song.getArtists().iterator().next();
            songsByArtist.computeIfAbsent(primaryArtist, k -> new ArrayList<>()).add(song);
        }

        // Create "Singles" album for each artist
        int albumsCreated = 0;
        int songsAssigned = 0;

        for (Map.Entry<ArtistEntity, List<SongEntity>> entry : songsByArtist.entrySet()) {
            ArtistEntity artist = entry.getKey();
            List<SongEntity> songs = entry.getValue();

            try {
                // Create album title
                String albumTitle = artist.getStageName() + " - Singles";

                // Check if this album already exists
                Optional<AlbumEntity> existingAlbum = albumRepository.findByTitle(albumTitle);
                AlbumEntity album;

                if (existingAlbum.isPresent()) {
                    album = existingAlbum.get();
                    log.info("Using existing singles album '{}' for artist '{}'",
                            albumTitle, artist.getStageName());
                } else {
                    // Create new album
                    album = new AlbumEntity();
                    album.setTitle(albumTitle);
                    album.setReleaseYear(CURRENT_YEAR);
                    album.setArtist(artist);

                    // Try to set album cover from artist profile pic
                    if (artist.getProfilePic() != null) {
                        album.setCoverArt(artist.getProfilePic());
                    }

                    album = albumRepository.save(album);
                    log.info("Created new singles album '{}' for artist '{}'",
                            albumTitle, artist.getStageName());
                    albumsCreated++;
                }

                // Assign all songs to this album
                for (SongEntity song : songs) {
                    song.setAlbum(album);
                    songRepository.save(song);
                    songsAssigned++;
                }

            } catch (Exception e) {
                log.error("Error creating singles album for artist '{}': {}",
                        artist.getStageName(), e.getMessage());
            }
        }

        log.info("Created {} singles albums and assigned {} songs", albumsCreated, songsAssigned);
    }

    /**
     * Updates existing albums with proper artist associations and cover art
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateExistingAlbums() {
        log.info("Starting update of existing albums");

        // Get all albums
        List<AlbumEntity> albums = albumRepository.findAll();
        log.info("Found {} albums to check", albums.size());

        int updatedCount = 0;

        for (AlbumEntity album : albums) {
            boolean updated = false;

            try {
                // Find all songs in this album
                List<SongEntity> albumSongs = songRepository.findByAlbumId(album.getId());

                // Skip if no songs
                if (albumSongs.isEmpty()) {
                    log.warn("Album '{}' (ID: {}) has no songs", album.getTitle(), album.getId());
                    continue;
                }

                // Step 1: Check if album needs artist association
                if (album.getArtist() == null) {
                    // Try to determine most common artist in the album's songs
                    Map<ArtistEntity, Integer> artistCounts = new HashMap<>();

                    for (SongEntity song : albumSongs) {
                        for (ArtistEntity artist : song.getArtists()) {
                            artistCounts.put(artist, artistCounts.getOrDefault(artist, 0) + 1);
                        }
                    }

                    // Find the most common artist
                    ArtistEntity mostCommonArtist = artistCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(null);

                    if (mostCommonArtist != null) {
                        album.setArtist(mostCommonArtist);
                        log.info("Set artist '{}' for album '{}'",
                                mostCommonArtist.getStageName(), album.getTitle());
                        updated = true;
                    }
                }

                // Step 2: Check if album needs a release year
                if (album.getReleaseYear() == null) {
                    // Try to find the most common release year from songs
                    Map<Integer, Integer> yearCounts = new HashMap<>();

                    for (SongEntity song : albumSongs) {
                        if (song.getReleaseYear() != null) {
                            yearCounts.put(song.getReleaseYear(),
                                    yearCounts.getOrDefault(song.getReleaseYear(), 0) + 1);
                        }
                    }

                    // Find the most common year
                    Integer mostCommonYear = yearCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(CURRENT_YEAR);

                    album.setReleaseYear(mostCommonYear);
                    log.info("Set release year {} for album '{}'", mostCommonYear, album.getTitle());
                    updated = true;
                }

                // Step 3: Check if album needs cover art
                if (album.getCoverArt() == null) {
                    // Try to use artist's profile picture
                    if (album.getArtist() != null && album.getArtist().getProfilePic() != null) {
                        album.setCoverArt(album.getArtist().getProfilePic());
                        log.info("Set cover art from artist profile for album '{}'", album.getTitle());
                        updated = true;
                    } else {
                        // Try to extract artwork from one of the songs
                        for (SongEntity song : albumSongs) {
                            try {
                                if (song.getStreamingMedia() == null) continue;

                                // Try to extract artwork from this song
                                File driveFile = googleDriveService.getFileMetadata(
                                        song.getStreamingMedia().getGoogleDriveId());

                                if (driveFile != null) {
                                    SongMetadata metadata = extractMetadata(
                                            song.getStreamingMedia().getGoogleDriveId(), driveFile);

                                    if (metadata.artworkFile != null) {
                                        StreamingMediaEntity coverArt = uploadAlbumArtwork(
                                                metadata.artworkFile, album.getTitle());

                                        if (coverArt != null) {
                                            album.setCoverArt(coverArt);
                                            log.info("Set cover art from song '{}' for album '{}'",
                                                    song.getTitle(), album.getTitle());
                                            updated = true;
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("Failed to extract artwork from song '{}': {}",
                                        song.getTitle(), e.getMessage());
                            }
                        }
                    }
                }

                // Save if updated
                if (updated) {
                    albumRepository.save(album);
                    updatedCount++;
                }

            } catch (Exception e) {
                log.error("Error updating album '{}' (ID: {}): {}",
                        album.getTitle(), album.getId(), e.getMessage());
            }
        }

        log.info("Updated {} albums with proper artist associations and cover art", updatedCount);
    }

    /**
     * Generates variations of an artist name to try matching with file names
     */
    private List<String> generateNameVariationsToTry(String artistName) {
        List<String> variations = new ArrayList<>();

        // Original name
        variations.add(artistName);

        // Remove spaces
        variations.add(artistName.replaceAll("\\s+", ""));

        // Replace spaces with underscores
        variations.add(artistName.replaceAll("\\s+", "_"));

        // Replace spaces with hyphens
        variations.add(artistName.replaceAll("\\s+", "-"));

        // Remove special characters
        String simplifiedName = artistName.replaceAll("[^a-zA-Z0-9\\s]", "");
        variations.add(simplifiedName);

        // Try without "The" prefix if present
        if (artistName.toLowerCase().startsWith("the ")) {
            String withoutThe = artistName.substring(4);
            variations.add(withoutThe);
            variations.add(withoutThe.replaceAll("\\s+", ""));
            variations.add(withoutThe.replaceAll("\\s+", "_"));
            variations.add(withoutThe.replaceAll("\\s+", "-"));
        }

        // Try with common name transformations (for featured artists, etc.)
        if (artistName.contains(" feat. ") || artistName.contains(" ft. ")) {
            String mainArtist = artistName.split(" feat.| ft.")[0].trim();
            variations.add(mainArtist);
            variations.add(mainArtist.replaceAll("\\s+", ""));
            variations.add(mainArtist.replaceAll("\\s+", "_"));
            variations.add(mainArtist.replaceAll("\\s+", "-"));
        }

        return variations;
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
     * Clear Hibernate session after errors
     */
    private void clearSession() {
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }
    }

    /**
     * Class to store song metadata during processing
     */
    private static class SongMetadata {
        Long songId;
        String title;
        String artistName;
        String albumName;
        String genre;
        String lyrics;
        Integer releaseYear;
        Integer duration;
        Long frame;
        Long bitrate;
        File driveFile;
        java.io.File artworkFile;
    }

    /**
     * Runner class that can be used to execute this tool
     */
    @Profile("relationship-update-tool")
    @SpringBootApplication
    @EnableJpaRepositories(basePackages = "com.javaweb.repository")
    @ComponentScan(basePackages = {"com.javaweb"})
    public static class SongRelationshipUpdateApplication {
        public static void main(String[] args) {
            disableJaudiotaggerLogging();
            System.setProperty("spring.profiles.active", "relationship-update-tool");
            ApplicationContext ctx = SpringApplication.run(SongRelationshipUpdateApplication.class, args);
            SongRelationshipUpdateTool tool = ctx.getBean(SongRelationshipUpdateTool.class);
            tool.updateSongRelationships();
            System.exit(0);
        }

        @Bean
        public CommandLineRunner commandLineRunner(SongRelationshipUpdateTool tool) {
            return args -> {
                tool.updateSongRelationships();
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