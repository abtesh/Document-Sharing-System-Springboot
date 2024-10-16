package com.LIB.MeesagingSystem.Model;


import com.LIB.MeesagingSystem.Model.Enums.GroupType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */



@Document(collection = "groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Group {
    @Id
    private String id;
    private String name;
    private String makerId;
    private String mail;
    //private String checkerId;
    private String description;
    private String title;
    private Date creationDate;
    private List<String> members;
    private GroupType groupType;
}
