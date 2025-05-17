package com.javaweb.repository;

import com.javaweb.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {

    Optional<AlbumEntity> findByTitle(String title);

    List<AlbumEntity> findByTitleContainingIgnoreCase(String title);

    List<AlbumEntity> findByArtistIdIn(List<Long> artistIds);

    @Query("select a from AlbumEntity a join a.songs s where s.id = :songId")
    AlbumEntity findAlbumEntitiesBySongsIsIn(Long songId);


    List<AlbumEntity> findAlbumEntitiesByArtist_Id(Long artistId);

}
