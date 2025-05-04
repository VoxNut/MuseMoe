package com.javaweb.view;

import com.javaweb.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface SongEntityRepository extends JpaRepository<SongEntity, Long> {
}
