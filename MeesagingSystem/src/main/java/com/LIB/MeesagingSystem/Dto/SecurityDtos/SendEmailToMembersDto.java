package com.LIB.MeesagingSystem.Dto.SecurityDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SendEmailToMembersDto {
    private List<String> memberID;
    private String subject;
    private String message;
    private MultipartFile[] files;
}
