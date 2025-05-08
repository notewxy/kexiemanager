package com.wan.service.impl;

import com.wan.mapper.GroupMunMapper;
import com.wan.pojo.GroupMun;
import com.wan.service.GroupMunService;

import java.util.List;

public class GroupMunServiceImpl implements GroupMunService {

    private GroupMunMapper groupMunMapper;
    public GroupMunServiceImpl(GroupMunMapper groupMunMapper)
    {
        this.groupMunMapper  = groupMunMapper;
    }

    @Override
    public List<GroupMun> showGroupsMunById(int groupId) {
        return groupMunMapper.showGroupsMunById(groupId);
    }
}
