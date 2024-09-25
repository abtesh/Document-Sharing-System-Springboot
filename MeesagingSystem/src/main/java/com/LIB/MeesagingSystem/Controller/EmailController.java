package com.LIB.MeesagingSystem.Controller;


import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Dto.InvitationDto;
import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Dto.SecurityDtos.SendEmailToMembersDto;
import com.LIB.MeesagingSystem.Model.BoardSecretary;
import com.LIB.MeesagingSystem.Repository.BoardSecretaryRepo;
import com.LIB.MeesagingSystem.Service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@Slf4j
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final BoardSecretaryRepo boardSecretaryRepo;


    @PostMapping("/sendToMembers")
    public ResponseEntity<ApiResponse> sendEmailToMembers(@ModelAttribute SendEmailToMembersDto sendEmailToMembersDto) {
        try {
            LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<BoardSecretary> boardSecretary = boardSecretaryRepo.findByEmail(user.getEmail());
            if (boardSecretary.isPresent()) {
                String boardSecretaryId = boardSecretary.get().getId();
                ApiResponse response = emailService.sendToMember(sendEmailToMembersDto.getMemberID(), sendEmailToMembersDto.getSubject(), sendEmailToMembersDto.getMessage(), sendEmailToMembersDto.getFiles(), boardSecretaryId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse("Error", "Access Restricted"));
            }
        } catch (IOException ioException) {
            log.info("hello{}",ioException.getMessage());
            ioException.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/sendToGroup")
    public ResponseEntity<ApiResponse> sendEmailToGroup(
            //    @RequestParam("from") String fromEmail,
            @RequestParam("groupID") String groupID,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<BoardSecretary> boardSecretary = boardSecretaryRepo.findByEmail(user.getEmail());
        if (boardSecretary.isPresent()) {
            List<String> boardSecretaryAssignedGroup = boardSecretary.get().getGroupID();
            if (boardSecretaryAssignedGroup.contains(groupID)) {
                String boardSecretaryId = boardSecretary.get().getId();
                ApiResponse response = emailService.sendToGroup(groupID, subject, message, files, boardSecretaryId);
                if ("Success".equals(response.getStatus())) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("Error", "Bad Request"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse("Error", "Board Secretary cannot send email to this group: Access Restricted"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Error", "Access Restricted"));
        }

    }


}