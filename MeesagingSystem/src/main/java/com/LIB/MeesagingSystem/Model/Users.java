package com.LIB.MeesagingSystem.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "user")
public class Users implements UserDetails {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    private String phone;
    private String name;
    private boolean isActive;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }
}
