package com.wan.service;

import com.wan.pojo.Users;

public interface UsersService {
    String getPasswordByName(String username);

    int AddUsers(Users users);
}
