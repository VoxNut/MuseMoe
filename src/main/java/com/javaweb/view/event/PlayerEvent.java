package com.javaweb.view.event;


public record PlayerEvent(EventType type, Object data) {
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
        SLIDER_CHANGED,
        HOME_PAGE_SLIDER_CHANGED,
        SLIDER_DRAGGING,
        LOADING_STARTED,
        LOADING_FINISHED,
        SONG_LIKED_CHANGED,
        SPECTRUM_DATA,
        SPECTRUM_STOP,
        TOGGLE_CAVA,
        LOAD_LOCAL_SONG,
        SONG_ALBUM,
        SEARCH_SONG,
    }

}