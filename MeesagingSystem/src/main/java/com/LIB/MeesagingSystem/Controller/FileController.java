package com.LIB.MeesagingSystem.Controller;



import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Model.FilePrivilege;
import com.LIB.MeesagingSystem.Model.Message;
import com.LIB.MeesagingSystem.Repository.FilePrivilegeRepository;
import com.LIB.MeesagingSystem.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;


/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */

@RestController
@RequestMapping("/downloadFiles")
@RequiredArgsConstructor
public class FileController {
    @Value("${file.storage-path}")
    private String storagePath;
    private final MessageRepository messageRepository;
    private final FilePrivilegeRepository filePrivilegeRepository;

    @GetMapping("/viewAttachment")
    public ResponseEntity<?> viewAttachment(@RequestParam String fileName) throws IOException {
        Optional<Message> message = messageRepository.findByAttachmentsContaining(fileName);
        if (message.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found for the given attachment");
        }
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUid();
        FilePrivilege privilege = filePrivilegeRepository.findByAttachmentIdAndUserId(fileName, userId)
                .orElseThrow(() -> new RuntimeException("Access Denied"));

        if (!privilege.isCanView() && !privilege.isCanDownload()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to view or download this file");
        }
                if (privilege.isCanView() && !privilege.isCanDownload() && !fileName.endsWith(".pdf")) {
            ResponseEntity<?> pdfConversionResponse = convertToPdf(fileName);
            if (pdfConversionResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(pdfConversionResponse.getStatusCode()).build();
            }
            fileName = fileName.replace(".", "_") + ".pdf"; // Update fileName to the converted PDF
        }

        Path path = Paths.get(storagePath).resolve(fileName);
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() || resource.isReadable()) {
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String contentDisposition = privilege.isCanDownload()
                    ? "attachment"
                    : "inline" ;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .header("Access-Control-Allow-Headers", "Content-Disposition")
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or not readable");
        }
    }
    private ResponseEntity<?> convertToPdf(String fileName) throws IOException {
        // Locate the file
        Path filePath = Paths.get(storagePath).resolve(fileName);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or not readable");
        }
        // Send the file to Gotenberg API for conversion
        String gotenbergUrl = "http://localhost:3000/forms/libreoffice/convert";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", new FileSystemResource(filePath.toFile()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(gotenbergUrl, HttpMethod.POST, requestEntity, byte[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Save the converted PDF file
            Path pdfPath = Paths.get(storagePath).resolve(fileName.replace(".", "_") + ".pdf");
            Files.write(pdfPath, Objects.requireNonNull(response.getBody()));
            return ResponseEntity.ok().body("File converted to PDF successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to convert file to PDF");
        }
    }

    @PostMapping("/setFilePrivilege")
    public ResponseEntity<?> setFilePrivilege(@RequestParam String attachmentId,
                                              @RequestParam String userId,
                                              @RequestParam boolean canView,
                                              @RequestParam boolean canDownload) {
        // Debugging logs
        System.out.println("Received Request: attachmentId=" + attachmentId + ", userId=" + userId);
        System.out.println("Privileges: canView=" + canView + ", canDownload=" + canDownload);


        Optional<FilePrivilege> optionalPrivilege = filePrivilegeRepository.findByAttachmentIdAndUserId(attachmentId, userId);
        FilePrivilege privilege;
        if (optionalPrivilege.isPresent()) {
            privilege = optionalPrivilege.get();
            privilege.setCanDownload(canDownload);
            privilege.setUserId(userId);
            privilege.setCanView(canView);
            System.out.println("Privilege found: Updating existing privilege");
        } else {
            privilege = new FilePrivilege();
            privilege.setAttachmentId(attachmentId);
            privilege.setUserId(userId);
            privilege.setCanDownload(canDownload);
            privilege.setCanView(canView);
            System.out.println("Privilege not found: Creating new privilege");
        }
        // Save privilege and log the operation
        filePrivilegeRepository.save(privilege);
        System.out.println("Privileges updated successfully");

        return ResponseEntity.ok("Privileges updated successfully");
    }

    @PostMapping("/group/setFilePrivilege")
    public ResponseEntity<?> setGroupFilePrivilege(@RequestParam String groupId,
                                                   @RequestParam String attachmentId,
                                                   @RequestParam boolean canView,
                                                   @RequestParam boolean canDownload) {
        Optional<FilePrivilege> optionalPrivilege = filePrivilegeRepository.findByAttachmentIdAndGroupId(attachmentId, groupId);

        FilePrivilege privilege;
        if (optionalPrivilege.isPresent()) {
            privilege = optionalPrivilege.get();
            privilege.setCanView(canView);
            privilege.setCanDownload(canDownload);
        } else {
            privilege = new FilePrivilege();
            privilege.setAttachmentId(attachmentId);
            privilege.setGroupId(groupId);
            privilege.setCanView(canView);
            privilege.setCanDownload(canDownload);
        }

        filePrivilegeRepository.save(privilege);
        return ResponseEntity.ok("Group file privileges updated successfully");
    }

    @GetMapping("/group/viewAttachment")
    public ResponseEntity<?> viewGroupAttachment(@RequestParam String fileName, @RequestParam String groupId) throws IOException {
        Message message = messageRepository.findByGroupIdAndAttachmentsContaining(groupId, fileName);
        if (message == null) {
            throw new RuntimeException("Message not found for the given attachment");
        }
        FilePrivilege privilege = filePrivilegeRepository.findByAttachmentIdAndGroupId(fileName, groupId)
                .orElseThrow(() -> new RuntimeException("Access Denied"));

        if (!privilege.isCanView() && !privilege.isCanDownload()) {
            throw new RuntimeException("You don't have permission to view or download this file");
        }
        if (privilege.isCanView() && !privilege.isCanDownload() && !fileName.endsWith(".pdf")) {
            ResponseEntity<?> pdfConversionResponse = convertToPdf(fileName);
            if (pdfConversionResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(pdfConversionResponse.getStatusCode()).build();
            }
            fileName = fileName.replace(".", "_") + ".pdf"; // Update fileName to the converted PDF
        }

        Path path = Paths.get(storagePath).resolve(fileName);
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() || resource.isReadable()) {
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String contentDisposition = privilege.isCanDownload()
                    ? "attachment"
                    : "inline" ;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .header("Access-Control-Allow-Headers", "Content-Disposition")
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or not readable");
        }
    }

    // Debugging logs
//            System.out.println("File: " + fileName);
//            System.out.println("Privilege: canView=" + privilege.isCanView() + ", canDownload=" + privilege.isCanDownload());
//            System.out.println("Content-Disposition: " + contentDisposition);
}