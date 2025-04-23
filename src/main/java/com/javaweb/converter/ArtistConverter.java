package com.javaweb.converter;

import com.javaweb.entity.ArtistEntity;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.request.ArtistRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArtistConverter implements EntityConverter<ArtistEntity, ArtistRequestDTO, ArtistDTO> {
    private final ModelMapper modelMapper;


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
        return null;
    }
}
