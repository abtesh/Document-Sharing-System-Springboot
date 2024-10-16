//package com.LIB.MeesagingSystem.Repository;
//
//import com.LIB.MeesagingSystem.Model.BODMembers;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//@Repository
//public interface BODMembersRepo extends MongoRepository<BODMembers, String> {
//    Optional<BODMembers> findByEmail(String email);
//    List<BODMembers> findByIdIn(List<String> ids);
//    Optional<BODMembers> findByFirstNameAndMiddleNameAndLastName(String firstName, String middleName, String lastName);
//    Optional<BODMembers> findBODMembersById ( String id);
//    List<BODMembers> findAllById(List<BODMembers> members);
//
//
//}






package com.LIB.MeesagingSystem.Repository;

import com.LIB.MeesagingSystem.Model.BODMembers;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BODMembersRepo extends MongoRepository<BODMembers, String> {
    Optional<BODMembers> findByEmail(String email);
    List<BODMembers> findByIdIn(List<String> ids);
    Optional<BODMembers> findByFirstNameAndMiddleNameAndLastName(String firstName, String middleName, String lastName);
    Optional<BODMembers> findBODMembersById(String id);
    List<BODMembers> findAllById(List<BODMembers> members);
}
