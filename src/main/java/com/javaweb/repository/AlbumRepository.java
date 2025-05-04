package com.javaweb.repository;

import com.javaweb.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {

    Optional<AlbumEntity> findByTitle(String title);


}
