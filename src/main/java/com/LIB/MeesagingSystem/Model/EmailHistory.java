package com.LIB.MeesagingSystem.Model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

import java.util.Date;
import java.util.List;
/**
 * Represents the history of emails sent within the system.
 * <p>
 * The {@link EmailHistory} class is used to model the details of each email sent, including information
 * about the sender, recipient, subject, and date of sending. It also stores the file path if there is an
 * attachment associated with the email.
 * </p>

 * @author Elizabeth Hagos
 */
@Data
@Document(collection = "EmailHistory")
public class EmailHistory {
    @Id
    private String id;
    private String boardSecretaryId;
    private List<String> recipient;
    private String recipientGroupId;
    private String subject;
    private Date sentDate;
    private String message;
    private List<String> attachmentName;


}