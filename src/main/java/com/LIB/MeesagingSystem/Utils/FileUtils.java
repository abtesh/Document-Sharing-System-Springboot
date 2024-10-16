//package com.LIB.MeesagingSystem.Utils;
//
//
//
//import com.LIB.MeesagingSystem.Model.FilePrivilege;
//import com.LIB.MeesagingSystem.Model.Message;
//import com.LIB.MeesagingSystem.Repository.FilePrivilegeRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.multipart.MultipartFile;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@RequiredArgsConstructor
//public class FileUtils {
//    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
//    private static final FilePrivilegeRepository filePrivilegeRepository = null;
//    public static String saveAttachment(MultipartFile file, String storagePath) {
//        String uploadDir = storagePath;
//        File directory = new File(uploadDir);
//
//        // Check if the directory exists, if not, create it
//        if (!directory.exists()) {
//            directory.mkdirs();
//            logger.info("Directory created: " + directory.getAbsolutePath());
//        }
//
//        try {
//            // Generate a unique file name
//            String originalFilename = file.getOriginalFilename();
//            String uniqueFileName =  originalFilename  + "_" + UUID.randomUUID();
//
//            // Save the file
//            File dest = new File(directory, uniqueFileName);
//            file.transferTo(dest);
//            logger.info("File saved at: " + dest.getAbsolutePath());
//
//            return uniqueFileName;
//        } catch (IOException e) {
//            logger.error("Failed to save file", e);
//            throw new RuntimeException("Failed to save file", e);
//        }
//    }
//
//    public static void saveFilePrivilegesForMessage(Message message) {
//        List<FilePrivilege> filePrivileges = new ArrayList<>();
//        String messageId = message.getId();
//        for (String attachment : message.getAttachments()) {
//            FilePrivilege privilege = new FilePrivilege();
//            privilege.setMessageId(messageId);
//            privilege.setAttachmentId(attachment); // Assuming attachment is the file name
//            privilege.setUserId(message.getReceiverId());// Assign the receiver as a user with privileges
//            privilege.setGroupId(message.getGroupId());
//            privilege.setCanView(true);
//            privilege.setCanDownload(true); // Default to not allowing download
//            filePrivileges.add(privilege);
//        }
//        // Save all privileges to the repository
//        filePrivilegeRepository.saveAll(filePrivileges);
//    }
//}


package com.LIB.MeesagingSystem.Utils;

import com.LIB.MeesagingSystem.Model.FilePrivilege;
import com.LIB.MeesagingSystem.Model.Message;
//import com.LIB.MeesagingSystem.Repository.FilePrivilegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
//    private static final FilePrivilegeRepository filePrivilegeRepository = null;
        public static String saveAttachment2(MultipartFile file, String storagePath) {
        String uploadDir = storagePath;
        File directory = new File(uploadDir);

        // Check if the directory exists, if not, create it
        if (!directory.exists()) {
            directory.mkdirs();
            logger.info("Directory created: " + directory.getAbsolutePath());
        }

        try {
            // Generate a unique file name
            String originalFilename = file.getOriginalFilename();
            String uniqueFileName =  originalFilename  + "_" + UUID.randomUUID();

            // Save the file
            File dest = new File(directory, uniqueFileName);
            file.transferTo(dest);
            logger.info("File saved at: " + dest.getAbsolutePath());

            return uniqueFileName;
        } catch (IOException e) {
            logger.error("Failed to save file", e);
            throw new RuntimeException("Failed to save file", e);
        }
    }
    // Method to save attachment with custom filename
    public static void saveAttachment(MultipartFile file, String storagePath, String fileName) {
        File directory = new File(storagePath);

        // Check if the directory exists, if not, create it
        if (!directory.exists()) {
            directory.mkdirs();
            logger.info("Directory created: " + directory.getAbsolutePath());
        }

        try {
            // Save the file
            File dest = new File(directory, fileName);
            file.transferTo(dest);
            logger.info("File saved at: " + dest.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save file", e);
            throw new RuntimeException("Failed to save file", e);
        }
    }

    // Method to generate a unique filename with UUID
    public static String generateUniqueFileName(String originalFilename) {
        String baseName = getFileNameWithoutExtension(originalFilename);
        String extension = getFileExtension(originalFilename);
        String uniqueFileName = baseName + "_" + UUID.randomUUID().toString();
        if (!extension.isEmpty()) {
            uniqueFileName += "." + extension;
        }
        return uniqueFileName;
    }

    public static String getFileNameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }
    }
