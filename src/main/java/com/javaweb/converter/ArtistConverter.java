package com.javaweb.converter;

import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.MediaEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.enums.MediaType;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.request.ArtistRequestDTO;
import com.javaweb.repository.MediaRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class ArtistConverter implements EntityConverter<ArtistEntity, ArtistRequestDTO, ArtistDTO> {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    @Override
    public ArtistDTO toDTO(ArtistEntity entity) {
        if (entity != null) {
            ArtistDTO artistDTO = modelMapper.map(entity, ArtistDTO.class);
            if ((entity.getProfilePic() != null)) {
                artistDTO.setProfilePicture(entity.getProfilePic().getFileUrl());
            }
            return artistDTO;
        }
        return null;
    }

    @Override
    public ArtistEntity toEntity(ArtistRequestDTO request) {
        ArtistEntity entity = modelMapper.map(request, ArtistEntity.class);
        File file = new File(request.getProfilePicture());

        MediaEntity mediaEntity = mediaRepository.findByFileUrl(request.getProfilePicture())
                .orElse(new MediaEntity(request.getProfilePicture(), MediaType.IMAGE, FileUtil.getFileSize(file)));
        
        entity.setProfilePic(mediaEntity);
        if (request.getUserId() != null) {
            UserEntity userEntity = userRepository.findById(request.getUserId()).orElse(null);
            entity.setUser(userEntity);
        }
        return entity;
    }
}
