package com.javaweb.repository;

import com.javaweb.entity.UserEntity;
import com.javaweb.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findOneByUsername(String userName);

    UserEntity findOneByUsernameAndAccountStatus(String username, AccountStatus accountStatus);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<UserEntity> findByUsernameContainingIgnoreCase(String query);


    List<UserEntity> findByAccountStatus(AccountStatus accountStatus);

    @Query("SELECT u FROM UserEntity u WHERE u.created_at BETWEEN :fromDate AND :toDate")
    List<UserEntity> findUsersByDateRange(@Param("fromDate") Date fromDate,
                                          @Param("toDate") Date toDate);
}
