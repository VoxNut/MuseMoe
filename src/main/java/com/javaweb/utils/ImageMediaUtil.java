package com.javaweb.utils;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageMediaUtil {
    private final GoogleDriveService googleDriveService;

    public void populateSongImage(SongDTO song) {
        populateSongImage(song, null);
    }

    public void populateSongImage(SongDTO song, Consumer<BufferedImage> callback) {
        if (song.getSongImage() == null && song.getAlbumArtId() != null) {
            String imageId = song.getAlbumArtId();

            // Check cache first
            if (ImageCache.containsImage(imageId)) {
                BufferedImage cachedImage = ImageCache.getImage(imageId);
                song.setSongImage(cachedImage);
                if (callback != null) {
                    callback.accept(cachedImage);
                }
                return;
            }
            CompletableFuture.supplyAsync(() -> {
                try {
                    return googleDriveService.getBufferImage(imageId);
                } catch (Exception e) {
                    log.error("Error loading image for song {}: {}", song.getId(), e.getMessage());
                    return null;
                }
            }).thenAcceptAsync(image -> {
                if (image != null) {
                    ImageCache.putImage(imageId, image);
                    song.setSongImage(image);
                    if (callback != null) {
                        SwingUtilities.invokeLater(() -> callback.accept(image));
                    }
                }
            });
        }
    }

    public void populateArtistProfile(ArtistDTO artist) {
        populateArtistProfile(artist, null);
    }

    public void populateArtistProfile(ArtistDTO artist, Consumer<BufferedImage> callback) {
        if (artist.getProfileImage() == null && artist.getProfilePictureId() != null) {
            String imageId = artist.getProfilePictureId();

            // Check cache first
            if (ImageCache.containsImage(imageId)) {
                BufferedImage cachedImage = ImageCache.getImage(imageId);
                artist.setProfileImage(cachedImage);
                if (callback != null) {
                    callback.accept(cachedImage);
                }
                return;
            }

            // Load image asynchronously
            CompletableFuture.supplyAsync(() -> {
                try {
                    return googleDriveService.getBufferImage(imageId);
                } catch (Exception e) {
                    log.error("Error loading image for artist {}: {}", artist.getId(), e.getMessage());
                    return null;
                }
            }).thenAcceptAsync(image -> {
                if (image != null) {
                    ImageCache.putImage(imageId, image);
                    artist.setProfileImage(image);
                    if (callback != null) {
                        SwingUtilities.invokeLater(() -> callback.accept(image));
                    }
                }
            });
        }
    }

    public void populateUserProfile(UserDTO user) {
        populateUserProfile(user, null);
    }

    public void populateUserProfile(UserDTO user, Consumer<BufferedImage> callback) {
        if (user.getAvatarImage() == null && user.getAvatarId() != null) {
            String imageId = user.getAvatarId();

            // Check cache first
            if (ImageCache.containsImage(imageId)) {
                BufferedImage cachedImage = ImageCache.getImage(imageId);
                user.setAvatarImage(cachedImage);
                if (callback != null) {
                    callback.accept(cachedImage);
                }
                return;
            }

            // Load image asynchronously
            CompletableFuture.supplyAsync(() -> {
                try {
                    return googleDriveService.getBufferImage(imageId);
                } catch (Exception e) {
                    log.error("Error loading image for user {}: {}", user.getId(), e.getMessage());
                    return null;
                }
            }).thenAcceptAsync(image -> {
                if (image != null) {
                    ImageCache.putImage(imageId, image);
                    user.setAvatarImage(image);
                    if (callback != null) {
                        SwingUtilities.invokeLater(() -> callback.accept(image));
                    }
                }
            });
        }
    }
}