package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LdapUser {
    private String id;
    private String cn;
    private String mail;
    private String displayName;
}
