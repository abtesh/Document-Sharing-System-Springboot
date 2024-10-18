package com.LIB.MeesagingSystem.Controller;

import com.LIB.MeesagingSystem.Dto.ApiResponse;
import com.LIB.MeesagingSystem.Dto.EventDTO;
import com.LIB.MeesagingSystem.Dto.SecurityDtos.SendEmailToMembersDto;
import com.LIB.MeesagingSystem.Model.Event;
import com.LIB.MeesagingSystem.Service.EmailService;
import com.LIB.MeesagingSystem.Service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@AllArgsConstructor
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventDTO eventDTO) {
        Event event = eventService.saveEvent(eventDTO);
        return ResponseEntity.ok(event);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<Event> updateEvent(@PathVariable String id, @RequestBody EventDTO eventDTO) {
//        Event updatedEvent = eventService.updateEvent(id, eventDTO);
//        if (updatedEvent != null) {
//            return ResponseEntity.ok(updatedEvent);
//        }
//        return ResponseEntity.notFound().build();
//    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> cancelEvent(@PathVariable String id) {
        boolean isDeleted = eventService.cancelEvent(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable String id) {
        Optional<Event> event = eventService.getEventById(id);
        if (event.isPresent()) {
            return ResponseEntity.ok(event.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }



}
