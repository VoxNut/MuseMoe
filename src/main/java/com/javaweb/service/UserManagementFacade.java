package com.javaweb.service;

import com.javaweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementFacade {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Autowired
    public UserManagementFacade(
            UserRepository userRepository,
            PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }


    @Transactional
    public boolean resetPassword(Long userId, String newPassword) {
        return userRepository.findById(userId).map(user -> {
            user.setPassword(passwordService.encodePassword(newPassword));
            userRepository.save(user);
            return true;
        }).orElse(false);
    }
}