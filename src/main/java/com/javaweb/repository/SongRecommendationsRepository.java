package com.javaweb.repository;

import com.javaweb.entity.SongRecommendationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRecommendationsRepository extends JpaRepository<SongRecommendationsEntity, Long> {
    List<SongRecommendationsEntity> findByUserIdOrderByRecommendationStrengthDesc(Long userId);

    void deleteByUserId(Long userId);


}
