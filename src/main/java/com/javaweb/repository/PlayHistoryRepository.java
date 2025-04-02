package com.javaweb.repository;

import com.javaweb.entity.PlayHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistoryEntity, Long> {

}
