package com.javaweb.view.mini_musicplayer.event;

import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.view.mini_musicplayer.MusicPlayer;

import java.io.IOException;

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


    public void loadSong(SongDTO song) {
        try {
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void prevSong() {
        try {
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


}