package com.javaweb.repository;

import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<ArtistEntity, Long> {

    Optional<ArtistEntity> findByStageName(String stageName);

    ArtistEntity findByUser(UserEntity user);

    @Query("SELECT a FROM ArtistEntity a WHERE a.user.id =:userId")
    Optional<ArtistEntity> findByUserId(Long userId);


}
