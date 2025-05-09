package com.javaweb.repository;

import com.javaweb.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<SongEntity, Long> {


    Optional<SongEntity> findOneByTitle(String title);

    @Query("SELECT DISTINCT s from SongEntity s WHERE s.title LIKE %:title%")
    List<SongEntity> findAllSongsLike(String title);

    @Query("SELECT s FROM SongEntity s WHERE s.streamingMedia.googleDriveId = :googleDriveId")
    SongEntity findByStreamingMediaGoogleDriveId(String googleDriveId);

    @Query("SELECT s FROM SongEntity s JOIN s.streamingMedia m WHERE m.webContentLink = :url")
    Optional<SongEntity> findSongByUrl(String url);

    Optional<SongEntity> findByStreamingMediaWebContentLink(String webContentLink);

    @Query("SELECT s FROM SongEntity s WHERE s.streamingMedia IS NULL")
    List<SongEntity> findByStreamingMediaIsNull();

    Optional<SongEntity> findByStreamingMediaId(Long streamingMediaId);

    @Query("SELECT s FROM SongEntity s JOIN s.artists a WHERE s.title = :title AND a.stageName = :artistName")
    Optional<SongEntity> findByTitleAndArtist(@Param("title") String title, @Param("artistName") String artistName);

    @Query("SELECT s FROM SongEntity s WHERE s.streamingMedia IS NOT NULL")
    List<SongEntity> findAllWithStreamingMedia();

    @Query("SELECT s FROM SongEntity s WHERE s.album IS NULL AND s.streamingMedia IS NOT NULL")
    List<SongEntity> findSongsWithoutAlbum();

    List<SongEntity> findByAlbumId(Long albumId);


}
