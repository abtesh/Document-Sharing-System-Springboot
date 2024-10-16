package com.LIB.MeesagingSystem.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BODMembersDto{
@NotNull
private String firstName;
@NotNull
private String middleName;
@NotNull
private String lastName;
@NotNull
private String email;
private String mobile;
private String address;
}