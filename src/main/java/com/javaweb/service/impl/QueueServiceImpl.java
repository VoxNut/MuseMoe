package com.javaweb.service.impl;


import com.javaweb.entity.QueueEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.repository.QueueRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.QueueService;
import com.javaweb.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    @Override
    public boolean createNewQueue(Long songId) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));

            SongEntity song = songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("Song with id: " + songId + " not found!"));

            Integer nextPosition = queueRepository.findHighestPositionByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Position not found!")) + 1;

            QueueEntity queueEntity = new QueueEntity(user, song, nextPosition);

            queueRepository.save(queueEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
