package com.LIB.MeesagingSystem.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */


@Document(collection = "file_privileges")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilePrivilege {

    @Id
    private String id;
    private String messageId;
    private String attachmentId;
    private String userId;
    private String groupId;
    private boolean canView;
    private boolean canDownload;
}
