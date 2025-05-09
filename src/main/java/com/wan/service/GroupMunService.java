package com.wan.service;

import com.wan.pojo.GroupMun;

import java.util.List;

public interface GroupMunService {
    List<GroupMun> showGroupsMunById(int groupId);

    int delGroupMunById(Integer id);

    int AddGroupMun(GroupMun groupMun);

    int UpdateGroupMunById(GroupMun groupMun);

    GroupMun showSingleGroupsMunById(Integer id);
}
