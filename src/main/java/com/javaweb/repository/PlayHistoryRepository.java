package com.javaweb.repository;

import com.javaweb.entity.PlayHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistoryEntity, Long> {

    @Query(value = "SELECT ph.* FROM play_history ph WHERE ph.user_id = :userId ORDER BY ph.played_at DESC LIMIT :limit", nativeQuery = true)
    List<PlayHistoryEntity> fetchRecentPlayHistory(@Param("userId") Long userId, @Param("limit") Integer limit);
}
