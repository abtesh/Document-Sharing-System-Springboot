package com.LIB.MeesagingSystem.Controller;


import com.LIB.MeesagingSystem.Dto.SecurityDtos.GenericResponseDto;

import com.LIB.MeesagingSystem.Service.LdapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *  @author Abenezer Teshome  - Date 17/sept/2024
 *  Controller class to fetch new groups created on the LDAP
 */

@RestController
@RequestMapping("/api/ldap")
@RequiredArgsConstructor
public class LdapController {
    private final LdapService ldapService;

    @GetMapping
    public ResponseEntity<GenericResponseDto<Void>> fetchLdapGroupsAndMembers(){
        return ResponseEntity.ok(ldapService.fetchGroupAndTheirMembersFromLdap());
    }
}
