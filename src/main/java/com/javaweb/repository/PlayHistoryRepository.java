package com.javaweb.repository;

import com.javaweb.entity.PlayHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistoryEntity, Long> {

    @Query(value = """
             WITH ranked_plays AS (
               SELECT *,
                      ROW_NUMBER() OVER (PARTITION BY song_id ORDER BY played_at DESC) AS rn
               FROM play_history
               WHERE user_id = :userId
             )
             SELECT *\s
             FROM ranked_plays
             WHERE rn = 1
             ORDER BY played_at DESC
             LIMIT :limit
            \s""", nativeQuery = true)
    List<PlayHistoryEntity> fetchRecentPlayHistory(@Param("userId") Long userId, @Param("limit") Integer limit);


    @Transactional
    @Modifying
    @Query("DELETE FROM PlayHistoryEntity ph WHERE ph.user.id = :userId AND ph.song.id IN :songIds")
    int deletePlayHistoryBySongIds(@Param("userId") Long userId, @Param("songIds") List<Long> songIds);


    @Modifying
    @Transactional
    @Query("DELETE FROM PlayHistoryEntity ph WHERE ph.user.id = :userId")
    int deleteAllPlayHistoryByUserId(@Param("userId") Long userId);
}
