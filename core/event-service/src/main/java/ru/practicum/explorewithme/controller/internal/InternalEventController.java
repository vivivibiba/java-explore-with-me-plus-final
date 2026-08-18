package ru.practicum.explorewithme.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.internal.EventInternalDto;
import ru.practicum.explorewithme.repository.EventRepository;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {
    private final EventRepository eventRepository;

    @GetMapping("/{eventId}")
    public EventInternalDto getEvent(@PathVariable long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(Entities.EVENT, eventId));
        return toInternalDto(event);
    }

    @PatchMapping("/{eventId}/confirmed-requests")
    public EventInternalDto adjustConfirmedRequests(@PathVariable long eventId, @RequestParam int delta) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(Entities.EVENT, eventId));
        event.setConfirmedRequests(Math.max(0, event.getConfirmedRequests() + delta));
        return toInternalDto(eventRepository.save(event));
    }

    private EventInternalDto toInternalDto(Event event) {
        return EventInternalDto.builder()
                .id(event.getId())
                .initiatorId(event.getInitiatorId())
                .confirmedRequests(event.getConfirmedRequests())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .status(event.getStatus())
                .build();
    }
}
