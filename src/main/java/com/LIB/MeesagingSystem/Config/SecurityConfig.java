package com.LIB.MeesagingSystem.Config;

import com.LIB.MeesagingSystem.Model.BoardSecretary;
import com.LIB.MeesagingSystem.Service.BoardSecretaryService;
import com.LIB.MeesagingSystem.Service.Impl.JwtService;
import com.LIB.MeesagingSystem.Service.Impl.UsersService;
import com.LIB.MeesagingSystem.enums.Role;
import com.LIB.MeesagingSystem.filters.AuthenticationFilter;
import com.LIB.MeesagingSystem.filters.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${ldap.username}")
    private String username;
    @Value("${ldap.password}")
    private String password;
    @Value("${ldap.url}")
    private String url;
    @Value("${ldap.search-base}")
    private String baseSearch;
    private final UsersService usersService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final BoardSecretaryService boardSecretaryService;

    @Bean
    public JwtService getJwtService() {
        return new JwtService();
    }
    @Bean
    public LdapTemplate ldapTemplate() {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource());
        ldapTemplate.setIgnoreNameNotFoundException(true);
        return ldapTemplate;

    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(url);
        ldapContextSource.setUserDn(username);
        ldapContextSource.setPassword(password);
        ldapContextSource.setReferral("ignore");

        final Map<String, Object> envProps = new HashMap<>();
        envProps.put("java.naming.ldap.attributes.binary","objectGUID");
        ldapContextSource.setBaseEnvironmentProperties(envProps);
        return ldapContextSource;
    }

    @Bean
    AuthenticationManager authManager(BaseLdapPathContextSource source) {
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(source);
        factory.setUserSearchBase(baseSearch);
        factory.setUserSearchFilter("userPrincipalName={0}");
        return factory.createAuthenticationManager();
    }

    private static final String[] WHITE_LIST_URL = {
            "/api/login",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/error",
            "/email/files/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authManager(contextSource());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WHITE_LIST_URL).permitAll() // Permit all requests to WHITE_LIST_URL
                        .anyRequest().hasAuthority("ROLE_ALL_ACCESS")
// Grant access to all other endpoints with ROLE_ALL_ACCESS
                )
                .addFilterBefore(authenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager){
        createDefaultUser();
        return new AuthenticationFilter(authenticationManager,ldapTemplate(), getJwtService(),usersService,baseSearch,objectMapper(),boardSecretaryService);
    }


    public void createDefaultUser() {
        BoardSecretary boardSecretary = boardSecretaryService.getExternalUser("BSd9b0f7a2e67151d65d6bfa");
        if(boardSecretary==null)
        {
            boardSecretary = new BoardSecretary();
            boardSecretary.setId("BSd9b0f7a2e67151d65d6bfa");
            boardSecretary.setRole(Role.BOARD_SECRETARY_ADMIN);
            boardSecretary.setCreatedDate(new Date());
            boardSecretary.setUpdatedDate(new Date());
            boardSecretary.setActive(true);
            boardSecretary.setFirstName("admin");
            boardSecretary.setMiddleName("admin");
            boardSecretary.setLastName("anbesabank");
            boardSecretary.setMobile("09000000000");
            boardSecretary.setPassword("$2a$12$WZu8TqeZkYmsg3ex77IoYuFWYesBh.UMHGGRQ52PLim29soPoNj8S");
            boardSecretary.setEmail("board.secritary.admin@anbesabank.com");
            boardSecretaryService.saveAdminBoardSecretary(boardSecretary);
        }
    }

}
