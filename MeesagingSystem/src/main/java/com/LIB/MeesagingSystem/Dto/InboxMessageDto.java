package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InboxMessageDto {
    private String id;
    private String senderEmail;
    private String content;
    private Date date;
    private List<String> attachments;
    private boolean isDownloadable;
}
