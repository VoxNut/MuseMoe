package com.javaweb.utils.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.utils.HttpClientProvider;
import com.javaweb.utils.Mp3Util;
import com.javaweb.utils.client.ApiConfig;
import com.javaweb.utils.client.client_service.*;
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