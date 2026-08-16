package ru.practicum.explorewithme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.Location;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.explorewithme.controller.ControllerConstants.URL_EVENTS;

@Slf4j
public class ServiceBase {
    protected <E> E findEntityIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(entity, id));
    }

    protected <E> void checkEntityExistsIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(entity, id);
        }
    }

    protected Map<String, Long> getStats(StatsClient statsClient, List<Event> events) {
        LocalDateTime start = getStartPoint(events);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = getUris(events);
        List<ViewStatsResponse> stats = statsClient.getStatistics(start, end, uris, true);
        Map<String, Long> viewsByUri = new HashMap<>();
        stats.forEach(stat -> viewsByUri.put(stat.getUri(), stat.getHits()));
        return viewsByUri;
    }

    protected List<EventDto> getEventsWithStats(List<Event> events, StatsClient statsClient) {
        Map<String, Long> viewsByUri = getStats(statsClient, events);
        List<EventDto> mappedEvents = events.stream()
                .map(event -> {
                    CategoryDto categoryDto = CategoryMapper.toCategoryDto(event.getCategory());
                    UserShortDto initiator = UserMapper.toUserShortDto(event.getInitiator());
                    Location location = LocationMapper.toLocation(event.getLocation());
                    EventDto eventDto = EventMapper.toEventDto(event);
                    setNestedClassesValues(eventDto, categoryDto, initiator, location);
                    return eventDto;
                })
                .toList();

        mappedEvents.forEach(eventDto -> {
            long views = getViews(eventDto, viewsByUri);
            eventDto.setViews(views);
        });
        log.debug("К событиям {} добавлено количество просмотров", mappedEvents);
        return mappedEvents;
    }

    protected long getViews(EventDto event, Map<String, Long> viewsByUri) {
        return viewsByUri.getOrDefault(URL_EVENTS + "/" + event.getId(), 0L);
    }

    protected void setNestedClassesValues(Event event,
                                          Category category,
                                          User initiator,
                                          LocationEmbeddable locationEmbeddable) {
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setLocation(locationEmbeddable);
    }

    protected void setNestedClassesValues(EventDto event,
                                          CategoryDto category,
                                          UserShortDto initiator,
                                          Location location) {
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setLocation(location);
    }

    private LocalDateTime getStartPoint(List<Event> events) {
        return events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No events were found"));
    }

    private List<String> getUris(List<Event> events) {
        return events.stream()
                .map(event -> URL_EVENTS + "/" + event.getId())
                .toList();
    }
}
