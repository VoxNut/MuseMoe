package com.javaweb.view.event;

public interface PlayerEventSubject {
    void addObserver(PlayerEventListener listener);

    void removeObserver(PlayerEventListener listener);

    void publishEvent(PlayerEvent event);
}
