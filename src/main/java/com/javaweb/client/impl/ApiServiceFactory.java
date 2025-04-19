package com.javaweb.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.*;
import com.javaweb.utils.HttpClientProvider;
import com.javaweb.utils.Mp3Util;
import org.springframework.stereotype.Component;

@Component
public class ApiServiceFactory {
    private final ApiConfig apiConfig;
    private final ObjectMapper objectMapper;
    private final Mp3Util mp3Util;

    public ApiServiceFactory(ApiConfig apiConfig, Mp3Util mp3Util) {
        this.apiConfig = apiConfig;
        this.mp3Util = mp3Util;
        objectMapper = new ObjectMapper();
    }

    //Factory methods
    public UserApiClient createUserApiClient() {
        return new UserApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig
        ) {
        };
    }


    public SongApiClient createSongApiClient() {
        return new SongApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig,
                mp3Util
        );
    }

    public PlaylistApiClient createPlaylistApiClient() {
        return new PlaylistApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig,
                mp3Util
        );
    }

    public UserDownloadApiClient createUserDownloadApiClient() {
        return new UserDownloadApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig,
                mp3Util
        );
    }


    public SongLikesApiClient createSonglikesdApiClient() {
        return new SongLikesApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig,
                mp3Util
        );
    }

    public PlayHistoryApiClient createPlayHistoryApiClient() {
        return new PlayHistoryApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig,
                mp3Util
        );
    }

    public SearchHistoryApiClient createSearchHistoryApiClient() {
        return new SearchHistoryApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig,
                mp3Util
        );
    }

    public UserArtistFollowApiClient createUserArtistFollowApiClient() {
        return new UserArtistFollowApiClientImpl(
                createApiClient(),
                createUrlEncoder(),
                createResponseParser(),
                apiConfig,
                mp3Util
        );
    }


    private ApiClient createApiClient() {
        return new HttpApiClient(HttpClientProvider.getHttpClient());
    }

    private UrlEncoder createUrlEncoder() {
        return new StandardUrlEncoder();
    }

    private ResponseParser createResponseParser() {
        return new JsonResponseParser(objectMapper);
    }
}