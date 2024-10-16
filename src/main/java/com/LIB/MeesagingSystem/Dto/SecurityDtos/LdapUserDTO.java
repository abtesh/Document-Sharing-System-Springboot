package com.LIB.MeesagingSystem.Dto.SecurityDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LdapUserDTO {
    private String email;
    private String name;
    private String uid;
}
