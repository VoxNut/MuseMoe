package com.javaweb.repository;

import com.javaweb.entity.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<MediaEntity, Long> {
    Optional<MediaEntity> findByFileUrl(String fileUrl);
}
