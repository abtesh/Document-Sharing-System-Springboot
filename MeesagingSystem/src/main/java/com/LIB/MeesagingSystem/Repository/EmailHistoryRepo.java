package com.LIB.MeesagingSystem.Repository;


import com.LIB.MeesagingSystem.Model.EmailHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailHistoryRepo extends MongoRepository<EmailHistory,String> {


   // List<EmailHistory> findByUserIdAndSentDateBetween(String userId, LocalDate fromDate, LocalDate toDate);

    List<EmailHistory> findByBoardSecretaryId(String BoardSecretaryId);

    @Query("{ 'boardSecretaryId': ?0, 'sentDate': { $gte: ?1, $lte: ?2 } }")
    List<EmailHistory> findByBoardSecretaryIdAndSentDateBetween(String boardSecretaryId, Date fromDate, Date toDate);

}




