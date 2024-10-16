package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LdapGroup {
    private String id;
    private String cn;
    private String mail;
    private String managedBy;
    private LdapUser manager;
    private String description;
    private List<LdapUser> members;
    private List<String> membersName;

}
