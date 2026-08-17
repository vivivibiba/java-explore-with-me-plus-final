package ru.practicum.explorewithme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.Location;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServiceBase {
    protected <E> E findEntityIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(entity, id));
    }

    protected <E> void checkEntityExistsIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(entity, id);
        }
    }

    protected List<EventDto> getEventsWithStats(List<Event> events, StatsClient statsClient) {
        Map<String, Long> viewsByUri = getStats(statsClient, events);
        return events.stream().map(event -> {
            CategoryDto category = CategoryMapper.toCategoryDto(event.getCategory());
            UserShortDto initiator = UserShortDto.builder()
                    .id(event.getInitiatorId())
                    .name(event.getInitiatorName())
                    .build();
            Location location = LocationMapper.toLocation(event.getLocation());
            EventDto dto = EventMapper.toEventDto(event);
            dto.setCategory(category);
            dto.setInitiator(initiator);
            dto.setLocation(location);
            dto.setViews(viewsByUri.getOrDefault("/events/" + event.getId(), 0L));
            return dto;
        }).toList();
    }

    private Map<String, Long> getStats(StatsClient statsClient, List<Event> events) {
        Map<String, Long> result = new HashMap<>();
        if (events.isEmpty()) {
            return result;
        }
        try {
            LocalDateTime start = events.stream()
                    .map(Event::getCreatedOn)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            List<String> uris = events.stream().map(event -> "/events/" + event.getId()).toList();
            List<ViewStatsResponse> stats = statsClient.getStatistics(start, LocalDateTime.now(), uris, true);
            stats.forEach(stat -> result.put(stat.getUri(), stat.getHits()));
        } catch (RuntimeException exception) {
            log.warn("Stats service is unavailable, using zero views: {}", exception.getMessage());
        }
        return result;
    }
}
