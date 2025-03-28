package com.javaweb.utils.client;

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


    /**
     * Build a full URL for a user endpoint
     */
    public String buildUserUrl(String path) {
        return baseUrl + userEndpoint + path;
    }

    /**
     * Build a full URL for a song endpoint
     */
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
}