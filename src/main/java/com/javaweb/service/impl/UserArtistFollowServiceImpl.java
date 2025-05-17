package com.javaweb.service.impl;

import com.javaweb.converter.ArtistConverter;
import com.javaweb.converter.UserConverter;
import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.UserArtistFollowEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.repository.UserArtistFollowRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.UserArtistFollowService;
import com.javaweb.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserArtistFollowServiceImpl implements UserArtistFollowService {

    private final UserRepository userRepository;
    private final UserArtistFollowRepository userArtistFollowRepository;
    private final ArtistConverter artistConverter;
    private final UserConverter userConverter;
    private final ArtistRepository artistRepository;


    @Override
    public List<ArtistDTO> findFollowedArtists() {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity currentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            return userArtistFollowRepository.findByFollower(currentUser)
                    .stream()
                    .map(UserArtistFollowEntity::getArtist)
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
            ArtistEntity artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

            UserArtistFollowEntity followEntity = new UserArtistFollowEntity(follower, artist);
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
            UserArtistFollowEntity userArtistFollowEntity = userArtistFollowRepository.findByArtist_IdAndFollower_Id(artistId, userId);
            userArtistFollowRepository.delete(userArtistFollowEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean followArtists(List<Long> artistIds) {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found! Id: " + userId));

            List<Long> existingFollowedArtistIds = userArtistFollowRepository.findByFollower(user).stream()
                    .map(follow -> follow.getArtist().getId())
                    .collect(Collectors.toList());

            List<Long> newArtistIds = artistIds.stream()
                    .filter(id -> !existingFollowedArtistIds.contains(id))
                    .collect(Collectors.toList());

            if (newArtistIds.isEmpty()) {
                log.warn("All Artists: {} has followed", artistIds);
                return true;
            }

            List<ArtistEntity> artistsToFollow = artistRepository.findAllById(newArtistIds);

            if (artistsToFollow.size() != newArtistIds.size()) {
                log.warn("Not all artists were found. Requested: {}, Found: {}",
                        newArtistIds.size(), artistsToFollow.size());
            }

            List<UserArtistFollowEntity> newFollows = artistsToFollow.stream()
                    .map(artist -> new UserArtistFollowEntity(user, artist))
                    .collect(Collectors.toList());
            userArtistFollowRepository.saveAll(newFollows);
            return true;
        } catch (Exception e) {
            log.error("User with id: {} cannot follow these artists: {}. Error: {}",
                    userId, artistIds, e.getMessage(), e);
            return false;
        }
    }
}