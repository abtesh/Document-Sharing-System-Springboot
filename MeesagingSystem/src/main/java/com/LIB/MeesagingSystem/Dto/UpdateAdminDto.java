package com.LIB.MeesagingSystem.Dto;

import lombok.Data;


import java.util.List;

@Data
public class UpdateAdminDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobile;
}
