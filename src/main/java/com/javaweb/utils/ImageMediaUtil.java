package com.javaweb.utils;

import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageMediaUtil {
    private final GoogleDriveService googleDriveService;

    // Track ongoing image loading tasks to prevent duplicate requests
    private final Map<String, CompletableFuture<BufferedImage>> ongoingImageLoads = new ConcurrentHashMap<>();

    // Track failed image IDs to avoid excessive retries
    private final Set<String> failedImageIds = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> retryCount = new ConcurrentHashMap<>();
    private static final int MAX_RETRIES = 2;

    /**
     * Populates a song's image asynchronously
     */
    public void populateSongImage(SongDTO song) {
        populateSongImage(song, null);
    }

    /**
     * Populates a song's image asynchronously with callback when complete
     */
    public void populateSongImage(SongDTO song, Consumer<BufferedImage> callback) {
        if (song == null || song.getAlbumArtId() == null) {
            return;
        }

        if (song.getSongImage() != null) {
            // Image is already loaded, just call the callback
            if (callback != null) {
                SwingUtilities.invokeLater(() -> callback.accept(song.getSongImage()));
            }
            return;
        }

        String imageId = song.getAlbumArtId();
        loadImageAsync(imageId, image -> {
            if (image != null) {
                song.setSongImage(image);
                if (callback != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(image));
                }
            }
        });
    }

    /**
     * Synchronously loads a song image with timeout
     *
     * @return true if image was successfully loaded, false otherwise
     */
    public boolean populateSongImageSync(SongDTO song, int timeoutMillis) {
        if (song == null || song.getAlbumArtId() == null) {
            return false;
        }

        if (song.getSongImage() != null) {
            return true; // Already loaded
        }

        String imageId = song.getAlbumArtId();
        BufferedImage image = loadImageSync(imageId, timeoutMillis);
        if (image != null) {
            song.setSongImage(image);
            return true;
        }
        return false;
    }

    /**
     * Populates an album's image asynchronously with callback when complete
     */
    public void populateAlbumImage(AlbumDTO album, Consumer<BufferedImage> callback) {
        if (album == null || album.getImageId() == null) {
            return;
        }

        if (album.getAlbumImage() != null) {
            // Image is already loaded, just call the callback
            if (callback != null) {
                SwingUtilities.invokeLater(() -> callback.accept(album.getAlbumImage()));
            }
            return;
        }

        String imageId = album.getImageId();
        loadImageAsync(imageId, image -> {
            if (image != null) {
                album.setAlbumImage(image);
                if (callback != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(image));
                }
            }
        });
    }

    public void populateArtistProfile(ArtistDTO artist) {
        populateArtistProfile(artist, null);
    }

    public void populateArtistProfile(ArtistDTO artist, Consumer<BufferedImage> callback) {
        if (artist == null || artist.getProfilePictureId() == null) {
            return;
        }

        if (artist.getProfileImage() != null) {
            // Image is already loaded, just call the callback
            if (callback != null) {
                SwingUtilities.invokeLater(() -> callback.accept(artist.getProfileImage()));
            }
            return;
        }

        String imageId = artist.getProfilePictureId();
        loadImageAsync(imageId, image -> {
            if (image != null) {
                artist.setProfileImage(image);
                if (callback != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(image));
                }
            }
        });
    }

    public void populateUserProfile(UserDTO user) {
        populateUserProfile(user, null);
    }

    public void populateUserProfile(UserDTO user, Consumer<BufferedImage> callback) {
        if (user == null || user.getAvatarId() == null) {
            return;
        }

        if (user.getAvatarImage() != null) {
            // Image is already loaded, just call the callback
            if (callback != null) {
                SwingUtilities.invokeLater(() -> callback.accept(user.getAvatarImage()));
            }
            return;
        }

        String imageId = user.getAvatarId();
        loadImageAsync(imageId, image -> {
            if (image != null) {
                user.setAvatarImage(image);
                if (callback != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(image));
                }
            }
        });
    }

    /**
     * Legacy method maintained for compatibility
     */
    public void loadAndWaitForImage(SongDTO song, int timeoutMillis) {
        populateSongImageSync(song, timeoutMillis);
    }

    /**
     * Core method for loading images asynchronously with consistent behavior
     */
    public void loadImageAsync(String imageId, Consumer<BufferedImage> callback) {
        if (imageId == null) {
            return;
        }

        // Check for excessive failures
        if (failedImageIds.contains(imageId)) {
            log.debug("Skipping previously failed image: {}", imageId);
            return;
        }

        // Check cache first
        if (ImageCache.containsImage(imageId)) {
            BufferedImage cachedImage = ImageCache.getImage(imageId);
            if (callback != null) {
                callback.accept(cachedImage);
            }
            return;
        }

        // Check if this image is already being loaded
        CompletableFuture<BufferedImage> existingFuture = ongoingImageLoads.get(imageId);
        if (existingFuture != null && !existingFuture.isDone()) {
            // This image is already being loaded, just add our callback
            existingFuture.thenAcceptAsync(image -> {
                if (image != null && callback != null) {
                    callback.accept(image);
                }
            });
            return;
        }

        // Start a new loading task
        CompletableFuture<BufferedImage> future = CompletableFuture.supplyAsync(() -> {
            try {
                return googleDriveService.getBufferImage(imageId);
            } catch (Exception e) {
                // Track failure for retry logic
                int attempts = retryCount.getOrDefault(imageId, 0) + 1;
                retryCount.put(imageId, attempts);

                if (attempts >= MAX_RETRIES) {
                    failedImageIds.add(imageId);
                    log.error("Failed to load image after {} attempts: {}", attempts, imageId);
                } else {
                    log.warn("Error loading image {}, attempt #{}: {}", imageId, attempts, e.getMessage());
                }
                return null;
            }
        });

        // Store this task so we don't start duplicate requests
        ongoingImageLoads.put(imageId, future);

        future.thenAcceptAsync(image -> {
            // Task completed, remove from tracking
            ongoingImageLoads.remove(imageId);

            if (image != null) {
                // Success - cache and return the image
                ImageCache.putImage(imageId, image);
                if (callback != null) {
                    callback.accept(image);
                }

                // Clear any failure records
                failedImageIds.remove(imageId);
                retryCount.remove(imageId);
            }
        });
    }

    /**
     * Load an image synchronously with timeout
     *
     * @return the loaded image or null if loading failed
     */
    public BufferedImage loadImageSync(String imageId, int timeoutMillis) {
        if (imageId == null) {
            return null;
        }

        // Check cache first
        if (ImageCache.containsImage(imageId)) {
            return ImageCache.getImage(imageId);
        }

        // Check if this image is already being loaded
        CompletableFuture<BufferedImage> existingFuture = ongoingImageLoads.get(imageId);
        if (existingFuture != null && !existingFuture.isDone()) {
            try {
                // Wait for the existing loading task
                return existingFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.debug("Waiting for existing image load timed out: {}", imageId);
                return null;
            }
        }

        // Start a new loading task
        CompletableFuture<BufferedImage> future = CompletableFuture.supplyAsync(() -> {
            try {
                return googleDriveService.getBufferImage(imageId);
            } catch (Exception e) {
                log.error("Error loading image synchronously: {}", e.getMessage());
                return null;
            }
        });

        // Track this task
        ongoingImageLoads.put(imageId, future);

        try {
            // Wait for the result with timeout
            BufferedImage image = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            if (image != null) {
                ImageCache.putImage(imageId, image);
            }
            return image;
        } catch (Exception e) {
            log.debug("Synchronous image loading timed out: {}", imageId);
            // Don't cancel the future - let it complete in the background
            return null;
        } finally {
            // Only remove from tracking if the task is done
            if (future.isDone()) {
                ongoingImageLoads.remove(imageId);
            }
        }
    }

    /**
     * Pre-loads a batch of images in the background
     * Useful when you know you'll need multiple images soon
     */
    public void preloadImages(Set<String> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            for (String imageId : imageIds) {
                if (!ImageCache.containsImage(imageId) &&
                        !failedImageIds.contains(imageId) &&
                        !ongoingImageLoads.containsKey(imageId)) {
                    loadImageAsync(imageId, null);
                }
            }
        });
    }

    /**
     * Clears all failed image tracking to allow retrying
     */
    public void resetFailedImages() {
        failedImageIds.clear();
        retryCount.clear();
    }
}