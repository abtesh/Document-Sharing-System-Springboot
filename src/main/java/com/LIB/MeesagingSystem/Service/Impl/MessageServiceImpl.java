package com.LIB.MeesagingSystem.Service.Impl;

import com.LIB.MeesagingSystem.Dto.InboxMessageDto;
import com.LIB.MeesagingSystem.Dto.OutboxMessageDto;
import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Model.Enums.RecipientTypes;
import com.LIB.MeesagingSystem.Model.FilePrivilege;
import com.LIB.MeesagingSystem.Model.Message;
import com.LIB.MeesagingSystem.Model.Users;
import com.LIB.MeesagingSystem.Repository.FilePrivilegeRepository;
import com.LIB.MeesagingSystem.Repository.MessageRepository;
import com.LIB.MeesagingSystem.Repository.UserRepository;
import com.LIB.MeesagingSystem.Service.MessageService;
import com.LIB.MeesagingSystem.Utils.FileUtils;
import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 *  This service Implementation class creates messages for Individuals, sends messages,
 *  Creates and Updates privilages for users and deletes messages
 */



@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FilePrivilegeRepository filePrivilegeRepository;
    @Value("${file.storage-path}")
    private String storagePath;

    public Message  createMessage(String receiverEmail, String content, List<MultipartFile> attachments) {
        // Find sender and receiver
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Users> sender = userRepository.findByEmail(user.getEmail());
        Optional<Users> receiver = userRepository.findByEmail(receiverEmail);

        if (sender.isEmpty() || receiver.isEmpty()) {
            throw new RuntimeException("Sender or receiver not found");
        }// Handle attachments
        List<String> attachmentPaths = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            if (attachments.size() > 2) {
                String zipFilePath = compressAttachments(attachments);
                attachmentPaths.add(zipFilePath);
            }else {
                for (MultipartFile attachment : attachments) {
                    String filePath = FileUtils.saveAttachment2(attachment, storagePath);
                    attachmentPaths.add(filePath);
                }
            }
//            for (MultipartFile attachment : attachments) {
//                String filePath = FileUtils.saveAttachment(attachment, storagePath);
//                attachmentPaths.add(filePath);
//            }
        }
        // Create message
        Message message = new Message();
        message.setSenderId(sender.get().getId());
        message.setReceiverId(receiver.get().getId());
        message.setContent(content);
        message.setDraft(true);
        message.setRecipientType(RecipientTypes.INDIVIDUAL);
        message.setAttachments(attachmentPaths);
        message.setDate(new Date());

        // Save message and return
        Message savedMessage = messageRepository.save(message);

        saveFilePrivilegesForMessage(message);
        System.out.println("Message saved with ID: " + savedMessage.getId());
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
        saveFilePrivilegesForMessage(message);
        return messageRepository.save(message);
    }

    private void saveFilePrivilegesForMessage(Message message) {
        List<FilePrivilege> filePrivileges = new ArrayList<>();
        String messageId = message.getId();

        for (String attachment : message.getAttachments()) {
            Optional<FilePrivilege> existingFilePrivilege = filePrivilegeRepository.findByAttachmentIdAndUserId(attachment, message.getReceiverId());

            FilePrivilege privilege;
            if(existingFilePrivilege.isPresent()){
                privilege = existingFilePrivilege.get();
            }else {
                privilege = new FilePrivilege();
                privilege.setAttachmentId(attachment);
                privilege.setUserId(message.getReceiverId());
            }
            privilege.setMessageId(messageId);
            privilege.setCanView(true);
            privilege.setCanDownload(true);
            filePrivileges.add(privilege);
        }
        filePrivilegeRepository.saveAll(filePrivileges);
    }
    public Message getMessage(String id) {
        return messageRepository.findById(id).orElseThrow(() -> new RuntimeException("Message not found"));
    }
    public List<Message> getMessagesBetweenUsers(String senderId, String receiverId){
        return messageRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    }
    public List<InboxMessageDto> getMessagesByUserId() {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUid();
        List<Message> messages = messageRepository.findByReceiverId(userId);
        return messages.stream().map(message -> {

            String senderEmail = userRepository.findById(message.getSenderId())
                    .map(Users::getEmail)
                    .orElse(null);
            List<String> attachments = message.getAttachments();
            return new InboxMessageDto(message.getId(), senderEmail, message.getContent(), message.getDate(), attachments);
        }).collect(Collectors.toList());
    }

    public List<OutboxMessageDto> getMessagesBySenderId() {
        // Get the authenticated user's ID
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUid();

        // Find messages by senderId and receiverId (to exclude group messages)
        List<Message> messages = messageRepository.findBySenderIdAndReceiverIdIsNotNull(userId);

        // Map each message to OutboxMessageDto
        return messages.stream().map(message -> {
            String senderEmail = userRepository.findById(message.getSenderId())
                    .map(Users::getEmail)
                    .orElse(null);
            String receiverEmail = userRepository.findById(message.getReceiverId())
                    .map(Users::getEmail)
                    .orElse(null);

            return new OutboxMessageDto(
                    message.getId().toString(),
                    senderEmail,
                    receiverEmail,
                    message.getContent(),
                    message.getDate(),
                    message.getAttachments()
            );
        }).collect(Collectors.toList());
    }

    public Message findMessageByIdForUser(String messageId, String username) {
        Users user = userRepository.findByUsername(username);

        return messageRepository.findById(messageId)
                .filter(message -> message.getSenderId().equals(user.getId()) ||
                        message.getReceiverId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Message not found or access denied"));
    }
    public Message getMessageWithAttachmentCheck(String messageId, String attachmentId, String userId) {
        Optional<FilePrivilege> privilege = filePrivilegeRepository.findByMessageIdAndAttachmentIdAndUserId(messageId, attachmentId, userId);
        if (privilege.isPresent()) {
            return messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));
        } else {
            throw new RuntimeException("Access Denied");
        }
    }

    public void deleteMessageById(String id) {
        messageRepository.deleteById(id);
        filePrivilegeRepository.deleteByMessageId(id);
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
    public Message resendMessage(String messageId) {
        // Find the original message by its ID
        Optional<Message> originalMessageOptional = messageRepository.findById(messageId);

        if (originalMessageOptional.isEmpty()) {
            throw new RuntimeException("Message not found");
        }

        Message originalMessage = originalMessageOptional.get();

        // Find sender from the current authenticated user
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Users> sender = userRepository.findByEmail(user.getEmail());

        if (sender.isEmpty()) {
            throw new RuntimeException("Sender not found");
        }

        // Clone the original message (excluding messageId) and mark it as a new sent message
        Message newMessage = new Message();
        newMessage.setSenderId(sender.get().getId());
        newMessage.setReceiverId(originalMessage.getReceiverId());
        newMessage.setContent(originalMessage.getContent());
        newMessage.setDraft(false);
        newMessage.setRecipientType(originalMessage.getRecipientType());
        newMessage.setDate(new Date());

        // Use the original attachment paths
        List<String> attachmentPaths = new ArrayList<>(originalMessage.getAttachments());
        newMessage.setAttachments(attachmentPaths);

        // Check for existing messages with the same attachments
        for (String attachment : attachmentPaths) {
            Optional<Message> existingMessage = messageRepository.findByAttachmentsContaining(attachment);
            if (existingMessage.isPresent()) {
                // Update existing message
                Message messageToUpdate = existingMessage.get();
                messageToUpdate.setContent(newMessage.getContent());
                messageToUpdate.setDate(newMessage.getDate());
                messageRepository.save(messageToUpdate);
            } else {
                // Save the new message if not found
                messageRepository.save(newMessage);
            }
        }

        // Save privileges for the message
        for (String attachment : attachmentPaths) {
            updateOrCreateFilePrivilege(attachment, sender.get().getId());
        }

        return newMessage;
    }

    private void updateOrCreateFilePrivilege(String attachmentId, String userId) {
        Optional<FilePrivilege> optionalPrivilege = filePrivilegeRepository.findByAttachmentIdAndUserId(attachmentId, userId);
        FilePrivilege privilege;
        if (optionalPrivilege.isPresent()) {
            privilege = optionalPrivilege.get();
        } else {
            privilege = new FilePrivilege();
            privilege.setAttachmentId(attachmentId);
            privilege.setUserId(userId);
        }
        // Set desired privileges
        privilege.setCanView(true);
        privilege.setCanDownload(true);

        filePrivilegeRepository.save(privilege);
    }
    public List<Message> searchInboxMessages(String senderEmail, String attachmentName) {
        List<Message> foundMessages = new ArrayList<>();

        // Search by sender's email
        if (senderEmail != null && !senderEmail.isEmpty()) {
            Optional<Users> userOptional = userRepository.findByEmail(senderEmail);
            if (userOptional.isPresent()) {
                String senderId = userOptional.get().getId();
                foundMessages.addAll(messageRepository.findBySenderId(senderId));
            }
        }
        // Search by attachment name
        if (attachmentName != null && !attachmentName.isEmpty()) {
            Optional<Message> messageOptional = messageRepository.findByAttachmentsContaining(attachmentName);
            messageOptional.ifPresent(foundMessages::add);  // Add the message if it exists
        }
        return foundMessages;
    }
}

