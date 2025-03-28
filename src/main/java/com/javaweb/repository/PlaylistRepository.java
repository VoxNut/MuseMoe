package com.javaweb.repository;

import com.javaweb.entity.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {


    @Query("SELECT p FROM PlaylistEntity p WHERE p.user.id = :userId")
    List<PlaylistEntity> findPlaylistsByUserId(Long userId);

}
