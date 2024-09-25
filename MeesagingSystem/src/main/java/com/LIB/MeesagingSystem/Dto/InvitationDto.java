package com.LIB.MeesagingSystem.Dto;


import lombok.Data;
import java.util.List;

@Data
public class InvitationDto {
    private List<String> memberIDs; // List of email addresses (single or multiple)
    private String subject;
    private String message;
    private String senderEmail; // Email address of the sender
    private String meetingLink; // Link to the meeting
    private String meetingTime; // Scheduled time of the meeting in ISO 8601 format
}
