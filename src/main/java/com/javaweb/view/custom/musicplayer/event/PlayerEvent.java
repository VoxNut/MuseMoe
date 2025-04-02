package com.javaweb.view.custom.musicplayer.event;

import lombok.Getter;

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
        VOLUME_CHANGED
    }

    @Getter
    private final EventType type;

    @Getter
    private final Object data;

    public PlayerEvent(EventType type, Object data) {
        this.type = type;
        this.data = data;
    }
}