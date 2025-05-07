package com.javaweb.view.navigation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NavigationManager {
    private static NavigationManager instance;

    private final List<NavigationItem> history = new ArrayList<>();
    private int currentIndex = -1;
    private List<NavigationListener> listeners = new ArrayList<>();

    private NavigationManager() {
        // Private constructor for singleton
    }

    public static synchronized NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }


    public void navigateTo(String destination, Object data) {
        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }

        history.add(new NavigationItem(destination, data));
        currentIndex++;

        updateNavigationState();
        log.debug("Navigated to: {} (history size: {})", destination, history.size());
    }


    public NavigationItem goBack() {
        if (!canGoBack()) {
            log.debug("Cannot go back - at beginning of history");
            return null;
        }

        currentIndex--;
        NavigationItem item = history.get(currentIndex);
        updateNavigationState();
        log.debug("Navigated back to: {}", item.destination);
        return item;
    }


    public NavigationItem goForward() {
        if (!canGoForward()) {
            log.debug("Cannot go forward - at end of history");
            return null;
        }

        currentIndex++;
        NavigationItem item = history.get(currentIndex);
        updateNavigationState();
        log.debug("Navigated forward to: {}", item.destination);
        return item;
    }

    public boolean canGoBack() {
        return currentIndex > 0;
    }


    public boolean canGoForward() {
        return currentIndex < history.size() - 1;
    }


    public NavigationItem getCurrentItem() {
        if (currentIndex >= 0 && currentIndex < history.size()) {
            return history.get(currentIndex);
        }
        return null;
    }


    public void addNavigationListener(NavigationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            updateNavigationState();
        }
    }


    public void removeNavigationListener(NavigationListener listener) {
        listeners.remove(listener);
    }


    public void clearHistory() {
        history.clear();
        currentIndex = -1;
        updateNavigationState();
    }

    private void updateNavigationState() {
        for (NavigationListener listener : listeners) {
            listener.onNavigationStateChanged(canGoBack(), canGoForward());
        }
    }

    public record NavigationItem(String destination, Object data) {
    }
}