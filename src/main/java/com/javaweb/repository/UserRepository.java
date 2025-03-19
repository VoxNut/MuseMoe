package com.javaweb.repository;

import com.javaweb.entity.User;
import com.javaweb.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findOneByUsername(String userName);

    User findOneByUsernameAndAccountStatus(String username, AccountStatus accountStatus);
}
