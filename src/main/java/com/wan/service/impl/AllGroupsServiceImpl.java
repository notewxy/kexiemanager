package com.wan.service.impl;

import com.wan.mapper.AllGroupsMapper;
import com.wan.pojo.AllGroups;
import com.wan.service.AllGroupsService;

import java.util.List;

public class AllGroupsServiceImpl implements AllGroupsService {

    private AllGroupsMapper allGroupsMapper;

    public AllGroupsServiceImpl(AllGroupsMapper allGroupsMapper){ this.allGroupsMapper=allGroupsMapper; }

    @Override
    public List<AllGroups> showGroups() {
        return allGroupsMapper.selectAllGroups();
    }

    @Override
    public int DelGroupsById(Integer id) {
        return allGroupsMapper.deleteGroupById(id);
    }

    @Override
    public int AddGroups(AllGroups allGroups) {
        return allGroupsMapper.addGroups(allGroups);
    }

    @Override
    public int UpdateGroupsById(AllGroups allGroups) {
        return allGroupsMapper.updateGroupsById(allGroups);
    }
}
