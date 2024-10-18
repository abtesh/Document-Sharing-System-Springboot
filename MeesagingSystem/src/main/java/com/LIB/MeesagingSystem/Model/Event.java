package com.LIB.MeesagingSystem.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
public class Event {

    private String id;
    private String title;
    private String description;
    private String location;
    private String meetingLink;
    private List<String> participants;  // Change to List<String>
    private String organizerName;
    private String organizerEmail;
    private Date start;
    private Date end;
    private Date createdDate;
    private Date updatedDate;
    private Boolean isCanceled = false;
}
