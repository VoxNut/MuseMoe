package com.javaweb.service.impl;

import com.javaweb.converter.UserConverter;
import com.javaweb.entity.User;
import com.javaweb.enums.AccountStatus;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.PasswordDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.PasswordService;
import com.javaweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final UserConverter userConverter;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordService passwordService, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.userConverter = userConverter;
    }




    @Override
    public UserDTO findOneByUsername(String userName) {
        User User = userRepository.findOneByUsername(userName);
        return User != null ? userConverter.toDTO(User) : null;
    }

    @Override
    public UserDTO findUserById(Long id) {
        User entity = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found!"));
        return userConverter.toDTO(entity);
    }



    @Override
    public void updatePassword(long id, PasswordDTO passwordDTO)  {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found!"));
        if (passwordService.matches(passwordDTO.getOldPassword(), passwordDTO.getNewPassword()) && passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            user.setPassword(passwordService.encodePassword(passwordDTO.getNewPassword()));
            userRepository.save(user);
        } else {
        }
    }


    @Override
    public UserDTO updateProfileOfUser(String username, UserDTO updateUser) {
        User oldUser = userRepository.findOneByUsername(username);
        oldUser.setFullName(updateUser.getFullName());
        return userConverter.toDTO(userRepository.save(oldUser));
    }

    @Override
    public void delete(Set<Long> ids) {
        for (Long item : ids) {
            User User = userRepository.findById(item).orElseThrow(() -> new EntityNotFoundException("User not found!"));
            User.setAccountStatus(AccountStatus.INACTIVE);
            userRepository.save(User);
        }
    }

    public boolean resetPassword(Long userId, String newPassword) {
        return userRepository.findById(userId).map(user -> {
            user.setPassword(passwordService.encodePassword(newPassword));
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    @Override
    public void saveSignUpUser(UserRequestDTO userRequestDTO) {
//        Role roleEntity = roleRepository.findOneByCode(Role.PREMIUM);// Customer role if register
//        if (roleEntity == null) {
//            throw new RuntimeException("Role CUSTOMER not found");
//        }
//        userRequestDTO.setPassword(passwordService.encodePassword(userRequestDTO.getPassword()));
//        User User = userConverter.convertToEntity(userRequestDTO);
//        User.setRoles(Stream.of(roleEntity).collect(Collectors.toCollection(HashSet::new)));
//        CustomerEntity customerEntity = new CustomerEntity();
//        customerEntity.setType(CustomerType.CASUAL);
//        customerEntity.setPoint(0);
//        User.setCustomer(customerEntity);
//        customerRepository.save(customerEntity);
//        userRepository.save(User);
    }

    @Override
    public void updateUser(UserRequestDTO userRequestDTO) {
//        User existingUser = userRepository.findById(userRequestDTO.getId()).orElseThrow(() -> new EntityNotFoundException("User not found!"));
//        if (userRequestDTO.getPassword().isEmpty()) {
//            userRequestDTO.setPassword(existingUser.getPassword());
//        } else {
//            userRequestDTO.setPassword(passwordService.encodePassword(userRequestDTO.getPassword()));
//        }
//        User User = userConverter.convertToEntity(userRequestDTO);
//        User.setCustomer(existingUser.getCustomer());
//        if (User.getCustomer() != null) {
//            CustomerEntity customerEntity = User.getCustomer();
//            customerEntity.setFullName(userRequestDTO.getFullName());
//            customerEntity.setEmail(userRequestDTO.getEmail());
//            customerEntity.setPhone(userRequestDTO.getPhone());
//            customerRepository.save(customerEntity);
//        }
//        User.setOrders(existingUser.getOrders());
//
//        User.setProducts(existingUser.getProducts());
//
//        userRepository.save(User);
    }


    @Override
    public void saveUser(UserRequestDTO userRequestDTO) {
        User User = userConverter.toEntity(userRequestDTO);
        User.setPassword(passwordService.encodePassword(userRequestDTO.getPassword()));
        userRepository.save(User);
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        return userConverter.toDTO(userRepository.findOneByUsername(username));
    }

    @Override
    public Set<UserDTO> findAllUser(Map<String, Object> params, Set<String> roles) {
        return null;
    }


}
