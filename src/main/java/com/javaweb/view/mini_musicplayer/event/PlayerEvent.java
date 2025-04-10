package com.javaweb.view.mini_musicplayer.event;

import lombok.Getter;

@Getter
public class PlayerEvent {
    public enum EventType {
        SONG_LOADED,
        PLAYBACK_STARTED,
        PLAYBACK_PAUSED,
        PLAYBACK_STOPPED,
        PLAYBACK_FINISHED,
        PLAYBACK_PROGRESS,
        REPEAT_MODE_CHANGED,
        PLAYLIST_LOADED,
        VOLUME_CHANGED,
        HEART_CHANGED,
        AD_ON,
        AD_OFF,
        COLORS_CHANGED,
        SLIDER_CHANGED,
        HOME_PAGE_SLIDER_CHANGED
    }

    private final EventType type;

    private final Object data;

    public PlayerEvent(EventType type, Object data) {
        this.type = type;
        this.data = data;
    }

}