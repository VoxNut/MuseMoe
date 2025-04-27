package com.javaweb.view.mini_musicplayer.event;

import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.CommonApiUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.view.MiniMusicPlayerGUI;
import com.javaweb.view.MusicPlayer;
import com.javaweb.view.theme.ThemeManager;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

//Replay, pause, play current, cycle repeat, set column
public class MusicPlayerFacade {

    private static MusicPlayerFacade instance;
    private final MusicPlayer player;
    private final MusicPlayerMediator mediator;


    private MusicPlayerFacade() {
        this.player = new MusicPlayer();
        this.mediator = MusicPlayerMediator.getInstance();
    }

    public static synchronized MusicPlayerFacade getInstance() {
        if (instance == null) {
            instance = new MusicPlayerFacade();
        }
        return instance;
    }

    private void updateThemeFromSong(SongDTO song) {
        if (song != null && song.getSongImage() != null) {
            try {
                MiniMusicPlayerGUI.getInstance();
                Color[] themeColors = GuiUtil.extractThemeColors(song.getSongImage());
                ThemeManager.getInstance().setThemeColors(
                        themeColors[0],
                        themeColors[1],
                        themeColors[2]
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void loadSong(SongDTO song) {
        try {
            updateThemeFromSong(song);
            player.loadSong(song);
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public void pauseSong() {
        try {
            player.pauseSong();
            mediator.notifyPlaybackPaused();
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public void stopSong() {
        try {
            player.stopSong();
//            mediator.notifyPlaybackStopped();
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public void playCurrentSong() {
        player.playCurrentSong();
        mediator.notifyPlaybackStarted();
    }

    public void nextSong() {
        try {
            player.nextSong();
            updateThemeFromSong(getCurrentSong());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void prevSong() {
        try {
            player.prevSong();
            updateThemeFromSong(getCurrentSong());
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
            updateThemeFromSong(getCurrentSong());
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


    public int getCurrentTimeInMilli() {
        return player.getCurrentTimeInMilli();
    }

    public int getCurrentFrame() {
        return player.getCurrentFrame();
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    public void setCurrentPlaylist(PlaylistDTO playlist) {
        player.setCurrentPlaylist(playlist);

        // Preload the next few songs if we have a playlist
        if (playlist != null && !playlist.getSongs().isEmpty()) {
            int currentIndex = 0;
            int playlistSize = playlist.size();

            // Preload up to 3 songs ahead in a background thread
            CompletableFuture.runAsync(() -> {
                for (int i = 0; i < 3 && i < playlistSize; i++) {
                    SongDTO songToPreload = playlist.getSongAt((currentIndex + i) % playlistSize);

                    // Only enrich if not already processed
                    if (songToPreload.getMp3File() == null) {
                        try {
                            SongDTO fullSong = CommonApiUtil.fetchSongById(songToPreload.getId());
                            // Update the playlist song with the enriched data
                            updatePlaylistSong(playlist, currentIndex + i, fullSong);
                        } catch (Exception e) {
                            // Just log and continue
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }


    private void updatePlaylistSong(PlaylistDTO playlist, int index, SongDTO updatedSong) {
        if (index < playlist.getSongs().size()) {
            playlist.getSongs().set(index, updatedSong);
        }
    }

    public SongDTO getCurrentSong() {
        return player.getCurrentSong();
    }

    public PlaylistDTO getCurrentPlaylist() {
        return player.getCurrentPlaylist();
    }

    public RepeatMode getRepeatMode() {
        return player.getRepeatMode();
    }

    public boolean isHavingAd() {
        return player.isHavingAd();
    }

    public int getCalculatedFrame() {
        return player.getCalculatedFrame();
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


}