package com.javaweb.repository;

import com.javaweb.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<SongEntity, Long> {


    Optional<SongEntity> findOneByTitle(String title);

    @Query("SELECT DISTINCT s from SongEntity s WHERE s.title LIKE %:title%")
    List<SongEntity> findAllSongsLike(String title);


    @Query("SELECT s FROM SongEntity s JOIN s.audioFile m WHERE m.fileUrl = :url")
    Optional<SongEntity> findSongByUrl(String url);
}
