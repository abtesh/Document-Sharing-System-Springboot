package com.LIB.MeesagingSystem.Repository;
import com.LIB.MeesagingSystem.Model.BODGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BODGroupRepo extends MongoRepository<BODGroup , String>{

    Optional<BODGroup> findByGroupName(String groupName);



}
