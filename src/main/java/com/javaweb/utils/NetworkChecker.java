package com.javaweb.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/*
 *
 * A utility for checking network connectivity.
 *
 * */
public class NetworkChecker {
    private static final String[] RELIABLE_HOSTS = {
            "https://www.google.com",
            "https://www.cloudflare.com",
            "https://www.amazon.com"
    };


    private static final int CONNECTION_TIMEOUT_MS = 3000;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static boolean lastKnownConnectionStatus = false;
    private static long lastCheckTime = 0;
    private static final long CHECK_INTERVAL_MS = 10000; // 10 seconds

    public static boolean isNetworkAvailable() {
        // Check from cache if checked recently
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime < CHECK_INTERVAL_MS) {
            return lastKnownConnectionStatus;
        }

        lastCheckTime = currentTime;

        for (String host : RELIABLE_HOSTS) {
            try {
                Future<Boolean> future = executorService.submit(() -> {
                    try {
                        URL url = new URL(host);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
                        connection.setReadTimeout(CONNECTION_TIMEOUT_MS);
                        connection.setRequestMethod("HEAD");
                        int responseCode = connection.getResponseCode();
                        return (200 <= responseCode && responseCode <= 399);
                    } catch (IOException exception) {
                        return false;
                    }
                });

                boolean isConnected = future.get(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (isConnected) {
                    lastKnownConnectionStatus = true;
                    return true;
                }
            } catch (Exception e) {
                // Try the next host
            }
        }

        lastKnownConnectionStatus = false;
        return false;
    }


    public static boolean isApiServerAvailable() {
        if (!isNetworkAvailable()) {
            return false;
        }

        try {
            String apiUrl = "http://localhost:8081/api/songs";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.setReadTimeout(CONNECTION_TIMEOUT_MS);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception e) {
            return false;
        }
    }

    public static void shutdown() {
        executorService.shutdown();
    }


}
