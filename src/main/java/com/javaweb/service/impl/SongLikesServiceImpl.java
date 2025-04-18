package com.javaweb.service.impl;


import com.javaweb.converter.SongLikesConverter;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.SongLikesEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.SongLikesDTO;
import com.javaweb.repository.SongLikesRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.SongLikesService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class SongLikesServiceImpl implements SongLikesService {

    private final UserRepository userRepository;

    private final SongRepository songRepository;

    private final SongLikesRepository songLikesRepository;

    private final SongLikesConverter songLikesConverter;

    @Override
    public List<SongLikesDTO> findAllByUser() {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found!"));
        List<SongLikesDTO> songLikesDTOS =
                songLikesRepository.findAllByUser(currentUser)
                        .stream()
                        .map(songLikesConverter::toDTO)
                        .toList();

        return songLikesDTOS;
    }

    @Override
    public boolean checkSongLiked(Long songId) {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found!"));
        SongEntity songEntity = songRepository.
                findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found!"));
        SongLikesEntity songLikesEntity = songLikesRepository.findSongLikesEntitiesBySongAndUser(songEntity, currentUser);
        return songLikesEntity != null;
    }

    @Override
    public boolean deleteSongLikes(Long songId) {

        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found!"));
        SongEntity songEntity = songRepository.
                findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found!"));
        SongLikesEntity songLikesEntity = songLikesRepository.findSongLikesEntitiesBySongAndUser(songEntity, currentUser);

        if (songLikesEntity != null) {
            songLikesRepository.delete(songLikesEntity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean createSongLikes(Long songId) {

        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found!"));


        SongEntity songEntity = songRepository.
                findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found!"));


        var songLikesEntity = new SongLikesEntity(currentUser, songEntity);

        try {
            songLikesRepository.save(songLikesEntity);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
