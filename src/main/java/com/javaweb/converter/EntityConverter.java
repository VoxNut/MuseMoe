package com.javaweb.converter;

public interface EntityConverter<E, R, D> {
    D toDTO(E entity);

    E toEntity(R request);
}
