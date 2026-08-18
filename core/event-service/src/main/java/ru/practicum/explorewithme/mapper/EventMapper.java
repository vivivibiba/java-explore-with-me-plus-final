package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.entity.Event;

public class EventMapper {
    public static Event toEvent(NewEventDto newEventDto) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventDto toEventDto(Event event) {

        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.isRequestModeration())
                .status(event.getStatus())
                .title(event.getTitle())
                .build();
    }

    public static EventShortDto toEventShortDto(EventDto eventDto) {
        return EventShortDto.builder()
                .id(eventDto.getId())
                .annotation(eventDto.getAnnotation())
                .category(eventDto.getCategory())
                .confirmedRequests(eventDto.getConfirmedRequests())
                .eventDate(eventDto.getEventDate())
                .initiator(eventDto.getInitiator())
                .paid(eventDto.isPaid())
                .title(eventDto.getTitle())
                .rating(eventDto.getRating())
                .build();
    }
}
