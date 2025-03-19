package com.javaweb.utils;


import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientProvider {
    private static CloseableHttpClient httpClient;

    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(new BasicCookieStore())
                    .build();
        }
        return httpClient;
    }
}