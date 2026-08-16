package ru.practicum.explorewithme.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.AdminUpdateEventDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.service.EventService;

import java.util.List;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = ACCESS_ADMIN + URL_EVENTS)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDto>> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(name = PARAM_FROM, defaultValue = "0") int from,
            @RequestParam(name = PARAM_SIZE, defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.searchEvents(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("/{" + ID_EVENT + "}")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable(name = ID_EVENT) long eventId,
            @Valid @RequestBody AdminUpdateEventDto adminUpdateEventDto
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.updateEvent(eventId, adminUpdateEventDto));
    }
}
