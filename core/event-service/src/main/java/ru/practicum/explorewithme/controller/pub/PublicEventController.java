package ru.practicum.explorewithme.controller.pub;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.PublicEventSort;
import ru.practicum.explorewithme.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = URL_EVENTS)
@RequiredArgsConstructor
@Validated
@SuppressWarnings("unused")
public class PublicEventController {

    private static final String USER_ID_HEADER = "X-EWM-USER-ID";
    private static final int RECOMMENDATIONS_LIMIT = 10;
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(
            @RequestParam(name = PARAM_TEXT, required = false) String text,
            @RequestParam(name = PARAM_CATEGORIES, required = false) List<Long> categories,
            @RequestParam(name = PARAM_PAID, required = false) Boolean paid,
            @RequestParam(name = PARAM_RANGE_START, required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(name = PARAM_RANGE_END, required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(name = PARAM_ONLY_AVAILABLE, required = false, defaultValue = "false") boolean onlyAvailable,
            @RequestParam(name = PARAM_SORT, required = false/*ТУТЬ(см ниже)*/) PublicEventSort sort,
            @RequestParam(name = PARAM_FROM, required = false, defaultValue = "0")
            @PositiveOrZero int from,
            @RequestParam(name = PARAM_SIZE, required = false, defaultValue = "10")
            @Positive int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getPublishedEvents(
                        text,
                        categories,
                        paid,
                        rangeStart,
                        rangeEnd,
                        onlyAvailable,
                        /// Вроде по дефолту можно строку прописать
                        sort == null ? PublicEventSort.EVENT_DATE : sort,
                        from,
                        size
                ));
    }

    @GetMapping("/{" + ID_EVENT + "}")
    public ResponseEntity<EventDto> getEvent(@PathVariable(name = ID_EVENT) long eventId,
                                             @RequestHeader(USER_ID_HEADER) long userId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getPublishedEvent(eventId, userId));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<EventShortDto>> getRecommendations(
            @RequestHeader(USER_ID_HEADER) long userId) {
        return ResponseEntity.ok(eventService.getRecommendations(userId, RECOMMENDATIONS_LIMIT));
    }

    @PutMapping("/{" + ID_EVENT + "}/like")
    public ResponseEntity<Void> likeEvent(
            @PathVariable(name = ID_EVENT) long eventId,
            @RequestHeader(USER_ID_HEADER) long userId) {
        eventService.likeEvent(eventId, userId);
        return ResponseEntity.noContent().build();
    }
}
