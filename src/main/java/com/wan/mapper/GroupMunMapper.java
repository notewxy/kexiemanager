package com.wan.mapper;

import com.wan.pojo.GroupMun;

import java.util.List;

public interface GroupMunMapper {
    List<GroupMun> showGroupsMunById(int groupId);

    int delGroupMunById(Integer id);

    int AddGroupMun(GroupMun groupMun);

    int UpdateGroupMunById(GroupMun groupMun);

    GroupMun showSingleGroupsMunById(Integer id);
}
