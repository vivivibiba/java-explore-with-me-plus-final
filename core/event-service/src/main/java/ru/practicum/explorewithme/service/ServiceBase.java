package ru.practicum.explorewithme.service;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.AnalyzerClient;
import ru.practicum.explorewithme.RecommendedEvent;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceBase {
    protected <E> E findEntityIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(entity, id));
    }

    protected <E> void checkEntityExistsIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(entity, id);
        }
    }

    protected List<EventDto> getEventsWithRatings(List<Event> events, AnalyzerClient analyzerClient) {
        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> ratings = getRatings(analyzerClient, events);
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
            dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
            return dto;
        }).toList();
    }

    private Map<Long, Double> getRatings(AnalyzerClient analyzerClient, List<Event> events) {
        return analyzerClient.getInteractionsCount(events.stream().map(Event::getId).toList())
                .collect(Collectors.toMap(
                        RecommendedEvent::eventId,
                        RecommendedEvent::score,
                        (left, right) -> right
                ));
    }
}
