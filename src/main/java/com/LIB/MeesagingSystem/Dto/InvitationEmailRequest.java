package com.LIB.MeesagingSystem.Dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Data
public class InvitationEmailRequest {

    @NotEmpty(message = "Member IDs cannot be empty")
    private List<String> memberIDs;

    @NotEmpty(message = "Subject cannot be empty")
    private String subject;

    @NotEmpty(message = "Message cannot be empty")
    private String message;

    private MultipartFile[] files;

    @NotEmpty(message = "Board Secretary ID cannot be empty")
    private String boardSecretaryId;
}
