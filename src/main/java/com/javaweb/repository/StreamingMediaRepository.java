package com.javaweb.repository;

import com.javaweb.entity.StreamingMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StreamingMediaRepository extends JpaRepository<StreamingMediaEntity, Long> {
    Optional<StreamingMediaEntity> findByGoogleDriveId(String id);
}
