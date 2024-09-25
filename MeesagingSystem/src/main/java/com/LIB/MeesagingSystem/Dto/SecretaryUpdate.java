package com.LIB.MeesagingSystem.Dto;


import lombok.Data;
import java.util.List;

@Data
public class SecretaryUpdate {
    private List<String> groupID;
    private String mobile;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private boolean active;
}
