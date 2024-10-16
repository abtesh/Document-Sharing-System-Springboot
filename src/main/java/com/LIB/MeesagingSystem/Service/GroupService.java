package com.LIB.MeesagingSystem.Service;


import com.LIB.MeesagingSystem.Dto.GroupCreateDto;
import com.LIB.MeesagingSystem.Dto.InboxMessageDto;
import com.LIB.MeesagingSystem.Model.Group;
import com.LIB.MeesagingSystem.Model.Message;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


public interface GroupService {
    Group createGroup(GroupCreateDto group);

    Group addMembers(String groupId, List<String> memberIds);

    Message createGroupMessage(String senderUsername, String groupId, String content, List<MultipartFile> attachments);

    Message sendMessage(String id);

    Group findGroupById(String groupId);
    List<Group> getGroupsByUserId();
    List<InboxMessageDto> getMessagesByGroupId(String groupId);
    Group removeMemberFromGroup(String groupId, String memberId);
}
