package com.javaweb.repository;


import com.javaweb.entity.LyricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LyricsRepository extends JpaRepository<LyricsEntity, Long> {
    Optional<LyricsEntity> findBySongId(Long songId);
}
