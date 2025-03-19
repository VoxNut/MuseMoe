package com.javaweb.repository;

import com.javaweb.entity.Role;
import com.javaweb.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Role findOneByName(String name);
    Role findOneByCode(RoleType type);
}
