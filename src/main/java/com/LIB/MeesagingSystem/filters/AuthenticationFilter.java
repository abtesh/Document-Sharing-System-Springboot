package com.LIB.MeesagingSystem.filters;


import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Model.BoardSecretary;
import com.LIB.MeesagingSystem.Service.BoardSecretaryService;
import com.LIB.MeesagingSystem.Service.Impl.JwtService;
import com.LIB.MeesagingSystem.Service.Impl.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;
    private final LdapTemplate ldapTemplate;
    private final JwtService jwtService;
    private final UsersService usersService;
    private final String searchBase;
    private final BoardSecretaryService boardSecretaryService;


    public AuthenticationFilter(AuthenticationManager authenticationManager, LdapTemplate ldapTemplate, JwtService jwtService, UsersService usersService, String searchBase, ObjectMapper objectMapper, BoardSecretaryService boardSecretaryService) {
        super("/api/login");
        this.ldapTemplate = ldapTemplate;
        this.jwtService = jwtService;
        this.usersService = usersService;
        this.searchBase = searchBase;
        this.objectMapper = objectMapper;
        this.boardSecretaryService = boardSecretaryService;
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), HashMap.class);
        String email = credentials.get("email");
        String password = credentials.get("password");
        // Store credentials in session
        request.getSession().setAttribute("email", email);
        request.getSession().setAttribute("password", password);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);
        return getAuthenticationManager().authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        LdapUserDetails ldapUserDetails = (LdapUserDetails) authResult.getPrincipal();
        LdapUserDTO user = searchByDn(ldapUserDetails.getDn());
        usersService.saveUsers(user.getEmail(),user.getName(),user.getUid());
        var jwtToken = jwtService.generateToken(userDetails, user);
        response.setContentType("application/json");
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Authentication successful!");
        responseBody.put("isSuccessful", true);
        responseBody.put("statusCode", 200);
        responseBody.put("data", jwtToken);
        response.setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        String email = request.getSession().getAttribute("email").toString();
        String password = request.getSession().getAttribute("password").toString();
        if (loginForExternalUsers(email,password,response)){
            BoardSecretary boardSecretary = boardSecretaryService.getExternalUser(email,true);
            LdapUserDTO ldapUserDTO = LdapUserDTO.builder().uid(boardSecretary.getId()).email(boardSecretary.getEmail()).name(boardSecretary.getFirstName()).build();
            var jwtToken = jwtService.generateToken(boardSecretary, ldapUserDTO);
            response.setContentType("application/json");
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Authentication successful!");
            responseBody.put("isSuccessful", true);
            responseBody.put("statusCode", 200);
            responseBody.put("data", jwtToken);
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            response.getWriter().write(jsonResponse);
        }else {
            log.error("Authentication failed: {}", failed.toString());
            response.setContentType("application/json");
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Authentication unsuccessful!");
            responseBody.put("isSuccessful", false);
            responseBody.put("statusCode", 401);
            responseBody.put("data","Authentication failed: " + failed.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            response.getWriter().write(jsonResponse);
        }

    }


//    public LdapUserDTO getUserObjectGUIDByEmail(String email) {
//        EqualsFilter filter = new EqualsFilter("userPrincipalName", email);
//
//        List<LdapUserDTO> users = ldapTemplate.search(searchBase, filter.encode(), new AttributesMapper<LdapUserDTO>() {
//            @Override
//            public LdapUserDTO mapFromAttributes(Attributes attributes) throws NamingException {
//                byte[] objectGuidBytes = (byte[]) attributes.get("objectGUID").get();
//                String objectGUID = convertBytesToGUID(objectGuidBytes);
//                String username = (String) attributes.get("cn").get();
//                return LdapUserDTO.builder().email(email).name(username).uid(objectGUID).build();
//            }
//        });
//
//        // Assuming only one result should match the email
//        return users.isEmpty() ? null : users.get(0);
//    }

    private String convertBytesToGUID(byte[] guidBytes) {
        UUID uuid = UUID.fromString(
                String.format("%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                        guidBytes[3] & 255,
                        guidBytes[2] & 255,
                        guidBytes[1] & 255,
                        guidBytes[0] & 255,
                        guidBytes[5] & 255,
                        guidBytes[4] & 255,
                        guidBytes[7] & 255,
                        guidBytes[6] & 255,
                        guidBytes[8] & 255,
                        guidBytes[9] & 255,
                        guidBytes[10] & 255,
                        guidBytes[11] & 255,
                        guidBytes[12] & 255,
                        guidBytes[13] & 255,
                        guidBytes[14] & 255,
                        guidBytes[15] & 255));
        log.info(uuid.toString());
        return uuid.toString();
    }

    @SneakyThrows
    public Boolean loginForExternalUsers(String email, String password, HttpServletResponse response) {
        return (boardSecretaryService.externalUserLogin(email, password)) ;


    }

    public LdapUserDTO searchByDn(String dn) {

        try {
            return ldapTemplate.lookup(escapeDn(dn), (AttributesMapper<LdapUserDTO>) attrs -> {
                LdapUserDTO user = new LdapUserDTO();
                byte[] guidBytes = (byte[]) attrs.get("objectGUID").get();
                user.setUid(convertBytesToGUID(guidBytes));
                user.setEmail(attrs.get("userPrincipalName") != null ? attrs.get("userPrincipalName").get().toString() : null);
                user.setName(attrs.get("displayName") != null ? attrs.get("displayName").get().toString() : null);
                return user;
            });
        } catch (Exception e) {
            log.error("Error retrieving LDAP user for dn: {} - {}", dn, e.getMessage());
            return null;
        }
    }


    public String escapeDn(String dn) {
        return dn.replace("/", "\\/");
    }



}
