package com.wan.service;

import com.wan.pojo.AllGroups;

import java.util.List;

public interface AllGroupsService{
    List<AllGroups> showGroups();
    int DelGroupsById(Integer id);
    int AddGroups(AllGroups allGroups);

    int UpdateGroupsById(AllGroups allGroups);

    AllGroups showSingleGroupById(Integer i);
}
