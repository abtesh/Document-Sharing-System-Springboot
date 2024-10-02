package com.LIB.MeesagingSystem.Controller;


import com.LIB.MeesagingSystem.Dto.InboxMessageDto;
import com.LIB.MeesagingSystem.Dto.MessageRequest;
import com.LIB.MeesagingSystem.Dto.MessageSearchRequestDto;
import com.LIB.MeesagingSystem.Dto.OutboxMessageDto;
import com.LIB.MeesagingSystem.Model.Message;
import com.LIB.MeesagingSystem.Model.Users;
import com.LIB.MeesagingSystem.Repository.UserRepository;
import com.LIB.MeesagingSystem.Service.Impl.MessageServiceImpl;
import com.LIB.MeesagingSystem.Service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 *  Controller class to create Messages, get Messages and Fetch Messages between Users
 */


@RestController
@RequestMapping("/messenger")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageServiceImpl;
    private final UserRepository userRepository;
    private final MessageServiceImpl messageService;


    @PostMapping("/createUser")
    public String createUser(@RequestBody Users user) {
        userRepository.save(user);
        return "User created";
    }

    @PostMapping("/create")
    public Message createMessage(@ModelAttribute MessageRequest message) {
        System.out.println("Received message request: " + message);
        return messageServiceImpl.createMessage(
                message.getReceiverEmail(),
                message.getContent(),
                message.getAttachments());
    }
    @PostMapping("/resend/{messageId}")
    public ResponseEntity<Void> resendMessage(@PathVariable String messageId) {
        try {
            messageServiceImpl.resendMessage(messageId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping("/send/{objectId}")
    public ResponseEntity<?> sendMessage(@PathVariable("objectId") String objectId) {
        try {
            Message sentMessage = messageServiceImpl.sendMessage(objectId);
            return ResponseEntity.ok(sentMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessage(@PathVariable("id") String id) {
        Message message = messageServiceImpl.getMessage(id);
        return ResponseEntity.ok(message);
    }
    @GetMapping("/inbox")
    public ResponseEntity<List<InboxMessageDto>> getMessagesByUserId() {
        List<InboxMessageDto> messages = messageServiceImpl.getMessagesByUserId();
        return ResponseEntity.ok(messages);
    }
    @GetMapping("/sent")
    public ResponseEntity<List<OutboxMessageDto>> getMessagesBySenderId() {
        List<OutboxMessageDto> messages = messageServiceImpl.getMessagesBySenderId();
        return ResponseEntity.ok(messages);
    }
    @GetMapping("/search")
    public ResponseEntity<Message> getMessageById(@RequestBody MessageSearchRequestDto searchRequest) {
        Message message = messageServiceImpl.findMessageByIdForUser(
                searchRequest.getMessageId(),
                searchRequest.getUsername()
        );
        return ResponseEntity.ok(message);
    }
    @DeleteMapping("/delete/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable("messageId") String messageId) {
        messageServiceImpl.deleteMessageById(messageId);
        return ResponseEntity.ok("Message and associated file privileges deleted successfully");
    }
    @GetMapping("/searchInbox")
    public ResponseEntity<List<Message>> searchInbox(
            @RequestParam(required = false) String senderEmail,
            @RequestParam(required = false) String attachmentName) {

        List<Message> messages = messageServiceImpl.searchInboxMessages(senderEmail, attachmentName);
        return ResponseEntity.ok(messages);
    }
    // New endpoint to get unread message count (for notification)
    @GetMapping("/inbox/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount() {
        long unreadCount = messageService.countUnreadMessage();
        return ResponseEntity.ok(unreadCount);
    }
    // New endpoint to mark a message as read
    @GetMapping("/inbox/mark-as-read")
    public ResponseEntity<Void> markMessagesAsRead() {
        messageService.markMessageAsRead();
        return ResponseEntity.ok().build();
    }
}

