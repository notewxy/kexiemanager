package com.wan.mapper;

import com.wan.pojo.AllGroups;

import java.util.List;

public interface AllGroupsMapper {
    List<AllGroups> selectAllGroups();
    int deleteGroupById(Integer id);

    int addGroups(AllGroups allGroups);

    int updateGroupsById(AllGroups allGroups);

    AllGroups selectSingleGroupById(Integer id);
}
