package com.javaweb.repository;

import com.javaweb.entity.QueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QueueRepository extends JpaRepository<QueueEntity, Long> {

    @Query("SELECT MAX(q.position) FROM QueueEntity q WHERE q.user.id = :userId")
    Optional<Integer> findHighestPositionByUserId(Long userId);

}
