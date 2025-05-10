package com.wan.service.impl;

import com.wan.mapper.UsersMapper;
import com.wan.pojo.Users;
import com.wan.service.UsersService;

public class UsersServiceImpl implements UsersService {

    private UsersMapper usersMapper;

    public UsersServiceImpl(UsersMapper usersMapper)
    {
        this.usersMapper = usersMapper;
    }

    @Override
    public String getPasswordByName(String username) {
        return usersMapper.getPasswordByName(username);
    }

    @Override
    public int AddUsers(Users users) {
        return usersMapper.AddUsers(users);
    }
}
