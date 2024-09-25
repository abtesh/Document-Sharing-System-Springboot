package com.LIB.MeesagingSystem.Model;


import com.LIB.MeesagingSystem.Model.Enums.RecipientTypes;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


@Document(collection = "messages")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Message {
    @Id
    private String id;
    private String documentId = UUID.randomUUID().toString();
    private String senderId;
    private String receiverId;
    private String groupId;
    private String content;
    private List<String> attachments = new ArrayList<>();
    //Group or Individual Recipients
    private RecipientTypes recipientType;
    private Date date;
    private boolean isDraft;
}
