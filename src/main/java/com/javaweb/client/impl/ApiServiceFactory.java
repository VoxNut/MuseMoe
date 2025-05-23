package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// Factory design pattern
@Component
@RequiredArgsConstructor
public class ApiServiceFactory {
    private final ApiConfig apiConfig;
    private final WebClient webClient;

    public UserApiClient createUserApiClient() {
        return new UserApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    public SongApiClient createSongApiClient() {
        return new SongApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    public PlaylistApiClient createPlaylistApiClient() {
        return new PlaylistApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    public UserDownloadApiClient createUserDownloadApiClient() {
        return new UserDownloadApiClientImpl(
                createApiClient(),
                apiConfig
        );
    }

    public SongLikesApiClient createSonglikesdApiClient() {
        return new SongLikesApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    public PlayHistoryApiClient createPlayHistoryApiClient() {
        return new PlayHistoryApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    public SearchHistoryApiClient createSearchHistoryApiClient() {
        return new SearchHistoryApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    public ArtistApiClient createArtistApiClient() {
        return new ArtistApiClientImpl(
                createApiClient(),
                apiConfig
        );
    }

    public UserArtistFollowApiClient createUserArtistFollowApiClient() {
        return new UserArtistFollowApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    public AlbumApiClient createAlbumApiClient() {
        return new AlbumApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                apiConfig
        );
    }

    private ApiClient createApiClient() {
        return new ApiClient(webClient);
    }

    private UrlEncoder createUrlEncoder() {
        return new StandardUrlEncoder();
    }
}