package com.LIB.MeesagingSystem.Service;
import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Dto.SecurityDtos.LdapUserDTO;
import com.LIB.MeesagingSystem.Model.BODGroup;
import com.LIB.MeesagingSystem.Model.BODMembers;
import com.LIB.MeesagingSystem.Model.EmailHistory;
import com.LIB.MeesagingSystem.Repository.BODGroupRepo;
import com.LIB.MeesagingSystem.Repository.BODMembersRepo;
import com.LIB.MeesagingSystem.Repository.EmailHistoryRepo;
import com.LIB.MeesagingSystem.Utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class EmailService {

    @Value("${file.storage-path}")
    private String storagePath;

    @Value("${lib-credentials.username}")
    private String LibUsername;

    @Value("${lib-credentials.password}")
    private String LibPassword;

    @Value("${lib-url.email}")
    private String emailUrl;

    @Value("${lib-url.tele-sms}")
    private String teleSmsUrl;

    @Value("${lib-url.safari-sms}")
    private String safariSmsUrl;

    private final RestTemplate restTemplate;
    private final BODMembersRepo bodMembersRepo;
    private final BODGroupRepo bodGroupRepo;
    private final EmailHistoryRepo emailHistoryRepo;

    public EmailService(RestTemplate restTemplate, BODMembersRepo bodMembersRepo, BODGroupRepo bodGroupRepo, EmailHistoryRepo emailHistoryRepo) {
        this.restTemplate = restTemplate;
        this.bodMembersRepo = bodMembersRepo;
        this.bodGroupRepo = bodGroupRepo;
        this.emailHistoryRepo = emailHistoryRepo;
    }

    public ApiResponse sendToMember(List<String> memberID, String subject, String message, MultipartFile[] files, String boardSecretaryid) throws IOException {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<BODMembers> members = bodMembersRepo.findByIdIn(memberID);
        List<String> emails = members.stream()
                .map(BODMembers::getEmail)
                .collect(Collectors.toList());

        List<String> mobile = members.stream()
                .map(BODMembers::getMobile)
                .collect(Collectors.toList());


        if (emails.isEmpty()) {
            return new ApiResponse("Error", "Member not found");
        }

        String from = "Lion International Bank S.C <" + user.getEmail() + ">";
        boolean isInvitation = false;

        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (file.getOriginalFilename().endsWith(".ics")) {
                    isInvitation = true;
                    break;
                }
            }

            handleFiles(from, emails, subject, message, files, null, boardSecretaryid);

            if (isInvitation) {
                sendIndividualInvitationSMS(members);
            }
        } else {
            for (String email : emails) {
                sendEmail(from, email, subject, message, null);
            }
            saveEmailHistory(emails, null, subject, message, boardSecretaryid, null);
            sendEmailNotificationSMStoIndividual(members);

        }
        return new ApiResponse("Success", "Email Sent Successfully");
    }

    public ApiResponse sendToGroup(String groupId, String subject, String message, MultipartFile[] files, String boardSecretaryid) throws IOException {
        LdapUserDTO user = (LdapUserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        BODGroup group = bodGroupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        List<String> memberIds = new ArrayList<>(group.getMembers());
        List<String> emails = bodMembersRepo.findByIdIn(memberIds).stream()
                .map(BODMembers::getEmail)
                .collect(Collectors.toList());
        List<String> mobileNumber = bodMembersRepo.findByIdIn(memberIds).stream()
                .map(BODMembers::getMobile)
                .collect(Collectors.toList());
        if (emails.isEmpty()) {
            return new ApiResponse("Error", "Group not found");
        }
        String from = "Lion International Bank S.C <" + user.getEmail() + ">";

        if (files != null && files.length > 0) {
            handleFiles(from, emails, subject, message, files, groupId, boardSecretaryid);
        } else {
            for (String email : emails) {
                sendEmail(from, email, subject, message, null);
            }
            saveEmailHistory(null, groupId, subject, message, boardSecretaryid, null);
        }

sendEmailNotificationSMStoGroup(mobileNumber, group.getGroupName());
        return new ApiResponse("Success", "Email Sent Successfully");
    }

    private void sendIndividualInvitationSMS(List<BODMembers> members) {
        for (BODMembers member : members) {
            String mobile = member.getMobile();
            String message = "Dear "+ member.getFirstName() +" " + member.getLastName()+"\n \nYou have received a new invitation email. Please check your Email for detail information. \n \n LIB - Key to success";

            if (mobile.startsWith("09")) {
                sendSMS(mobile, message, teleSmsUrl);
            } else if (mobile.startsWith("07")) {
                sendSMS(mobile, message, safariSmsUrl);
            }
        }
    }

    private void sendEmailNotificationSMStoGroup(List<String> members, String groupName) {
        for (String member : members) {
            String mobile = member;
            String message = "Dear " + groupName + " Member, \n \n You have received a new email. Please check your Email for detail information. \n \n LIB - Key to success";

            if (mobile.startsWith("09")) {
                sendSMS(mobile, message, teleSmsUrl);
            } else if (mobile.startsWith("07")) {
                sendSMS(mobile, message, safariSmsUrl);
            }
        }
    }

    private void sendEmailNotificationSMStoIndividual(List<BODMembers> members) {
        for (BODMembers member : members) {
            String mobile = member.getMobile(); // Access the mobile number
            String message = "Dear " + member.getFirstName() + " " + member.getMiddleName() + ", \n \nYou have received a new email. Please check your Email for detailed information. \n \n LIB - Key to success";

            if (mobile.startsWith("09")) {
                sendSMS(mobile, message, teleSmsUrl);
            } else if (mobile.startsWith("07")) {
                sendSMS(mobile, message, safariSmsUrl);
            }
        }
    }

    private void sendSMS(String mobile, String message, String emailUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(LibUsername, LibPassword);

        Map<String, String> body = new HashMap<>();
        body.put("mobile", mobile);
        body.put("message", message);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(emailUrl, requestEntity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("SMS not sent, status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    private void handleFiles(String from, List<String> emails, String subject, String message, MultipartFile[] files, String groupId, String boardSecretaryid) throws IOException {
        if (files.length > 1) {
            String zipFileName = compressAttachments(Arrays.asList(files));
            Path zipFilePath = Paths.get(storagePath, zipFileName);

            for (String email : emails) {
                sendEmail(from, email, subject, message, zipFilePath);
            }
            saveEmailHistory(emails, groupId, subject, message, boardSecretaryid, Collections.singletonList(zipFileName));
        } else {
            List<String> attachmentNames = new ArrayList<>();
            for (MultipartFile file : files) {
                String originalFileName = file.getOriginalFilename();
                String uniqueFileName = FileUtils.generateUniqueFileName(originalFileName);
                FileUtils.saveAttachment(file, storagePath, uniqueFileName);
                attachmentNames.add(uniqueFileName);

                for (String email : emails) {
                    sendEmail(from, email, subject, message, Paths.get(storagePath, uniqueFileName));
                }
            }
            saveEmailHistory(emails, groupId, subject, message, boardSecretaryid, attachmentNames);
        }
    }

    private void sendEmail(String from, String to, String subject, String message, Path filePath) throws IOException {
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("from", from);
        bodyMap.add("to", to);
        bodyMap.add("subject", subject);
        bodyMap.add("message", message);

        if (filePath != null) {
            byte[] fileBytes = Files.readAllBytes(filePath);
            bodyMap.add("files", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return filePath.getFileName().toString();
                }
            });
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth(LibUsername, LibPassword);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
        ResponseEntity<String> response = restTemplate.exchange(emailUrl, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Email not sent, status code: " + response.getStatusCode());
        }
    }

    private void saveEmailHistory(List<String> recipients, String recipientGroupId, String subject, String message, String boardSecretaryid, List<String> attachmentNames) {
        EmailHistory emailHistory = new EmailHistory();
        emailHistory.setBoardSecretaryId(boardSecretaryid);
        emailHistory.setRecipient(recipients);
        emailHistory.setRecipientGroupId(recipientGroupId);
        emailHistory.setSubject(subject);
        emailHistory.setMessage(message);
        emailHistory.setSentDate(new Date());

        if (attachmentNames != null && !attachmentNames.isEmpty()) {
            emailHistory.setAttachmentName(attachmentNames);
        }
        emailHistoryRepo.save(emailHistory);
    }

    private String compressAttachments(List<MultipartFile> attachments) {
        String baseFileName = attachments.stream()
                .map(file -> FileUtils.getFileNameWithoutExtension(file.getOriginalFilename()))
                .collect(Collectors.joining("_"));
        String zipFileName = baseFileName + "_" + UUID.randomUUID() + ".zip";
        Path zipFilePath = Paths.get(storagePath, zipFileName);
        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (MultipartFile attachment : attachments) {
                String fileName = FileUtils.generateUniqueFileName(attachment.getOriginalFilename());
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                zos.write(attachment.getBytes());
                zos.closeEntry();
            }
            zos.finish();

            Files.write(zipFilePath, baos.toByteArray());

            return zipFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress files", e);
        }
    }
}








































