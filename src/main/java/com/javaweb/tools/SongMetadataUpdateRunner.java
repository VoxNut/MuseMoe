package com.javaweb.tools;

import org.springframework.boot.SpringApplication;

/**
 * Runner for the SongMetadataUpdateTool
 */
public class SongMetadataUpdateRunner {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "metadata-update-tool");
        SpringApplication.run(SongMetadataUpdateTool.SongMetadataUpdateApplication.class, args);
    }
}