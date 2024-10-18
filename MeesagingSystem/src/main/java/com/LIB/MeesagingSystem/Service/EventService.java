package com.LIB.MeesagingSystem.Service;

import com.LIB.MeesagingSystem.Dto.EventDTO;
import com.LIB.MeesagingSystem.Model.Event;
import com.LIB.MeesagingSystem.Repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
@AllArgsConstructor
@Service
public class EventService {

    private EventRepository eventRepository;

    public Event saveEvent(EventDTO eventDTO) {
        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setLocation(eventDTO.getLocation());
        event.setMeetingLink(eventDTO.getMeetingLink());
        event.setParticipants(eventDTO.getParticipants());
        event.setOrganizerName(eventDTO.getOrganizerName());
        event.setOrganizerEmail(eventDTO.getOrganizerEmail());
        event.setStart(eventDTO.getStart());
        event.setEnd(eventDTO.getEnd());
        event.setCreatedDate(new Date());
        event.setUpdatedDate(new Date());
        return eventRepository.save(event);
    }

    public boolean cancelEvent(String id) {
        if (eventRepository.existsById(id)) {
            Event event = eventRepository.findById(id).orElse(null);
            if (event != null) {
                event.setIsCanceled(true);
                eventRepository.save(event);
                return true;
            }
        }
        return false;
    }


    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
