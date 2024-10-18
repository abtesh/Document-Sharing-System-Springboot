package com.LIB.MeesagingSystem.Repository;

import com.LIB.MeesagingSystem.Model.Group;
import com.LIB.MeesagingSystem.Model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 *  @author Abenezer Teshome  - Date 17/aug/2024
 */

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findBySenderIdAndReceiverId(String senderId, String receiverId);
    List<Message> findBySenderId(String senderId);
    List<Message> findByReceiverId(String receiverId);
    List<Message> findByGroupId(String groupId);
    Message deleteMessageById(String id);
    Optional<Message> findByAttachmentsContaining(String fileName);
    Message findByGroupIdAndAttachmentsContaining(String groupId, String fileName);
    @Query("{ 'senderId': ?0, 'receiverId': { $exists: true, $ne: null } }")
    List<Message> findBySenderIdAndReceiverIdIsNotNull(String senderId);
    long countByReceiverIdAndIsReadFalse(String receiverId);
    List<Message> findByReceiverIdAndIsReadFalse(String receiverId);
    List<Message> findByGroupIdAndIsReadFalse(String groupId);
    long countByGroupIdAndIsReadFalse(String groupId);

}

