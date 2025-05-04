package com.javaweb.converter;

import com.javaweb.entity.StreamingMediaEntity;
import com.javaweb.model.dto.StreamingMediaDTO;
import com.javaweb.model.request.StreamingMediaRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StreamingMediaConverter implements EntityConverter<StreamingMediaEntity, StreamingMediaRequestDTO, StreamingMediaDTO> {


    private final ModelMapper modelMapper;

    @Override
    public StreamingMediaDTO toDTO(StreamingMediaEntity entity) {
        return modelMapper.map(entity, StreamingMediaDTO.class);
    }

    @Override
    public StreamingMediaEntity toEntity(StreamingMediaRequestDTO request) {
        return null;
    }
}
