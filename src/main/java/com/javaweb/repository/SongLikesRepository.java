package com.javaweb.repository;

import com.javaweb.entity.SongEntity;
import com.javaweb.entity.SongLikesEntity;
import com.javaweb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongLikesRepository extends JpaRepository<SongLikesEntity, Long> {

    SongLikesEntity findSongLikesEntitiesBySongAndUser(SongEntity song, UserEntity user);

    SongLikesEntity song(SongEntity song);
}
