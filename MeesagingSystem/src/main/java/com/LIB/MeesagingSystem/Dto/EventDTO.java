package com.LIB.MeesagingSystem.Dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class EventDTO {

    private String title;
    private String description;
    private String location;
    private String meetingLink;
    private List<String> participants;  // Change to List<String>
    private String organizerName;
    private String organizerEmail;
    private Date start;
    private Date end;

    // Getters and Setters
}
