package com.javaweb.repository.custom;


import com.javaweb.builder.UserSearchBuilder;
import com.javaweb.entity.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> getAllUsers(UserSearchBuilder userSearchBuilder);


}
