package com.javaweb.utils;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientProvider {
    private static CloseableHttpClient httpClient;

    public static CloseableHttpClient getHttpClient() {
        int timeout = 5000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }
}