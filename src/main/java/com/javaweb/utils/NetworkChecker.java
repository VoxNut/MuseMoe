package com.javaweb.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A utility for checking network connectivity.
 */
public class NetworkChecker {
    private static final String[] RELIABLE_HOSTS = {
            "https://www.google.com",
            "https://www.cloudflare.com",
            "https://www.amazon.com"
    };

    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS))
            .build();

    /**
     * Checks if the network is available by attempting to connect to reliable hosts
     *
     * @return true if the network is available, false otherwise
     */
    public static boolean isNetworkAvailable() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<Boolean> future = executor.submit(() -> {
                for (String host : RELIABLE_HOSTS) {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(host))
                                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                .timeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS))
                                .build();

                        HttpResponse<Void> response = httpClient.send(request,
                                HttpResponse.BodyHandlers.discarding());

                        if (response.statusCode() >= 200 && response.statusCode() < 400) {
                            return true;
                        }
                    } catch (Exception e) {
                        // Try the next host
                    }
                }
                return false;
            });

            return future.get(CONNECTION_TIMEOUT_MS * 2, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Checks if the API server is available
     *
     * @return true if the API server is available, false otherwise
     */
    public static boolean isApiServerAvailable() {
        if (!isNetworkAvailable()) {
            return false;
        }

        try {
            // Using the root path is safer as a health check
            URI uri = URI.create("http://localhost:8081/");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS))
                    .build();

            try {
                HttpResponse<Void> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.discarding());

                // Any valid HTTP response indicates the server is up
                return true;
            } catch (IOException e) {
                // Connection issues indicate server is down
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a specific API endpoint is accessible and returns expected status
     *
     * @param endpoint The API endpoint to check (e.g., "/api/songs")
     * @return true if the endpoint is accessible with 2xx or 3xx response, false otherwise
     */
    public static boolean isApiEndpointAccessible(String endpoint) {
        if (!isNetworkAvailable()) {
            return false;
        }

        try {
            String baseUrl = "http://localhost:8081";
            if (!endpoint.startsWith("/")) {
                endpoint = "/" + endpoint;
            }

            URI uri = URI.create(baseUrl + endpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS))
                    .build();

            HttpResponse<Void> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.discarding());

            return (response.statusCode() >= 200 && response.statusCode() < 400);
        } catch (Exception e) {
            return false;
        }
    }
}