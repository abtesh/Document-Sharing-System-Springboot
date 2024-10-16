package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutboxMessageDto {
    private String id;
    private String senderEmail;
    private String receiverEmail;
    private String content;
    private Date date;
    private List<String> attachments;
}

