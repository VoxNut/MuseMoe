package com.javaweb.repository;

import com.javaweb.entity.SongEntity;
import com.javaweb.entity.SongLikesEntity;
import com.javaweb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongLikesRepository extends JpaRepository<SongLikesEntity, Long> {

    SongLikesEntity findSongLikesEntitiesBySongAndUser(SongEntity song, UserEntity user);

    SongLikesEntity song(SongEntity song);

    @Query("SELECT sl FROM SongLikesEntity sl where sl.user = :user order by sl.likedAt desc")
    List<SongLikesEntity> findAllByUser(UserEntity user);
}
