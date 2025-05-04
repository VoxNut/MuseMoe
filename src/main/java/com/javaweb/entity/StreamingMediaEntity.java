package com.javaweb.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "streaming_media")
@Getter
@Setter
public class StreamingMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_drive_id", nullable = false)
    private String googleDriveId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "web_content_link")
    private String webContentLink;


    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    public StreamingMediaEntity() {
    }

    public StreamingMediaEntity(String googleDriveId, String name, String mimeType, Long sizeBytes, String webContentLink) {
        this.googleDriveId = googleDriveId;
        this.name = name;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.webContentLink = webContentLink;
        this.uploadedAt = LocalDateTime.now();
    }
}