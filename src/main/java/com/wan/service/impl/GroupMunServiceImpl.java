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

    @Override
    public int delGroupMunById(Integer id) {
        return groupMunMapper.delGroupMunById(id);
    }

    @Override
    public int AddGroupMun(GroupMun groupMun) {
        return groupMunMapper.AddGroupMun(groupMun);
    }

    @Override
    public int UpdateGroupMunById(GroupMun groupMun) {
        return groupMunMapper.UpdateGroupMunById(groupMun);
    }

    @Override
    public GroupMun showSingleGroupsMunById(Integer id) {
        return groupMunMapper.showSingleGroupsMunById(id);
    }

    @Override
    public List<GroupMun> showAll() {
        return groupMunMapper.showAll();
    }
}
