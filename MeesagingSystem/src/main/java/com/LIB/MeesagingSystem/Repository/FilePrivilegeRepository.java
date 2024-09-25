package com.LIB.MeesagingSystem.Repository;


import com.LIB.MeesagingSystem.Model.FilePrivilege;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */

public interface FilePrivilegeRepository extends MongoRepository <FilePrivilege, String> {
  Optional<FilePrivilege> findByMessageIdAndAttachmentIdAndUserId(String messageId, String attachmentId, String userId);
  void deleteByMessageId(String messageId);
  Optional<FilePrivilege> findByAttachmentIdAndUserId(String attachmentId, String userId);
  Optional<FilePrivilege> findByAttachmentIdAndGroupId(String attachmentId, String groupId);
}

