package com.LIB.MeesagingSystem.Dto.SecurityDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserLoginRequestDto {
    private String email;
    private String password;
}