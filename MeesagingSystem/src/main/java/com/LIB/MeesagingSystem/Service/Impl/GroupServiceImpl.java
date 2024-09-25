package com.LIB.MeesagingSystem.Service.Impl;


import com.LIB.MeesagingSystem.Dto.GroupCreateDto;
import com.LIB.MeesagingSystem.Dto.InboxMessageDto;
import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Model.Enums.RecipientTypes;
import com.LIB.MeesagingSystem.Model.FilePrivilege;
import com.LIB.MeesagingSystem.Model.Group;
import com.LIB.MeesagingSystem.Model.Message;
import com.LIB.MeesagingSystem.Model.Users;
import com.LIB.MeesagingSystem.Repository.FilePrivilegeRepository;
import com.LIB.MeesagingSystem.Repository.GroupRepository;
import com.LIB.MeesagingSystem.Repository.MessageRepository;
import com.LIB.MeesagingSystem.Repository.UserRepository;
import com.LIB.MeesagingSystem.Service.GroupService;
import com.LIB.MeesagingSystem.Utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 *  This service implementation class creates new groups, add members to the groups,
 *  create messages to groups and send messages
 */


@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FilePrivilegeRepository filePrivilegeRepository;
    @Value("${file.storage-path}")
    private String storagePath;


    public Group createGroup(GroupCreateDto groupDto) {
        // Check if all member IDs exist in the system
        for (String memberId : groupDto.getMembers()) {
            if (!userRepository.existsById(memberId)) {
                throw new RuntimeException("User with ID: " + memberId + " does not exist in the system");
            }
        }
        // Fetch the maker's ID from the token (assumed to be from the security context)
        LdapUserDTO ldapUser = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String senderId = ldapUser.getUid();
        groupDto.setMakerId(senderId);
        groupDto.setCreateDate(new Date());

        Group group = getGroup(groupDto, senderId);
        return groupRepository.save(group);
    }
    private static Group getGroup(GroupCreateDto groupDto, String senderId) {
        Group group = new Group();
        group.setName(groupDto.getGroupName());
        group.setMakerId(groupDto.getMakerId());
        group.setCreationDate(groupDto.getCreateDate());
        group.setDescription(groupDto.getGroupDescription());
        // Get the list of members from the DTO and add the maker's ID
        List<String> members = new ArrayList<>(groupDto.getMembers());
        members.add(senderId);  // Add the maker to the group
        // Set the updated list of members to the group
        group.setMembers(members);
        return group;
    }
    public Group addMembers(String groupId, List<String> memberEmails) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<String> existingMembers = group.getMembers();
        List<String> newMembers = new ArrayList<>();

        for (String memberEmail : memberEmails) {
            // Find user by email
            Users user = userRepository.findByEmail(memberEmail)
                    .orElseThrow(() -> new RuntimeException("User with email " + memberEmail + " does not exist"));

            String memberId = user.getId();

            // Check if the user is already a member of the group
            if (existingMembers.contains(memberId)) {
                throw new RuntimeException("User with email " + memberEmail + " is already a member of the group");
            } else {
                newMembers.add(memberId);
            }
        }

        group.getMembers().addAll(newMembers);
        return groupRepository.save(group);
    }
    public Message createGroupMessage(String senderEmail, String groupId, String content, List<MultipartFile> attachments) {
        // Find sender by email
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Users> sender = userRepository.findByEmail(user.getEmail());

        // Find group by ID
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (sender.isEmpty()) {
            throw new RuntimeException("Sender email not found");
        }

        // Handle attachments
        List<String> attachmentPaths = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            if (attachments.size() > 2) {
                String zipFilePath = compressAttachments(attachments);
                attachmentPaths.add(zipFilePath);
            } else {
                for (MultipartFile attachment : attachments) {
                    String filePath = FileUtils.saveAttachment2(attachment, storagePath);
                    attachmentPaths.add(filePath);
                }
            }
        }

        // Create message
        Message message = new Message();
        message.setSenderId(sender.get().getId());
        message.setGroupId(groupId);
        message.setContent(content);
        message.setDraft(true);
        message.setRecipientType(RecipientTypes.GROUP);
        message.setAttachments(attachmentPaths);
        message.setDate(new Date());

        // save message and return
        Message savedMessage = messageRepository.save(message);

        saveFilePrivilegesForGroupMessage(savedMessage);
        System.out.println("Saved Message: " + savedMessage.getId());
        return savedMessage;
    }

    public Message sendMessage(String id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!message.isDraft()) {
            throw new RuntimeException("Message is Already Sent and cannot be sent again");
        }
        // Mark the message as sent
        message.setDraft(false);
        message.setDate(new Date());

        // Save file privileges
        saveFilePrivilegesForGroupMessage(message);
        return messageRepository.save(message);
    }
    private void saveFilePrivilegesForGroupMessage(Message message) {
        List<FilePrivilege> filePrivileges = new ArrayList<>();
        String messageId = message.getId();

        for (String attachment : message.getAttachments()) {
            Optional<FilePrivilege> existingPrivilege = filePrivilegeRepository.findByAttachmentIdAndGroupId(attachment, message.getGroupId());

            FilePrivilege privilege;
            if (existingPrivilege.isPresent()) {
                privilege = existingPrivilege.get();
            } else {
                privilege = new FilePrivilege();
                privilege.setAttachmentId(attachment);
                privilege.setGroupId(message.getGroupId());
            }
            privilege.setMessageId(messageId);
            privilege.setCanView(true);
            privilege.setCanDownload(false); // Default to not allowing download
            filePrivileges.add(privilege);
        }

        filePrivilegeRepository.saveAll(filePrivileges);
    }

    public Group findGroupById(String groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
    }

    public List<Group> getGroupsByUserId() {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUid();
        List<Group> groups = groupRepository.findByMembersContaining(userId);
        if (groups.isEmpty()) {
            throw new RuntimeException("No groups found for the user");
        }
        return groups;
    }
    public Group removeMemberFromGroup(String groupId, String memberId) {
        Group group = findGroupById(groupId);
        group.getMembers().remove(memberId);
        return groupRepository.save(group);
    }
    //    public List<InboxMessageDto> getMessagesByGroupId(String groupId) {
//        Group group = groupRepository.findById(groupId)
//                .orElseThrow(() -> new RuntimeException("Group not found"));
//        // Fetch the currently authenticated user
//        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String userId = user.getUid();
//        // Fetch the user entity
//        Users currentUser = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Check if the user is a member of the group
//        if (!group.getMembers().contains(currentUser)) {
//            throw new RuntimeException("You are not a member of this group");
//        }
//        // Fetch and return the messages for the group
//        return messageRepository.findByGroupId(groupId)
//                .stream()
//                .map(this::mapToInboxMessageDto)
//                .collect(Collectors.toList());
//    }
//    private InboxMessageDto mapToInboxMessageDto(Message message) {
//        String senderId = message.getSenderId();
//        System.out.println("Mapping message ID: " + message.getId() + " with sender ID: " + senderId);
//
//        String senderEmail = userRepository.findById(senderId)
//                .map(Users::getEmail)
//                .orElse("Unknown Sender");
//
//        if (senderEmail.equals("Unknown Sender")) {
//            System.out.println("Warning: Sender not found for message ID: " + message.getId());
//        }
//
//        return new InboxMessageDto(senderEmail, message.getContent(), message.getDate(), message.getAttachments());
//    }
    public List<InboxMessageDto> getMessagesByGroupId(String groupId) {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUid();

        // Verify if the user is a member of the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        if (!group.getMembers().contains(userId)) {
            throw new RuntimeException("User is not a member of this group");
        }

        // Fetch the messages for the group
        List<Message> messages = messageRepository.findByGroupId(groupId);

        // Map messages to InboxMessageDto, including the sender's email
        return messages.stream()
                .map(this::mapToInboxMessageDto)  // Map each message to InboxMessageDto
                .collect(Collectors.toList());
    }

    private InboxMessageDto mapToInboxMessageDto(Message message) {
        String senderEmail = userRepository.findById(message.getSenderId())
                .map(Users::getEmail)
                .orElse("Unknown Sender");

        if (senderEmail.equals("Unknown Sender")) {
            System.out.println("Warning: Sender not found for message ID: " + message.getId());
        }

        return new InboxMessageDto(message.getId(), senderEmail, message.getContent(), message.getDate(), message.getAttachments());
    }

    public String compressAttachments(List<MultipartFile> attachments) {
        // Generate a unique name for the zip file
        String zipFileName = "attachments_" + UUID.randomUUID().toString() + ".zip";
        Path zipFilePath = Paths.get(storagePath, zipFileName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            for (MultipartFile attachment : attachments) {
                ZipEntry zipEntry = new ZipEntry(attachment.getOriginalFilename());
                zos.putNextEntry(zipEntry);

                try (InputStream inputStream = attachment.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress attachments", e);
        }
        // Return only the zip file name, not the full path
        return zipFileName;
    }


}
