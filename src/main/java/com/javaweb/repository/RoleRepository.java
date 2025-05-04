package com.javaweb.repository;

import com.javaweb.entity.RoleEntity;
import com.javaweb.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    RoleEntity findOneByName(String name);

    RoleEntity findOneByCode(RoleType type);

    RoleEntity findOneByCode(String code);
}
