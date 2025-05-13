package com.javaweb.repository;

import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<ArtistEntity, Long> {

    Optional<ArtistEntity> findByStageName(String stageName);

    ArtistEntity findByUser(UserEntity user);

    @Query("SELECT a FROM ArtistEntity a WHERE a.user.id =:userId")
    Optional<ArtistEntity> findByUserId(Long userId);

    List<ArtistEntity> findBySongsId(Long songsId);

    @Query("SELECT a FROM ArtistEntity a " +
            "LEFT JOIN UserArtistFollowEntity uaf ON uaf.artist.id = a.id " +
            "WHERE LOWER(a.stageName) LIKE LOWER(CONCAT('%', :stageName, '%')) " +
            "GROUP BY a.id, a.stageName, a.bio, a.created_at, a.updated_at, a.profilePic " +
            "ORDER BY COUNT(uaf) DESC")
    List<ArtistEntity> findByStageNameContainingIgnoreCaseOrderByFollowersCountDesc(@Param("stageName") String stageName);
}