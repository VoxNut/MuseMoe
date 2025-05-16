package com.javaweb.view.event;


import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;

// Mediator design pattern
@Slf4j
@Component
@RequiredArgsConstructor
public class MusicPlayerMediator {
    @Getter
    private final PlayerEventPublisher eventPublisher;

    private MusicPlayerMediator() {
        eventPublisher = new PlayerEventPublisher();
    }


    public void subscribeToPlayerEvents(PlayerEventListener listener) {
        eventPublisher.addObserver(listener);
    }

    public void unsubscribeFromPlayerEvents(PlayerEventListener listener) {
        eventPublisher.removeObserver(listener);
    }

    public void publishPlayerEvent(PlayerEvent event) {
        eventPublisher.publishEvent(event);
    }

    public void notifySongLoaded(SongDTO song) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.SONG_LOADED, song));
    }

    public void notifyPlaybackStarted(SongDTO song) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_STARTED, song));
    }

    public void notifyPlaybackPaused(SongDTO song) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_PAUSED, song));
    }

    public void notifyPlaybackStopped() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_STOPPED, null));
    }

    public void notifyPlaybackProgress(int frame, int timeInMillis) {
        final int[] data = new int[]{frame, timeInMillis};
        SwingUtilities.invokeLater(() -> {
            publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_PROGRESS, data));
        });
    }

    public void notifyRepeatModeChanged(Object repeatMode) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.REPEAT_MODE_CHANGED, repeatMode));
    }

    public void notifyPlaylistLoaded(PlaylistDTO playlist) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYLIST_LOADED, playlist));
    }

    public void notifyVolumeChanged(float volume) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.VOLUME_CHANGED, volume));
    }


    public void notifyAdOn() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.AD_ON, null));
    }

    public void notifyAdOff() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.AD_OFF, null));
    }

    public void notifyPlaybackSlider(int currentFrame) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.SLIDER_CHANGED, currentFrame));
    }

    public void notifyHomePagePlaybackSlider() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.HOME_PAGE_SLIDER_CHANGED, null));
    }

    public void notifySliderDragging(int frame, int timeInMillis) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.SLIDER_DRAGGING,
                new int[]{frame, timeInMillis}));
    }

    public void notifySongLikedChanged() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.SONG_LIKED_CHANGED, null));
    }


    public void notifyToggleCava(boolean isToggling) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.TOGGLE_CAVA, isToggling));
    }

    public void notifyLoadLocalSong() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.LOAD_LOCAL_SONG, null));
    }

    public void notifySongAlbum(String songAlbum) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.SONG_ALBUM, songAlbum));
    }


}
