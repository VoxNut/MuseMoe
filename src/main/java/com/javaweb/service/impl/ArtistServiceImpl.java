package com.javaweb.service.impl;

import com.javaweb.converter.ArtistConverter;
import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.RoleEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.request.ArtistRequestDTO;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.ArtistService;
import com.javaweb.service.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistServiceImpl implements ArtistService {
    private final ArtistConverter artistConverter;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GoogleDriveService googleDriveService;
    private final PasswordService passwordService;

    @Override
    public List<Long> getArtistsIdBySongId(Long songId) {
        List<ArtistDTO> artistDTOS =
                artistRepository.findBySongsId(songId)
                        .stream()
                        .map(artistConverter::toDTO)
                        .toList();
        List<Long> artistIds = artistDTOS.stream().map(ArtistDTO::getId).toList();
        return artistIds;
    }

    @Override
    @Transactional
    public ArtistDTO createArtist(ArtistRequestDTO artistRequestDTO) {
        try {
            // Step 1: Upload artist profile picture if provided
            if (artistRequestDTO.getArtistProfilePicture() != null && !artistRequestDTO.getArtistProfilePicture().isEmpty()) {
                String driveFileId = googleDriveService.uploadImageFile(
                        artistRequestDTO.getArtistProfilePicture(),
                        GoogleDriveService.ARTIST_PROFILE_FOLDER_ID
                );
                artistRequestDTO.setGoogleDriveFileId(driveFileId);
                log.info("Uploaded artist profile image to Google Drive with ID: {}", driveFileId);
            }

            // Step 2: Check if user already exists by ID
            UserEntity user;
            if (artistRequestDTO.getUserId() != null) {
                user = userRepository.findById(artistRequestDTO.getUserId())
                        .orElseThrow(() -> new RuntimeException("User with ID " + artistRequestDTO.getUserId() + " not found"));
                //F*ck
                RoleEntity role = roleRepository.findOneByCode(RoleType.ARTIST);
                user.getRoles().add(role);
            } else {
                // If no specific user ID is provided, create a new user based on artist info
                user = createUserForArtist(artistRequestDTO);
            }

            // Step 3: Create and save artist entity
            ArtistEntity artistEntity = artistConverter.toEntity(artistRequestDTO);
            artistEntity.setUser(user);

            // Set non-null fields
            if (StringUtils.isBlank(artistEntity.getBio())) {
                artistEntity.setBio("Artist on MuseMoe");
            }

            // Save the artist entity to get ID assigned
            artistEntity = artistRepository.save(artistEntity);

            // Step 4: Update the user with artist role if needed
            addArtistRoleToUser(user);

            // Step 5: Associate the artist with the user
            user.setArtist(artistEntity);
            userRepository.save(user);

            return artistConverter.toDTO(artistEntity);
        } catch (Exception e) {
            log.error("Cannot create artist: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create artist: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new user account for an artist
     */
    private UserEntity createUserForArtist(ArtistRequestDTO artistRequestDTO) {
        // Create a new user based on artist information
        UserEntity user = new UserEntity();

        // Generate username from stage name if available
        String stageName = artistRequestDTO.getStageName();
        String username = stageName.toLowerCase().replace(" ", "");

        // Check if username already exists and modify if needed
        if (userRepository.existsByUsername(username)) {
            username = username + System.currentTimeMillis() % 1000;
        }

        // Set user properties
        user.setUsername(username);
        user.setFullName(stageName);
        user.setEmail(username + "@musemoe.com");  // Generate an email
        user.setPassword(passwordService.encodePassword("Artist@" + System.currentTimeMillis() % 10000));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setLastLogin(LocalDateTime.now());

        // Add user role
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleRepository.findOneByCode(RoleType.FREE));
        user.setRoles(roles);

        return userRepository.save(user);
    }

    /**
     * Add the artist role to a user if they don't already have it
     */
    private void addArtistRoleToUser(UserEntity user) {
        RoleEntity artistRole = roleRepository.findOneByCode(RoleType.ARTIST);
        if (artistRole != null && !userHasRole(user, RoleType.ARTIST)) {
            user.getRoles().add(artistRole);
        }
    }

    /**
     * Check if a user has a specific role
     */
    private boolean userHasRole(UserEntity user, RoleType roleCode) {
        return user.getRoles().stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }
}