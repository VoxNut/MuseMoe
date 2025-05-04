package com.javaweb.service.impl;

import com.javaweb.entity.StreamingMediaEntity;
import com.javaweb.repository.StreamingMediaRepository;
import com.javaweb.service.StreamingMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingMediaServiceImpl implements StreamingMediaService {
    private final StreamingMediaRepository streamingMediaRepository;

    public StreamingMediaEntity getOrCreateStreamingMedia(String fileId, String name,
                                                          String mimeType, Long size,
                                                          String webContentLink) {
        return streamingMediaRepository.findByGoogleDriveId(fileId)
                .orElse(new StreamingMediaEntity(
                        fileId,
                        name,
                        mimeType,
                        size,
                        webContentLink
                ));
    }
}
