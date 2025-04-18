package com.javaweb.client;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Getter
public class ApiConfig {
    @Value("${api.base.url:http://localhost:8081}")
    private String baseUrl;

    @Value("${api.user.endpoint:/api/user}")
    private String userEndpoint;

    @Value("${api.songs.endpoint:/api/songs}")
    private String songsEndpoint;

    @Value("${api.playlists.endpoint:/api/playlists}")
    private String playListsEndpoint;

    @Value("${api.user_downloads.endpoint:/api/user_downloads}")
    private String songDownloadsEndpoint;

    @Value("${api.song_likes.endpoint:/api/song_likes}")
    private String songLikesEndpoint;

    @Value("${api.play_history.endpoint:/api/play_history}")
    private String playHistoryEndpoint;

    @Value("${api.search_history.endpoint:/api/search_history}")
    private String searchHistoryEndpoint;


    public String buildUserUrl(String path) {
        return baseUrl + userEndpoint + path;
    }


    public String buildSongUrl(String path) {
        return baseUrl + songsEndpoint + path;
    }

    public String buildPlaylistUrl(String path) {
        return baseUrl + playListsEndpoint + path;
    }

    public String buildUserDownloadUrl(String path) {
        return baseUrl + songDownloadsEndpoint + path;
    }


    public String buildSongLikesUrl(String path) {
        return baseUrl + songLikesEndpoint + path;
    }

    public String buildPlayHistoryUrl(String path) {
        return baseUrl + playHistoryEndpoint + path;
    }

    public String buildSearchHistoryUrl(String path) {
        return baseUrl + searchHistoryEndpoint + path;
    }
}