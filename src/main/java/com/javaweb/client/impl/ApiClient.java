package com.javaweb.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.javaweb.view.user.UserSessionManager;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private final WebClient webClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ApiClient(WebClient webClient) {
        this.webClient = webClient;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

    }

    private WebClient.RequestHeadersSpec<?> addAuthHeader(WebClient.RequestHeadersSpec<?> spec) {
        String token = UserSessionManager.getInstance().getAuthToken();
        if (token != null && !token.isEmpty()) {
            return spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return spec;
    }

    private HttpEntity<?> createAuthenticatedEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String token = UserSessionManager.getInstance().getAuthToken();
        if (token != null && !token.isEmpty()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return new HttpEntity<>(body, headers);
    }

    public <T> T get(String url, Class<T> responseType) {
        return addAuthHeader(webClient.get().uri(url))
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public <T> List<T> getList(String url, Class<T> elementType) {
        try {
            String response = addAuthHeader(webClient.get().uri(url))
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.empty();
                    })
                    .block();

            if (response == null) {
                return new ArrayList<>();
            }

            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementType);

            return objectMapper.readValue(response, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public <T> T post(String url, Object body, Class<T> responseType) {
        return addAuthHeader(webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body))
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public <T> T put(String url, Object body, Class<T> responseType) {
        return addAuthHeader(webClient.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body))
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public <T> T delete(String url, Object jsonBody, Class<T> responseType) {
        return addAuthHeader(webClient.method(HttpMethod.DELETE)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody))
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public <T> T delete(String url, Class<T> responseType) {
        return addAuthHeader(webClient.delete()
                .uri(url))
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public <T> T postMultipart(String url, Map<String, Object> parts, Class<T> responseType) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            if (parts != null) {
                parts.forEach((key, value) -> {
                    if (value != null) {
                        if (value instanceof List<?> && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof MultipartFile) {
                            // Handle list of MultipartFiles
                            @SuppressWarnings("unchecked")
                            List<MultipartFile> files = (List<MultipartFile>) value;

                            for (int i = 0; i < files.size(); i++) {
                                MultipartFile file = files.get(i);
                                try {
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.setContentType(MediaType.parseMediaType(file.getContentType()));

                                    // Create a resource from the file bytes - NOT from the input stream
                                    org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                                        @Override
                                        public String getFilename() {
                                            return file.getOriginalFilename();
                                        }
                                    };

                                    // Use array-style parameter names for lists (mp3Files[0], mp3Files[1], etc.)
                                    body.add(key + "[" + i + "]", new HttpEntity<>(resource, headers));
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to process file upload", e);
                                }
                            }
                        } else if (value instanceof MultipartFile) {
                            // Handle single MultipartFile
                            MultipartFile file = (MultipartFile) value;
                            try {
                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.parseMediaType(file.getContentType()));

                                org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                                    @Override
                                    public String getFilename() {
                                        return file.getOriginalFilename();
                                    }
                                };

                                body.add(key, new HttpEntity<>(resource, headers));
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to process file upload", e);
                            }
                        } else {
                            body.add(key, value);
                        }
                    }
                });
            }

            // Create headers with authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String token = UserSessionManager.getInstance().getAuthToken();
            if (token != null && !token.isEmpty()) {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    responseType
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error in postMultipart request: " + e.getMessage(), e);
        }
    }

    public <T> T putMultipart(String url, Map<String, Object> parts, Class<T> responseType) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            if (parts != null) {
                parts.forEach((key, value) -> {
                    if (value != null) {
                        if (value instanceof List<?> && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof MultipartFile) {
                            // Handle list of MultipartFiles
                            @SuppressWarnings("unchecked")
                            List<MultipartFile> files = (List<MultipartFile>) value;

                            for (int i = 0; i < files.size(); i++) {
                                MultipartFile file = files.get(i);
                                try {
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.setContentType(MediaType.parseMediaType(file.getContentType()));

                                    // Create a resource from the file bytes - NOT from the input stream
                                    org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                                        @Override
                                        public String getFilename() {
                                            return file.getOriginalFilename();
                                        }
                                    };

                                    // Use array-style parameter names for lists (mp3Files[0], mp3Files[1], etc.)
                                    body.add(key + "[" + i + "]", new HttpEntity<>(resource, headers));
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to process file upload", e);
                                }
                            }
                        } else if (value instanceof MultipartFile) {
                            // Handle single MultipartFile
                            MultipartFile file = (MultipartFile) value;
                            try {
                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.parseMediaType(file.getContentType()));

                                org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                                    @Override
                                    public String getFilename() {
                                        return file.getOriginalFilename();
                                    }
                                };

                                body.add(key, new HttpEntity<>(resource, headers));
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to process file upload", e);
                            }
                        } else {
                            body.add(key, value);
                        }
                    }
                });
            }

            // Create headers with authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String token = UserSessionManager.getInstance().getAuthToken();
            if (token != null && !token.isEmpty()) {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Use PUT instead of POST
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    responseType
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error in putMultipart request: " + e.getMessage(), e);
        }
    }


    public String get(String url) {
        return addAuthHeader(webClient.get().uri(url))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public String post(String url, String jsonBody) {
        return addAuthHeader(webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public String put(String url, String jsonBody) {
        return addAuthHeader(webClient.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public String delete(String url) {
        return addAuthHeader(webClient.delete()
                .uri(url))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }

    public byte[] getAsByteArray(String url) {
        return addAuthHeader(webClient.get().uri(url))
                .retrieve()
                .bodyToMono(byte[].class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }
}