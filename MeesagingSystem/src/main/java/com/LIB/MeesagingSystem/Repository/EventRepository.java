package com.LIB.MeesagingSystem.Repository;

import com.LIB.MeesagingSystem.Model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
}
