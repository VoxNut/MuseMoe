package com.javaweb.tools;

import org.springframework.boot.SpringApplication;

public class SongRelationshipUpdateRunner {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "relationship-update-tool");
        SpringApplication.run(SongRelationshipUpdateTool.SongRelationshipUpdateApplication.class, args);
    }
}