package com.javaweb.service.impl;


import com.javaweb.entity.SongEntity;
import com.javaweb.entity.SongLikesEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.repository.SongLikesRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.SongLikesService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class SongLikesServiceImpl implements SongLikesService {

    private final UserRepository userRepository;

    private final SongRepository songRepository;

    private final SongLikesRepository songLikesRepository;


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
    public boolean createSongLikes(Long songId) {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found!"));


        SongEntity songEntity = songRepository.
                findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found!"));


        SongLikesEntity songLikesEntity = songLikesRepository.findSongLikesEntitiesBySongAndUser(songEntity, currentUser);
        boolean liked = true;
        if (songLikesEntity == null) {
            SongLikesEntity newSongLikes = new SongLikesEntity(currentUser, songEntity);
            songLikesRepository.save(newSongLikes);
            liked = false;
        } else {
            songLikesRepository.delete(songLikesEntity);
        }

        return liked;


    }
}
