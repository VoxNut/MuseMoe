package com.javaweb.view.event;

import com.javaweb.enums.RepeatMode;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.ImageMediaUtil;
import com.javaweb.view.MusicPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MusicPlayerFacade {

    private final MusicPlayer player;
    @Getter
    private final ImageMediaUtil imageMediaUtil;
    @Getter
    private final MusicPlayerMediator mediator;


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


    public boolean isPaused() {
        return player.isPaused();
    }

    public void setCurrentPlaylist(PlaylistDTO playlist) {
        player.setCurrentPlaylist(playlist);
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
        mediator.getEventPublisher().addObserver(listener);
    }


    public void unsubscribeFromPlayerEvents(PlayerEventListener listener) {
        mediator.getEventPublisher().removeObserver(listener);
    }

    public void notifySliderDragging(int value, int timeInMillis) {
        mediator.notifySliderDragging(value, timeInMillis);
    }

    public void populateSongImage(SongDTO songDTO, Consumer<BufferedImage> callback) {
        imageMediaUtil.populateSongImage(songDTO, callback);
    }

    public void populateArtistProfile(ArtistDTO artistDTO, Consumer<BufferedImage> callback) {
        imageMediaUtil.populateArtistProfile(artistDTO, callback);
    }

    public void populateUserProfile(UserDTO userDTO, Consumer<BufferedImage> callback) {
        imageMediaUtil.populateUserProfile(userDTO, callback);
    }


}