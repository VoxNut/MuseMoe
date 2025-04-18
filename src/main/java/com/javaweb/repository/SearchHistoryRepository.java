package com.javaweb.repository;

import com.javaweb.entity.SearchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntity, Long> {

    @Query(value = """
             WITH ranked_plays AS (
               SELECT *,
                      ROW_NUMBER() OVER (PARTITION BY song_id ORDER BY searched_at DESC) AS rn
               FROM search_history
               WHERE user_id = :userId
             )
             SELECT *\s
             FROM ranked_plays
             WHERE rn = 1
             ORDER BY searched_at DESC
             LIMIT :limit
            \s""", nativeQuery = true)
    List<SearchHistoryEntity> fetchRecentSearchHistory(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Modifying
    @Transactional
    @Query("DELETE FROM SearchHistoryEntity sh WHERE sh.user.id = :userId AND sh.song.id IN :songIds")
    int deleteSearchHistoryBySongIds(@Param("userId") Long userId, @Param("songIds") List<Long> songIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM SearchHistoryEntity sh WHERE sh.user.id = :userId")
    int deleteAllSearchHistoryByUserId(@Param("userId") Long userId);

    @Query(value = """
             SELECT sh.search_term
             FROM search_history sh
             WHERE sh.user_id = :userId
             GROUP BY sh.search_term
             ORDER BY MAX(sh.searched_at) DESC
             LIMIT :limit
            """, nativeQuery = true)
    List<String> fetchRecentSearchTerms(@Param("userId") Long userId, @Param("limit") Integer limit);
}