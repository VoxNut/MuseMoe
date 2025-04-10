package com.javaweb.view.mini_musicplayer.event;


import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;

import java.awt.*;

// Mediator design pattern
public class MusicPlayerMediator {
    private static MusicPlayerMediator instance;
    private final PlayerEventPublisher eventPublisher;

    private MusicPlayerMediator() {
        eventPublisher = new PlayerEventPublisher();
    }


    public static synchronized MusicPlayerMediator getInstance() {
        if (instance == null) {
            instance = new MusicPlayerMediator();
        }
        return instance;
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

    public void notifyPlaybackStarted() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_STARTED, null));
    }

    public void notifyPlaybackPaused() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_PAUSED, null));
    }

    public void notifyPlaybackStopped() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_STOPPED, null));
    }

    public void notifyPlaybackProgress(int frame, int timeInMillis) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_PROGRESS,
                new int[]{frame, timeInMillis}));
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

    public void notifyHeartChanged() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.HEART_CHANGED, null));
    }

    public void notifyAdOn() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.AD_ON, null));
    }

    public void notifyAdOff() {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.AD_OFF, null));
    }

    public void notifyColorsChanged(Color backgroundColor, Color textColor, Color accentColor) {
        Color[] colors = new Color[]{backgroundColor, textColor, accentColor};
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.COLORS_CHANGED, colors));
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


}
