package com.javaweb.repository;

import com.javaweb.entity.UserEntity;
import com.javaweb.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findOneByUsername(String userName);

    UserEntity findOneByUsernameAndAccountStatus(String username, AccountStatus accountStatus);

    Optional<UserEntity> findByEmail(String email);


}
