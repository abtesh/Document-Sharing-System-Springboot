package com.LIB.MeesagingSystem.Controller;


import com.LIB.MeesagingSystem.Dto.GroupCreateDto;
import com.LIB.MeesagingSystem.Dto.GroupMessageDto;
import com.LIB.MeesagingSystem.Dto.InboxMessageDto;
import com.LIB.MeesagingSystem.Dto.membersDTO;
import com.LIB.MeesagingSystem.Model.Group;
import com.LIB.MeesagingSystem.Model.Message;
import com.LIB.MeesagingSystem.Model.Users;
import com.LIB.MeesagingSystem.Repository.GroupRepository;
import com.LIB.MeesagingSystem.Repository.MessageRepository;
import com.LIB.MeesagingSystem.Repository.UserRepository;
import com.LIB.MeesagingSystem.Service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 *  Controller class for operations related to groups
 */


@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupServiceImpl;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody GroupCreateDto group) {
        try {
            Group createdGroup = groupServiceImpl.createGroup(group);
            return ResponseEntity.ok(createdGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/users")
    public ResponseEntity<List<Users>> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    @PutMapping("/{groupId}/addMembers")
    public Group addMembers(@PathVariable String groupId, @RequestBody List<String> memberIds) {
        return groupServiceImpl.addMembers(groupId, memberIds);
    }
    @PostMapping("/createMessage")
    public Message createGroupMessage(@ModelAttribute GroupMessageDto message) {
        return groupServiceImpl.createGroupMessage(message.getSenderEmail(),
                message.getGroupId(),
                message.getContent(),
                message.getAttachments());

    }
    @PutMapping("/send/{groupId}")
    public ResponseEntity<?> sendMessage(@PathVariable("groupId") String groupId) {
        try {
            Message sentMessage = groupServiceImpl.sendMessage(groupId);
            return ResponseEntity.ok(sentMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/getAll")
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> group = groupRepository.findAll();
        return ResponseEntity.ok(group);
    }
    @GetMapping("/search/{groupId}")
    public ResponseEntity<Group> getGroupById(@PathVariable String groupId) {
        Group group = groupServiceImpl.findGroupById(groupId);
        return ResponseEntity.ok(group);
    }
    @GetMapping("/user")
    public ResponseEntity<List<Group>> getGroupsByUserId() {
        List<Group> groups = groupServiceImpl.getGroupsByUserId();
        return ResponseEntity.ok(groups);
    }
    @GetMapping("/inbox/{groupId}")
    public ResponseEntity<List<InboxMessageDto>> getMessagesByGroupId(@PathVariable String groupId) {
        List<InboxMessageDto> messages = groupServiceImpl.getMessagesByGroupId(groupId);
        return ResponseEntity.ok(messages);
    }
    @DeleteMapping("/removeMember")
    public ResponseEntity<String> deleteMemberFromGroup(@RequestParam String groupId, @RequestParam String memberId) {
        groupServiceImpl.removeMemberFromGroup(groupId, memberId);
        return ResponseEntity.ok("group Deleted Successfully");
    }
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<membersDTO>> getMembersOfGroup(@PathVariable String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        List<String> memberIds = group.getMembers();
        List<membersDTO> memberDtos = memberIds.stream()
                .map(memberId -> {
                    Users member = userRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return new membersDTO(member.getId(), member.getName(), member.getEmail());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(memberDtos);
    }
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<InboxMessageDto>> getGroupMessages(@PathVariable String groupId) {
        try {
            List<InboxMessageDto> messageDtos = groupServiceImpl.getMessagesByGroupId(groupId);
            return ResponseEntity.ok(messageDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        }
    }

}


