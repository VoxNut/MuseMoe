package com.javaweb.view.custom.musicplayer;


import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.view.custom.musicplayer.event.PlayerEvent;
import com.javaweb.view.custom.musicplayer.event.PlayerEventListener;
import com.javaweb.view.custom.musicplayer.event.PlayerEventPublisher;

// Mediator design pattern
public class MusicPlayerMediator {
    private static MusicPlayerMediator instance;
    private PlayerEventPublisher eventPublisher;

    public MusicPlayerMediator() {

    }

    public static synchronized MusicPlayerMediator getInstance() {
        if (instance == null) {
            return new MusicPlayerMediator();
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

    public void notifyPlaybackStarted(SongDTO song) {
        publishPlayerEvent(new PlayerEvent(PlayerEvent.EventType.PLAYBACK_STARTED, song));
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

}
