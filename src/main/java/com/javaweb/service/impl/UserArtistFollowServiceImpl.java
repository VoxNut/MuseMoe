package com.javaweb.service.impl;

import com.javaweb.converter.ArtistConverter;
import com.javaweb.converter.UserConverter;
import com.javaweb.entity.UserArtistFollowEntity;
import com.javaweb.entity.UserArtistFollowId;
import com.javaweb.entity.UserEntity;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.repository.UserArtistFollowRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.UserArtistFollowService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserArtistFollowServiceImpl implements UserArtistFollowService {

    private final UserRepository userRepository;
    private final UserArtistFollowRepository userArtistFollowRepository;
    private final ArtistConverter artistConverter;
    private final UserConverter userConverter;


    @Override
    public List<ArtistDTO> findFollowedArtists() {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity currentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            return userArtistFollowRepository.findByFollower(currentUser)
                    .stream()
                    .map(UserArtistFollowEntity::getArtist)
                    .map(UserEntity::getArtist)
                    .map(artistConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<UserDTO> findFollowersByArtistId(Long artistId) {
        try {
            UserEntity artist = userRepository.findById(artistId)
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

            return userArtistFollowRepository.findByArtist(artist)
                    .stream()
                    .map(UserArtistFollowEntity::getFollower)
                    .map(userConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public boolean followArtist(Long artistId) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity follower = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            UserEntity artist = userRepository.findById(artistId)
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

            UserArtistFollowId id = new UserArtistFollowId(follower.getId(), artist.getId());
            UserArtistFollowEntity followEntity = new UserArtistFollowEntity();
            followEntity.setId(id);
            followEntity.setFollower(follower);
            followEntity.setArtist(artist);
            followEntity.setFollowedAt(LocalDateTime.now());

            userArtistFollowRepository.save(followEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean unfollowArtist(Long artistId) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity follower = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            UserEntity artist = userRepository.findById(artistId)
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

            UserArtistFollowId id = new UserArtistFollowId(follower.getId(), artist.getId());
            userArtistFollowRepository.deleteById(id);


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}