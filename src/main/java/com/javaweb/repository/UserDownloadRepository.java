package com.javaweb.repository;

import com.javaweb.entity.UserDownloadEntity;
import com.javaweb.entity.UserDownloadId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDownloadRepository extends JpaRepository<UserDownloadEntity, UserDownloadId> {

    // Find all downloads by user ID
    List<UserDownloadEntity> findByUserId(Long userId);

    // Find all users who downloaded a specific song
    List<UserDownloadEntity> findBySongId(Long songId);

    // Find top downloaded songs (with count)
    @Query("SELECT ud.song, SUM(ud.downloadCount) as totalDownloads " +
            "FROM UserDownloadEntity ud " +
            "GROUP BY ud.song " +
            "ORDER BY totalDownloads DESC")
    List<Object[]> findTopDownloadedSongs(Pageable pageable);

    // Check if a user has downloaded a specific song
    boolean existsByUserIdAndSongId(Long userId, Long songId);

    // Find recent downloads by a user
    @Query("SELECT ud FROM UserDownloadEntity ud WHERE ud.user.id = :userId " +
            "ORDER BY ud.downloadDate DESC")
    List<UserDownloadEntity> findRecentDownloadsByUser(@Param("userId") Long userId, Pageable pageable);
}
