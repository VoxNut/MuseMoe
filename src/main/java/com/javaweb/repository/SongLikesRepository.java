package com.javaweb.repository;

import com.javaweb.entity.SongEntity;
import com.javaweb.entity.SongLikesEntity;
import com.javaweb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongLikesRepository extends JpaRepository<SongLikesEntity, Long> {

    @Query("SELECT sl FROM SongLikesEntity sl JOIN FETCH sl.song JOIN FETCH sl.user WHERE sl.song = :song AND sl.user = :user")
    SongLikesEntity findSongLikesEntitiesBySongAndUser(@Param("song") SongEntity song, @Param("user") UserEntity user);

    @Query("SELECT sl FROM SongLikesEntity sl JOIN FETCH sl.song JOIN FETCH sl.user WHERE sl.user = :user ORDER BY sl.likedAt DESC")
    List<SongLikesEntity> findAllByUser(@Param("user") UserEntity user);

    List<SongLikesEntity> findByUserIdIn(List<Long> userIds);
}
