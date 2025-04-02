package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.PlayHistoryApiClient;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayHistoryApiClientImpl implements PlayHistoryApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;
    private final Mp3Util mp3Util;


    @Override
    public Boolean createNewPlayHistory(Long songId) {
        try {
            String url = apiConfig.buildPlayHistoryUrl("/create");
            String responseEntity = apiClient.postWithFormParam(url, "songId", songId);
            return responseParser.parseObject(responseEntity, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
