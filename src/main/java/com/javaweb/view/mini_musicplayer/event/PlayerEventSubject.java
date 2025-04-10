package com.javaweb.view.mini_musicplayer.event;

public interface PlayerEventSubject {
    void addObserver(PlayerEventListener listener);

    void removeObserver(PlayerEventListener listener);

    void publishEvent(PlayerEvent event);
}
