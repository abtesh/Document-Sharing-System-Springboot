package com.LIB.MeesagingSystem.Controller;
import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Model.EmailHistory;
import com.LIB.MeesagingSystem.Service.EmailHistoryService;
import com.LIB.MeesagingSystem.Dto.EmailHistoryRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing email history operations.
 * Provides a REST endpoint to retrieve email history records.
 *
 * <p>
 * This controller allows fetching a list of all email history records from the system.
 * </p>
 *
 * @author Elizabeth Hagos
 */
@RestController
@RequestMapping("/email")
public class EmailHistoryController {

    @Autowired
    private EmailHistoryService emailHistoryService;

    private ResourceLoader resourceLoader;


    private final String storagePath = "E:/EmailZip"; // Updated path
    @GetMapping("/byUser")
    public List<EmailHistory> getEmailHistories() {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return emailHistoryService.getEmailHistoriesByBoardSecretaryId(user.getUid());
    }
//    @GetMapping ("/email-histories")
//    public List<EmailHistory> getEmailHistories(@RequestBody EmailHistoryRequestDTO request) {
//        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        return emailHistoryService.getEmailHistories(user.getUid(), request.getFromDate(), request.getToDate());
//    }

    @GetMapping("/email-histories/{fromDate}/{toDate}")
    public List<EmailHistory> getEmailHistories(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate
          ) {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return emailHistoryService.getEmailHistories(user.getUid(), fromDate, toDate);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        return emailHistoryService.getFile(filename);
    }



}
