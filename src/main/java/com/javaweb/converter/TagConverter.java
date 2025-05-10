package com.javaweb.converter;


import com.javaweb.entity.TagEntity;
import com.javaweb.model.dto.TagDTO;
import com.javaweb.model.request.TagRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagConverter implements EntityConverter<TagEntity, TagRequestDTO, TagDTO> {
    private final ModelMapper modelMapper;

    @Override
    public TagDTO toDTO(TagEntity entity) {
        TagDTO dto = modelMapper.map(entity, TagDTO.class);
        dto.setType(entity.getTagtype().toString());
        return dto;
    }

    @Override
    public TagEntity toEntity(TagRequestDTO request) {
        return null;
    }
}
