package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.request.ChangedRequestStatusesDto;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;
import ru.practicum.explorewithme.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventDto> getEvents(long userId, int from, int size);

    EventDto createEvent(long userId, NewEventDto newEventDto);

    EventDto getEvent(long userId, long eventId);

    EventDto updateEvent(long userId, long eventId, UserUpdateEventDto userUpdateEventDto);

    List<RequestDto> getRequests(long userId, long eventId);

    ChangedRequestStatusesDto updateRequestStatuses(long userId, long eventId, UpdateRequestStatusDto update);

    Event getEventById(long eventId, long userId);

    List<EventShortDto> getPublishedEvents(String text,
                                           List<Long> categories,
                                           Boolean paid,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           boolean onlyAvailable,
                                           PublicEventSort sort,
                                           int from,
                                           int size,
                                           String ip,
                                           String uri);

    EventDto getPublishedEvent(long eventId, String ip, String uri);

    List<EventDto> searchEvents(List<Long> users,
                                List<String> states,
                                List<Long> categories,
                                String rangeStart,
                                String rangeEnd,
                                int from,
                                int size);

    EventDto updateEvent(long eventId, AdminUpdateEventDto adminUpdateEventDto);
}
