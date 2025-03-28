package com.javaweb.converter;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;

public interface EntityConverter<E, R, D> {
    D toDTO(E entity) throws InvalidDataException, UnsupportedTagException, IOException;

    E toEntity(R request);
}
