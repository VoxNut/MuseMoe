package com.javaweb.repository;

import com.javaweb.entity.UserArtistFollowEntity;
import com.javaweb.entity.UserArtistFollowId;
import com.javaweb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserArtistFollowRepository extends JpaRepository<UserArtistFollowEntity, Long> {

    @Query("SELECT uaf FROM UserArtistFollowEntity uaf WHERE uaf.follower = :follower")
    List<UserArtistFollowEntity> findByFollower(UserEntity follower);

    @Query("SELECT uaf FROM UserArtistFollowEntity uaf WHERE uaf.artist = :artist")
    List<UserArtistFollowEntity> findByArtist(UserEntity artist);

    void deleteById(UserArtistFollowId id);


    boolean existsByArtist_IdAndFollower_Id(Long artistId, Long userId);

    UserArtistFollowEntity findByArtist_IdAndFollower_Id(Long artistId, Long userId);

}
