package com.LIB.MeesagingSystem.Service;


import com.LIB.MeesagingSystem.Dto.InboxMessageDto;
import com.LIB.MeesagingSystem.Dto.OutboxMessageDto;
import com.LIB.MeesagingSystem.Model.Message;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


public interface MessageService {
    Message createMessage( String receiverUsername, String content, List<MultipartFile> attachments);
    Message sendMessage(String id);
    Message getMessage(String id);
    List<Message> getMessagesBetweenUsers(String senderId, String receiverId);
    List<InboxMessageDto> getMessagesByUserId();
    Message findMessageByIdForUser(String messageId, String username);
    List<OutboxMessageDto> getMessagesBySenderId();
    Message getMessageWithAttachmentCheck(String messageId, String attachmentId, String userId);
    void deleteMessageById(String id);
    Message resendMessage(String messageId);
    List<Message> searchInboxMessages(String senderEmail, String attachmentName);
}


