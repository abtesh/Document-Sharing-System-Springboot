package com.LIB.MeesagingSystem.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BSRequest {
    private List<String>  groupID;
   private String mobile;
    private boolean isActive;
    private String firstName;
    private String middleName;
    private String lastName;
    private String password;
    private String email;

    }


