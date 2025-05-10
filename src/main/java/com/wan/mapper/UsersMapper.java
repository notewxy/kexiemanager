package com.wan.mapper;

import com.wan.pojo.Users;

public interface UsersMapper {

    String getPasswordByName(String username);

    int AddUsers(Users users);
}
