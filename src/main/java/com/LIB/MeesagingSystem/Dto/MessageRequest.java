package com.LIB.MeesagingSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {
  //  private String senderEmail;
    private String receiverEmail;
    private String content;
    private List<MultipartFile> attachments;
}
