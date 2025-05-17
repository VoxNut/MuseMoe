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

    @Query("SELECT s FROM SongEntity s JOIN PlayHistoryEntity ph ON s.id = ph.song.id GROUP BY s.id, s.title ORDER BY COUNT(ph.id) DESC LIMIT :limit")
    List<SongEntity> findMostPopularSongs(int limit);

    @Query("SELECT s.id FROM SongEntity s")
    List<Long> findAllIds();

    List<SongEntity> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT s FROM SongEntity s WHERE s.album.id IN :albumIds")
    List<SongEntity> findByAlbumIds(@Param("albumIds") List<Long> albumIds);

    @Query("SELECT s FROM SongEntity s JOIN s.artists a WHERE a.id IN :artistIds")
    List<SongEntity> findByArtistIds(@Param("artistIds") List<Long> artistIds);

    @Query("SELECT s FROM SongEntity s JOIN PlaylistSongEntity ps ON s.id = ps.song.id WHERE ps.playlist.id IN :playlistIds")
    List<SongEntity> findByPlaylistIds(@Param("playlistIds") List<Long> playlistIds);

    @Query(value = "select s.* from song s join song_artist sa on s.id = sa.song_id where sa.artist_id = :artistId order by play_count desc limit :limit ", nativeQuery = true)
    List<SongEntity> fetchPopularTracksByArtistId(Long artistId, int limit);

}

