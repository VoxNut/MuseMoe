package com.javaweb.view.event;

import com.javaweb.enums.PlaylistSourceType;
import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.*;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.ImageMediaUtil;
import com.javaweb.utils.LocalSongManager;
import com.javaweb.view.MusicPlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MusicPlayerFacade {

    private final MusicPlayer player;
    private final ImageMediaUtil imageMediaUtil;
    private final MusicPlayerMediator mediator;
    private final MusicPlayer musicPlayer;


    private List<SongDTO> songQueue = new ArrayList<>();
    private boolean isQueueActive = false;


    public void loadLocalSong(SongDTO song) {
        if (isHavingAd()) return;
        PlaylistDTO playlistDTO = convertSongListToPlaylist(LocalSongManager.getDownloadedSongs(), "Local Songs");
        setPlaylistContext(playlistDTO, PlaylistSourceType.LOCAL);
        SwingUtilities.invokeLater(() -> player.loadLocalSong(song));
    }

    public void loadSongWithContext(SongDTO song, PlaylistDTO playlist, PlaylistSourceType sourceType) {
        if (isHavingAd()) return;
        clearQueue();
        setPlaylistContext(playlist, sourceType);
        player.loadSong(song);
    }

    public void playQueueFrom(int position) {
        if (isHavingAd()) return;
        if (isQueueActive && position >= 0 && position < songQueue.size()) {
            for (int i = 0; i < position; i++) {
                songQueue.removeFirst();
            }
            SongDTO song = songQueue.getFirst();
            player.loadSong(song);
            mediator.notifyQueueUpdated(songQueue);
        }
    }

    public void clearQueue() {
        songQueue.clear();
        isQueueActive = false;
        musicPlayer.setCurrentPlaylist(null);
        mediator.notifyQueueUpdated(songQueue);
    }

    public List<SongDTO> getSongsByArtist(String artistName, int limit) {
        return CommonApiUtil.fetchSongsByArtist(artistName, limit);
    }


    public PlaylistDTO convertSongListToPlaylist(List<SongDTO> songs, String name) {
        PlaylistDTO playlist = new PlaylistDTO();
        playlist.setName(name);
        playlist.setSongs(songs);
        return playlist;
    }

    private void setPlaylistContext(PlaylistDTO playlist, PlaylistSourceType sourceType) {
        if (playlist != null) {
            playlist.setSourceType(sourceType);
        }
        player.setCurrentPlaylist(playlist);
    }


    public void addToQueueNext(SongDTO song) {
        if (!isQueueActive) {
            this.songQueue = new ArrayList<>();
            songQueue.add(getCurrentSong());
            this.isQueueActive = true;
        }

        songQueue.add(song);
        PlaylistDTO updatedQueue = convertSongListToPlaylist(songQueue, "Current Queue");
        updatedQueue.setSourceType(PlaylistSourceType.QUEUE);
        player.setCurrentPlaylist(updatedQueue);
        mediator.notifyQueueUpdated(songQueue);
    }


    public void pauseSong() {
        try {
            if (isHavingAd()) return;
            player.pauseSong();
            mediator.notifyPlaybackPaused(getCurrentSong());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public void stopSong() {
        musicPlayer.stopSong();
    }


    public void playCurrentSong() {
        if (isHavingAd()) return;
        player.playCurrentSong();
        mediator.notifyPlaybackStarted(getCurrentSong());
    }

    public void nextSong() {
        if (isHavingAd()) return;
        try {
            if (isQueueActive) {
                if (songQueue != null && !songQueue.isEmpty()) {
                    songQueue.removeFirst();
                    player.loadSong(songQueue.getFirst());
                    mediator.notifyQueueUpdated(songQueue);
                    return;
                }

            }

            player.nextSong();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public PlaylistDTO getCurrentPlaylist() {
        return player.getCurrentPlaylist();
    }

    public void prevSong() {
        try {
            if (isHavingAd()) {
                return;
            }
            player.prevSong();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void replayFiveSeconds() {
        try {
            player.replayFiveSeconds();
            mediator.notifyPlaybackSlider(player.getCurrentFrame());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void shufflePlaylist() {
        try {
            if (isHavingAd()) return;
            player.shufflePlaylist();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void setVolume(int gain) {
        player.setVolume(gain);
        mediator.notifyVolumeChanged(gain);
    }

    public void cycleRepeatMode() {
        try {
            if (isHavingAd()) return;
            player.cycleRepeatMode();
            mediator.notifyRepeatModeChanged(getRepeatMode());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public boolean isPaused() {
        return player.isPaused();
    }

//    public void setCurrentPlaylist(PlaylistDTO playlist) {
//        if (playlist == null) {
//            setPlaylistContext(null, null);
//            isQueueActive = false;
//            return;
//        }
//        PlaylistSourceType sourceType;
//
//        if ("Liked Songs".equals(playlist.getName())) {
//            sourceType = PlaylistSourceType.LIKED_SONGS;
//        } else if ("Current Queue".equals(playlist.getName()) || isQueueActive) {
//            sourceType = PlaylistSourceType.QUEUE;
//        } else {
//            sourceType = PlaylistSourceType.USER_PLAYLIST;
//        }
//
//        setPlaylistContext(playlist, sourceType);
//    }


    public SongDTO getCurrentSong() {
        return player.getCurrentSong();
    }

    public void setCurrentSong(SongDTO song) {
        player.setCurrentSong(song);
    }


    public RepeatMode getRepeatMode() {
        return player.getRepeatMode();
    }

    public boolean isHavingAd() {
        return player.isHavingAd();
    }

    public int getCalculatedFrame() {
        return (int) player.getCalculatedFrame();
    }

    public void setCurrentTimeInMilli(int timeInMilli) {
        player.setCurrentTimeInMilli(timeInMilli);
    }

    public void setCurrentFrame(int timeInMilli) {
        player.setCurrentFrame(timeInMilli);
    }

    public void notifySongLiked() {
        mediator.notifySongLikedChanged();
    }

    public float getCurrentVolumeGain() {
        return player.getCurrentVolumeGain();
    }

    public void notifyToggleCava(boolean isToggle) {
        mediator.notifyToggleCava(isToggle);
    }

    public void subscribeToPlayerEvents(PlayerEventListener listener) {
        mediator.subscribeToPlayerEvents(listener);
    }


    public void unsubscribeFromPlayerEvents(PlayerEventListener listener) {
        mediator.unsubscribeFromPlayerEvents(listener);
    }

    public void notifySliderDragging(int value, int timeInMillis) {
        mediator.notifySliderDragging(value, timeInMillis);
    }

    public void populateSongImage(SongDTO songDTO, Consumer<BufferedImage> callback) {
        imageMediaUtil.populateSongImage(songDTO, callback);
    }

    public void populateAlbumImage(AlbumDTO albumDTO, Consumer<BufferedImage> callback) {
        imageMediaUtil.populateAlbumImage(albumDTO, callback);
    }

    public void populateArtistProfile(ArtistDTO artistDTO, Consumer<BufferedImage> callback) {
        imageMediaUtil.populateArtistProfile(artistDTO, callback);
    }

    public void populateUserProfile(UserDTO userDTO, Consumer<BufferedImage> callback) {
        imageMediaUtil.populateUserProfile(userDTO, callback);
    }

    public List<SongDTO> getQueueSongs() {
        return isQueueActive ? new ArrayList<>(songQueue) : new ArrayList<>();
    }

}