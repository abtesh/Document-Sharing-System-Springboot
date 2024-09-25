package com.LIB.MeesagingSystem.Service;

import com.LIB.MeesagingSystem.Model.EmailHistory;
import com.LIB.MeesagingSystem.Repository.EmailHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing email history operations.
 * <p>
 * The {@link EmailHistoryService} class provides business logic and operations related to email history.
 * It interacts with the {@link EmailHistoryRepo} repository to perform CRUD operations on the email history records.
 * </p>
 *
 * @author Elizabeth Hagos
 */

@Service
public class EmailHistoryService {

    @Autowired
    private EmailHistoryRepo emailHistoryRepository;
    private ResourceLoader resourceLoader;
    private final String BASE_PATH = "E:/EmailZip/";
    public List<EmailHistory> getEmailHistoriesByBoardSecretaryId(String boardSecretaryId) {
        return emailHistoryRepository.findByBoardSecretaryId(boardSecretaryId);
    }



    public List<EmailHistory> getEmailHistories(String boardSecretaryId, Date fromDate, Date toDate) {
        // Set end time to 23:59:59 for the toDate
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calendar.getTime();

        return emailHistoryRepository.findByBoardSecretaryIdAndSentDateBetween(boardSecretaryId, fromDate, endOfDay);
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = Paths.get("E:/EmailZip" + filename).toAbsolutePath().normalize();
            return resourceLoader.getResource("file:" + filePath.toString());
        } catch (Exception e) {
            throw new RuntimeException("File not found " + filename, e);
        }
    }

//    public List<EmailHistory> getEmailHistories(String boardSecretaryId, Date fromDate, Date toDate) {
//
//        return emailHistoryRepository.findByBoardSecretaryIdAndSentDateBetween(boardSecretaryId, fromDate, toDate);
//    }


    public ResponseEntity<Resource> getFile(String filename) {
        try {
            File file = new File(BASE_PATH + filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            URI fileUri = file.toURI();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new UrlResource(fileUri));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
