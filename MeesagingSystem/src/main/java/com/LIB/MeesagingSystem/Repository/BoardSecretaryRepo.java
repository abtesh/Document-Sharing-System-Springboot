package com.LIB.MeesagingSystem.Repository;

import com.LIB.MeesagingSystem.Model.BODMembers;
import com.LIB.MeesagingSystem.Model.BoardSecretary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardSecretaryRepo extends MongoRepository<BoardSecretary, String> {
    Optional<BoardSecretary> findByEmail(String email);

    @Query("{ 'email': ?0, 'isActive': ?1 }")
    Optional<BoardSecretary> findByEmailAndIsActive(String email, boolean isActive);

    @Query("{ '_id': ?0, 'isActive': ?1 }")
    Optional<BoardSecretary> findByIdAndIsActive(String id, boolean isActive);
    
    Optional<BoardSecretary> findByFirstNameAndMiddleNameAndLastName(String firstName, String middleName, String lastName);
    Optional<BoardSecretary> findIDByEmail(String from);
}
