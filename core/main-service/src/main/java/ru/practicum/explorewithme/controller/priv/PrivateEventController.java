package ru.practicum.explorewithme.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UserUpdateEventDto;
import ru.practicum.explorewithme.dto.request.ChangedRequestStatusesDto;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;
import ru.practicum.explorewithme.service.EventService;

import java.util.List;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = ACCESS_PRIVATE + "/{" + ID_USER + "}" + URL_EVENTS)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(
            @PathVariable(name = ID_USER) long userId,
            @RequestParam(name = PARAM_FROM, defaultValue = "0") int from,
            @RequestParam(name = PARAM_SIZE, defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getEvents(userId, from, size));
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(
            @PathVariable(name = ID_USER) long userId,
            @Valid @RequestBody NewEventDto newEventDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(userId, newEventDto));
    }

    @GetMapping("/{" + ID_EVENT + "}")
    public ResponseEntity<EventDto> getEvent(
            @PathVariable(name = ID_USER) long userId,
            @PathVariable(name = ID_EVENT) long eventId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getEvent(userId, eventId));
    }

    @PatchMapping("/{" + ID_EVENT + "}")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable(name = ID_USER) long userId,
            @PathVariable(name = ID_EVENT) long eventId,
            @Valid @RequestBody UserUpdateEventDto userUpdateEventDto
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.updateEvent(userId, eventId, userUpdateEventDto));
    }

    @GetMapping("/{" + ID_EVENT + "}" + URL_REQUESTS)
    public ResponseEntity<List<RequestDto>> getRequests(
            @PathVariable(name = ID_USER) long userId,
            @PathVariable(name = ID_EVENT) long eventId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getRequests(userId, eventId));
    }

    @PatchMapping("/{" + ID_EVENT + "}" + URL_REQUESTS)
    public ResponseEntity<ChangedRequestStatusesDto> updateRequestStatuses(
            @PathVariable(name = ID_USER) long userId,
            @PathVariable(name = ID_EVENT) long eventId,
            @Valid @RequestBody UpdateRequestStatusDto update
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.updateRequestStatuses(userId, eventId, update));
    }
}
