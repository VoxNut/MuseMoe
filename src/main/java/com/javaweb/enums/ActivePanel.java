package com.javaweb.enums;

public enum ActivePanel implements BaseEnum {
    HOME("Home"),
    VISUALIZER("Visualizer"),
    COMMITS("Commits"),
    INSTRUCTIONS("Instructions"),
    QUEUE("Queue"),
    ARTIST_UPLOAD("Artist Upload"),
    ADMIN_STATISTICS("Admin Statistics"),
    SEARCH_RESULTS("Search Results"),
    ALBUM_VIEW("Album View"),
    SONG_DETAILS("Song Details"),
    ARTIST_PROFILE("Artist Profile"),
    ACCOUNT_SETTINGS("Account Settings");

    private final String activePanelName;

    ActivePanel(String activePanelName) {
        this.activePanelName = activePanelName;
    }

    @Override
    public String getValue() {
        return this.activePanelName;
    }


}
