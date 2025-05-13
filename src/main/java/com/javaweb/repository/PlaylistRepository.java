package com.javaweb.repository;

import com.javaweb.entity.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {


    @Query("SELECT p FROM PlaylistEntity p WHERE p.user.id = :userId")
    List<PlaylistEntity> findPlaylistsByUserId(Long userId);

    @Query("SELECT DISTINCT p FROM PlaylistEntity p " +
            "LEFT JOIN FETCH p.playlistSongEntities ps " +
            "LEFT JOIN FETCH ps.song " +
            "WHERE p.user.id = :userId " +
            "ORDER BY ps.position ASC")
    List<PlaylistEntity> findPlaylistsByUserIdWithSongsOrdered(@Param("userId") Long userId);


    List<PlaylistEntity> findByNameContainingIgnoreCase(String name);
}
