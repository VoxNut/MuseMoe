package com.javaweb.view.event;

import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.*;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.ImageMediaUtil;
import com.javaweb.utils.LocalSongManager;
import com.javaweb.view.MusicPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MusicPlayerFacade {

    private final MusicPlayer player;
    private final ImageMediaUtil imageMediaUtil;
    private final MusicPlayerMediator mediator;

    public enum PlaylistSourceType {
        USER_PLAYLIST,
        ALBUM,
        LIKED_SONGS,
        QUEUE,
        SEARCH_RESULTS,
        NONE,
        LOCAL,
        POPULAR
    }

    @Getter
    private PlaylistSourceType currentPlaylistSourceType = PlaylistSourceType.NONE;

    private List<SongDTO> songQueue = new ArrayList<>();
    private int queuePosition = 0;
    private boolean isQueueActive = false;


    public void loadLocalSong(SongDTO song) {
        PlaylistDTO playlistDTO = convertSongListToPlaylist(LocalSongManager.getDownloadedSongs(), "Local Songs");
        setPlaylistContext(playlistDTO, PlaylistSourceType.LOCAL);
        player.loadLocalSong(song);
    }


    public void loadSong(SongDTO song) {
        if (isQueueActive && songQueue.contains(song)) {
            int index = songQueue.indexOf(song);
            queuePosition = index;
            setPlaylistContext(convertSongListToPlaylist(songQueue, "Queue"), PlaylistSourceType.QUEUE);
            player.loadSong(song);
            return;
        }

        // Try to find from different sources
        PlaylistDTO playlist = null;
        PlaylistSourceType sourceType = PlaylistSourceType.NONE;

        // Check user playlists first
        PlaylistDTO foundPlaylist = CommonApiUtil.fetchPlaylistContainsThisSong(song.getId());
        if (foundPlaylist != null) {
            playlist = foundPlaylist;
            sourceType = PlaylistSourceType.USER_PLAYLIST;
        }
        // If not found, check if it's in an album
        else {
            AlbumDTO foundAlbum = CommonApiUtil.fetchAlbumContainsThisSong(song.getId());
            if (foundAlbum != null) {
                PlaylistDTO convertedAlbum = new PlaylistDTO();
                convertedAlbum.setName(foundAlbum.getTitle());
                convertedAlbum.setSongs(foundAlbum.getSongDTOS());
                playlist = convertedAlbum;
                sourceType = PlaylistSourceType.ALBUM;
            }
            // Check if it's a liked song
            else if (CommonApiUtil.checkSongLiked(song.getId())) {
                List<SongDTO> likedSongs = CommonApiUtil.findAllSongLikes().stream()
                        .map(SongLikesDTO::getSongDTO)
                        .toList();

                PlaylistDTO likedSongsPlaylist = new PlaylistDTO();
                likedSongsPlaylist.setName("Liked Songs");
                likedSongsPlaylist.setSongs(likedSongs);
                playlist = likedSongsPlaylist;
                sourceType = PlaylistSourceType.LIKED_SONGS;
            }
        }

        setPlaylistContext(playlist, sourceType);
        player.loadSong(song);

    }

    public void loadSongWithContext(SongDTO song, PlaylistDTO playlist, PlaylistSourceType sourceType) {
        setPlaylistContext(playlist, sourceType);
        player.loadSong(song);
    }

    public void playQueue(List<SongDTO> songs, int startIndex) {
        if (songs == null || songs.isEmpty()) return;

        // Set up the queue
        this.songQueue = new ArrayList<>(songs);
        this.queuePosition = Math.min(startIndex, songs.size() - 1);
        this.isQueueActive = true;

        // Create a queue playlist
        PlaylistDTO queuePlaylist = convertSongListToPlaylist(songs, "Current Queue");
        setPlaylistContext(queuePlaylist, PlaylistSourceType.QUEUE);

        player.loadSong(songs.get(queuePosition));

    }

    public void addToQueue(SongDTO song) {
        addToQueue(Collections.singletonList(song));
    }

    public PlaylistDTO convertSongListToPlaylist(List<SongDTO> songs, String name) {
        PlaylistDTO playlist = new PlaylistDTO();
        playlist.setName(name);
        playlist.setSongs(songs);
        return playlist;
    }

    private void setPlaylistContext(PlaylistDTO playlist, PlaylistSourceType sourceType) {
        this.currentPlaylistSourceType = sourceType;
        player.setCurrentPlaylist(playlist);
    }


    public void addToQueue(List<SongDTO> songs) {
        if (songs == null || songs.isEmpty()) return;

        // Create queue if doesn't exist
        if (!isQueueActive) {
            songQueue = new ArrayList<>();
            isQueueActive = true;
        }

        // Add the songs
        songQueue.addAll(songs);

        // Update the player's playlist if currently in queue mode
        if (currentPlaylistSourceType == PlaylistSourceType.QUEUE) {
            PlaylistDTO updatedQueue = convertSongListToPlaylist(songQueue, "Current Queue");
            player.setCurrentPlaylist(updatedQueue);
        }
    }

    public void pauseSong() {
        try {
            player.pauseSong();
            mediator.notifyPlaybackPaused(getCurrentSong());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }


    public void playCurrentSong() {
        player.playCurrentSong();
        mediator.notifyPlaybackStarted(getCurrentSong());
    }

    public void nextSong() {
        try {
            if (currentPlaylistSourceType == PlaylistSourceType.QUEUE && isQueueActive) {
                if (queuePosition < songQueue.size() - 1) {
                    queuePosition++;
                    player.loadSong(songQueue.get(queuePosition));
                    return;
                } else if (player.getRepeatMode() == RepeatMode.REPEAT_ALL) {
                    queuePosition = 0;
                    player.loadSong(songQueue.get(queuePosition));
                    return;
                }
            }

            player.nextSong();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void prevSong() {
        try {
            if (currentPlaylistSourceType == PlaylistSourceType.QUEUE && isQueueActive) {
                if (queuePosition > 0) {
                    queuePosition--;
                    player.loadSong(songQueue.get(queuePosition));
                    return;
                } else if (player.getRepeatMode() == RepeatMode.REPEAT_ALL) {
                    queuePosition = songQueue.size() - 1;
                    player.loadSong(songQueue.get(queuePosition));
                    return;
                }
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
            player.shufflePlaylist();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void setVolume(float gain) {
        player.setVolume(gain);
        mediator.notifyVolumeChanged(gain);
    }

    public void cycleRepeatMode() {
        try {
            player.cycleRepeatMode();
            mediator.notifyRepeatModeChanged(getRepeatMode());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public boolean isPaused() {
        return player.isPaused();
    }

    public void setCurrentPlaylist(PlaylistDTO playlist) {
        if (playlist == null) {
            setPlaylistContext(null, PlaylistSourceType.NONE);
            isQueueActive = false;
            return;
        }
        PlaylistSourceType sourceType;

        if ("Liked Songs".equals(playlist.getName())) {
            sourceType = PlaylistSourceType.LIKED_SONGS;
        } else if ("Current Queue".equals(playlist.getName()) || isQueueActive) {
            sourceType = PlaylistSourceType.QUEUE;
        } else {
            sourceType = PlaylistSourceType.USER_PLAYLIST;
        }

        setPlaylistContext(playlist, sourceType);
    }


    public SongDTO getCurrentSong() {
        return player.getCurrentSong();
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


}