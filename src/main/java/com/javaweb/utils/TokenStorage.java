package com.javaweb.utils;

import com.javaweb.model.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;


@Slf4j
public class TokenStorage {
    private static final String TOKEN_DIR = System.getProperty("user.home") + File.separator + ".musemoe";
    private static final String TOKEN_FILE = TOKEN_DIR + File.separator + "session.properties";

    /**
     * Saves the authentication token and user details to disk
     */
    public static void saveToken(String token, UserDTO user) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(TOKEN_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Save token and username to properties file
            Properties props = new Properties();
            props.setProperty("token", token);
            props.setProperty("username", user.getUsername());
            props.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));

            // Write properties to file
            String propsContent = propertiesToString(props);
            Files.write(Paths.get(TOKEN_FILE), propsContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            log.debug("Token saved to disk");
        } catch (IOException e) {
            log.error("Failed to save token to disk", e);
        }
    }

    /**
     * Loads the saved token from disk
     *
     * @return The token or null if no token exists or is invalid
     */
    public static String loadToken() {
        try {
            Path tokenPath = Paths.get(TOKEN_FILE);
            if (!Files.exists(tokenPath)) {
                return null;
            }

            Properties props = new Properties();
            props.load(Files.newBufferedReader(tokenPath, StandardCharsets.UTF_8));

            return props.getProperty("token");
        } catch (IOException e) {
            log.error("Failed to load token from disk", e);
            return null;
        }
    }

    /**
     * Clears any saved tokens
     */
    public static void clearToken() {
        try {
            Path tokenPath = Paths.get(TOKEN_FILE);
            if (Files.exists(tokenPath)) {
                Files.delete(tokenPath);
                log.debug("Token file deleted");
            }
        } catch (IOException e) {
            log.error("Failed to delete token file", e);
        }
    }

    /**
     * Convert properties to string
     */
    private static String propertiesToString(Properties props) {
        StringBuilder sb = new StringBuilder();
        for (String key : props.stringPropertyNames()) {
            sb.append(key).append("=").append(props.getProperty(key)).append("\n");
        }
        return sb.toString();
    }
}