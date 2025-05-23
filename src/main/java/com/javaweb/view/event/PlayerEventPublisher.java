package com.javaweb.view.event;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlayerEventPublisher implements PlayerEventSubject {

    private final List<PlayerEventListener> listeners = new ArrayList<>();

    @Override
    public void addObserver(PlayerEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeObserver(PlayerEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void publishEvent(PlayerEvent event) {
        for (PlayerEventListener listener : listeners) {
            listener.onPlayerEvent(event);
        }
    }
}
