package com.javaweb.utils;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageMediaUtil {
    private final GoogleDriveService googleDriveService;

    public void populateSongImage(SongDTO song) {
        if (song.getSongImage() == null && song.getAlbumArtId() != null) {
            song.setSongImage(googleDriveService.getBufferImage(song.getAlbumArtId()));
        }
    }

    public void populateArtistProfile(ArtistDTO song) {
        if (song.getProfileImage() == null && song.getProfilePictureId() != null) {
            song.setProfileImage(googleDriveService.getBufferImage(song.getProfilePictureId()));
        }
    }

    public void populateUserProfile(UserDTO song) {
        if (song.getAvatarImage() == null && song.getAvatarId() != null) {
            song.setAvatarImage(googleDriveService.getBufferImage(song.getAvatarId()));
        }
    }

}
