package com.javaweb.service;

import com.javaweb.entity.StreamingMediaEntity;

public interface StreamingMediaService {
    StreamingMediaEntity getOrCreateStreamingMedia(String fileId, String name,
                                                   String mimeType, Long size,
                                                   String webContentLink);
}
