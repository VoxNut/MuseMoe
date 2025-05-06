package com.javaweb.utils;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ImageCache {
    private static final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 200; // Adjust based on memory constraints

    public static BufferedImage getImage(String imageId) {
        return cache.get(imageId);
    }

    public static void putImage(String imageId, BufferedImage image) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            if (!cache.isEmpty()) {
                String keyToRemove = cache.keySet().iterator().next();
                cache.remove(keyToRemove);
                log.debug("Cache full, removed image: {}", keyToRemove);
            }
        }
        cache.put(imageId, image);
    }

    public static boolean containsImage(String imageId) {
        return cache.containsKey(imageId);
    }

    public static void clearCache() {
        cache.clear();
    }
}