package com.LIB.MeesagingSystem.filters;


import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Model.BoardSecretary;
import com.LIB.MeesagingSystem.Model.Users;
import com.LIB.MeesagingSystem.Repository.BoardSecretaryRepo;
import com.LIB.MeesagingSystem.Service.Impl.JwtService;
import com.LIB.MeesagingSystem.Service.Impl.UsersService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsersService usersService;
    private final BoardSecretaryRepo boardSecretaryRepo;

    @Override


    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            try {
                username = jwtService.extractUsername(jwtToken);
            } catch (Exception e) {
                // Log or handle the token parsing/validation failure
                log.error(e.getMessage());
                throw new AccessDeniedException("Invalid JWT token");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Users userDetails = this.usersService.loadUserByUsername(username);
            if (userDetails == null) {
                BoardSecretary boardSecretary = boardSecretaryRepo.findByEmailAndIsActive(username, true).orElseThrow(() -> new UsernameNotFoundException("user not found"));
                LdapUserDTO ldapUserDTO = LdapUserDTO.builder().uid(boardSecretary.getId()).name(boardSecretary.getFirstName()).email(boardSecretary.getEmail()).build();
                if (jwtService.isTokenValid(jwtToken, boardSecretary)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            ldapUserDTO, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ALL_ACCESS")));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } else {
                LdapUserDTO ldapUserDTO = LdapUserDTO.builder().uid(userDetails.getId()).name(userDetails.getName()).email(userDetails.getEmail()).build();
                if (jwtService.isTokenValid(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            ldapUserDTO, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ALL_ACCESS")));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }


        }

        chain.doFilter(request, response);
    }

}
