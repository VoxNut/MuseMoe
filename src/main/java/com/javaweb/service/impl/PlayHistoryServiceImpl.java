package com.javaweb.service.impl;

import com.javaweb.entity.PlayHistoryEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.repository.PlayHistoryRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.PlayHistoryService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class PlayHistoryServiceImpl implements PlayHistoryService {

    private final PlayHistoryRepository playHistoryRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    @Override
    public boolean createNewPlayHistory(Long songId) {
        try {
            SongEntity song = songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("Song with id: " + songId + " not found"));
            song.incrementPlayCount();
            UserEntity user = userRepository.findById(
                            Objects.requireNonNull(SecurityUtils.getPrincipal()).getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));
            PlayHistoryEntity playHistoryEntity = new PlayHistoryEntity(user, song);
            playHistoryRepository.save(playHistoryEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
