package com.javaweb.repository;


import com.javaweb.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findByName(String name);

    @Query("SELECT t FROM TagEntity t JOIN t.songs s WHERE s.id = :songId")
    List<TagEntity> findTagEntitiesBySongId(@Param("songId") Long songId);
}
