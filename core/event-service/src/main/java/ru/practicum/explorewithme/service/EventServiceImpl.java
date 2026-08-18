package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.explorewithme.AnalyzerClient;
import ru.practicum.explorewithme.CollectorClient;
import ru.practicum.explorewithme.RecommendedEvent;
import ru.practicum.explorewithme.UserActionType;
import ru.practicum.explorewithme.client.RequestClient;
import ru.practicum.explorewithme.client.UserClient;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.request.ChangedRequestStatusesDto;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.exception.*;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.specification.AdminEventSearchSpecification;
import ru.practicum.explorewithme.repository.specification.UsersEventSearchSpecifications;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl extends ServiceBase implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;
    private final TransactionTemplate transactionTemplate;
    private static final int ADMIN_MIN_OFFSET = 1;

    @Override
    public List<EventDto> getEvents(long userId, int from, int size) {
        log.trace("Инициировано получение событий пользователем с id {}", userId);
        Pageable pageable = new OffsetPageRequest(from, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        log.debug("Получен список событий {}", events);
        if (events.isEmpty()) {
            return List.of();
        }

        return getEventsWithRatings(events);
    }

    @Override
    public EventDto createEvent(long userId, NewEventDto newEventDto) {
        log.trace("Инициировано создание события {} пользователем с id {}", newEventDto, userId);

        Category category = findEntityIn(categoryRepository, newEventDto.getCategory(), Entities.CATEGORY);
        UserShortDto initiator = userClient.getUser(userId);
        LocationEmbeddable locationEmbeddable = LocationMapper.toLocationEmbeddable(newEventDto.getLocation());

        Event event = EventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setInitiatorId(initiator.getId());
        event.setInitiatorName(initiator.getName());
        event.setLocation(locationEmbeddable);
        setParamsOnCreation(event, newEventDto);

        Event createdEvent = transactionTemplate.execute(transactionStatus ->
                eventRepository.save(event)
        );

        log.debug("Создано событие {}", createdEvent);
        EventDto result = getEventsWithRatings(List.of(createdEvent)).getFirst();
        log.debug("Событие преобразовано в DTO {}", result);
        return result;
    }

    @Override
    public EventDto getEvent(long userId, long eventId) {
        log.trace("Инициировано получение события {} пользователем с id {}", eventId, userId);
        Event event = getEventById(eventId, userId);
        log.debug("Получено событие {}", event);
        return getEventsWithRatings(List.of(event)).getFirst();
    }

    @Override
    public EventDto updateEvent(long userId, long eventId, UserUpdateEventDto update) {
        log.trace("Инициировано обновление события с id {} пользователем с id {}, изменения - {}", eventId, userId, update);

        Event updatedEvent = transactionTemplate.execute(transactionStatus -> {
            Event event = getEventById(eventId, userId);
            checkEventNotCanceled(event);
            EventStatus status = changeEventStatus(update);
            updateEventFields(event, update, status);
            return eventRepository.save(event);
        });

        log.debug("Обновлено событие {}", updatedEvent);
        return getEventsWithRatings(List.of(updatedEvent)).getFirst();
    }

    @Override
    public List<RequestDto> getRequests(long userId, long eventId) {
        log.trace("Инициировано получение запроса пользователем с id {} к событию с id {}", userId, eventId);
        checkEventExistence(userId, eventId);
        return requestClient.getRequestsByEvent(eventId);
    }

    @Override
    public ChangedRequestStatusesDto updateRequestStatuses(long userId, long eventId, UpdateRequestStatusDto update) {
        log.trace("Инициировано обновление статусов запросов пользователем с id {} к событию с id {}, изменения - {}", userId, eventId, update);
        Event event = getEventById(eventId, userId);
        //Обработка ситуации, когда не установлено ограничение по количеству участников
        //или запрос не требует модерации
        if ((event.getParticipantLimit() == 0 || !event.isRequestModeration()) && update.getStatus().equals(RequestStatus.CONFIRMED)) {
            return confirmRequestsIfNoDemandsMade(event, update);
        }

        int requestsAvailableToConfirm = event.getParticipantLimit() - event.getConfirmedRequests();

        if (requestsAvailableToConfirm == 0) {
            throw new UnavailableUpdateException(Entities.EVENT.name(), eventId);
        }

        //Обработка случая подтверждения запросов
        if (update.getStatus().equals(RequestStatus.CONFIRMED)) {
            return processRequestsWithActionStatusConfirmed(event, update, requestsAvailableToConfirm);
        }
        //Обработка случая отклонения запросов
        return processRequestsWithActionStatusRejected(update);
    }

    @Override
    public List<EventShortDto> getPublishedEvents(String text,
                                                  List<Long> categories,
                                                  Boolean paid,
                                                  LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd,
                                                  boolean onlyAvailable,
                                                  PublicEventSort sort,
                                                  int from,
                                                  int size) {
        log.trace("Инициировано получение опубликованных событий");
        checkDateRange(rangeStart, rangeEnd);

        LocalDateTime start = rangeStart;
        if (rangeStart == null && rangeEnd == null) {
            start = LocalDateTime.now();
        }

        Specification<Event> specification = Specification.<Event>unrestricted()
                .and(UsersEventSearchSpecifications.hasStatus(EventStatus.PUBLISHED))
                .and(UsersEventSearchSpecifications.textContains(text))
                .and(UsersEventSearchSpecifications.categoryIn(categories))
                .and(UsersEventSearchSpecifications.paidEquals(paid))
                .and(UsersEventSearchSpecifications.eventDateFrom(start))
                .and(UsersEventSearchSpecifications.eventDateTo(rangeEnd))
                .and(UsersEventSearchSpecifications.onlyAvailable(onlyAvailable));

        if (sort == PublicEventSort.VIEWS) {
            List<Event> events = eventRepository.findAll(specification);

            if (events.isEmpty()) {
                return List.of();
            }

            List<EventShortDto> dtos = getEventsWithRatings(events).stream()
                    .map(EventMapper::toEventShortDto)
                    .sorted(Comparator.comparingDouble(EventShortDto::getRating).reversed())
                    .toList();
            log.debug("Получен список опубликованных событий {}", dtos);
            return getPage(dtos, from, size);
        }

        Pageable pageable = new OffsetPageRequest(from, size, Sort.by("eventDate").ascending());
        Page<Event> eventPage = eventRepository.findAll(specification, pageable);
        List<Event> events = eventPage.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<EventShortDto> dtos = getEventsWithRatings(events).stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        log.debug("Получен список опубликованных событий {}", dtos);
        return dtos;
    }

    @Override
    public EventDto getPublishedEvent(long eventId, long userId) {
        log.trace("Инициировано получение опубликованного события с id {}", eventId);
        Event event = eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(Entities.EVENT, eventId));
        sendAction(userId, eventId, UserActionType.VIEW);
        return getEventsWithRatings(List.of(event)).getFirst();
    }

    @Override
    public List<EventShortDto> getRecommendations(long userId, int maxResults) {
        List<Long> recommendedIds = analyzerClient.getRecommendationsForUser(userId, maxResults)
                .map(RecommendedEvent::eventId)
                .toList();

        if (recommendedIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Event> eventsById = eventRepository.findByIdIn(recommendedIds).stream()
                .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        List<Event> orderedEvents = recommendedIds.stream()
                .map(eventsById::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        return getEventsWithRatings(orderedEvents).stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public void likeEvent(long eventId, long userId) {
        eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(Entities.EVENT, eventId));

        boolean visited = requestClient.getRequestsByEvent(eventId).stream()
                .anyMatch(request -> request.getRequester() == userId
                        && request.getStatus() == RequestStatus.CONFIRMED);
        if (!visited) {
            throw new BadRequestException("User can like only an attended event");
        }
        sendAction(userId, eventId, UserActionType.LIKE);
    }

    @Override
    public Event getEventById(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(Entities.EVENT, eventId));
    }

    @Override
    public List<EventDto> searchEvents(List<Long> users,
                                       List<String> states,
                                       List<Long> categories,
                                       String rangeStart,
                                       String rangeEnd,
                                       int from,
                                       int size) {
        log.trace("Инициирован поиск событий");
        Specification<Event> spec = new AdminEventSearchSpecification(users, states, categories, rangeStart, rangeEnd);
        Pageable pageable = new OffsetPageRequest(from, size);
        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventPage.getContent();
        log.debug("Получен список событий {}", events);
        if (events.isEmpty()) {
            return List.of();
        }

        return getEventsWithRatings(events);
    }

    @Override
    public EventDto updateEvent(long eventId, AdminUpdateEventDto update) {
        log.trace("Инициировано обновление события с id {} администратором, изменения - {}", eventId, update);

        Event updatedEvent = transactionTemplate.execute(transactionStatus -> {
            Event event = findEntityIn(eventRepository, eventId, Entities.EVENT);
            EventStatus status = changeEventStatus(event, update);

            //Валидация даты события проходит после изменения статуса для случая изменения данных о событии одновременно с публикацией
            if (event.getPublishedOn() != null) {
                if (update.getEventDate() != null && !update.getEventDate().isAfter(event.getPublishedOn().plusHours(ADMIN_MIN_OFFSET))) {
                    throw new EarlyDateException(ADMIN_MIN_OFFSET, event.getEventDate());
                }
                //Обработка случая, если событие изменяется, но не опубликовано
            } else {
                if (update.getEventDate() != null && !update.getEventDate().isAfter(event.getCreatedOn().plusHours(ADMIN_MIN_OFFSET))) {
                    throw new EarlyDateException(ADMIN_MIN_OFFSET, event.getEventDate());
                }
            }

            updateEventFields(event, update, status);
            return eventRepository.save(event);
        });

        log.debug("Обновлено событие {}", updatedEvent);
        return getEventsWithRatings(List.of(updatedEvent)).getFirst();
    }

    private <T> List<T> getPage(List<T> source, int from, int size) {
        if (from >= source.size()) {
            return List.of();
        }

        int toIndex = Math.min(from + size, source.size());
        return source.subList(from, toIndex);
    }

    private List<EventDto> getEventsWithRatings(List<Event> events) {
        return super.getEventsWithRatings(events, analyzerClient);
    }

    private void sendAction(long userId, long eventId, UserActionType actionType) {
        collectorClient.collect(userId, eventId, actionType);
    }

    private void checkDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new WrongDateIntervalException("rangeStart must be before rangeEnd");
        }
    }

    private void updateEventFields(Event event, EventUpdateCommon update, EventStatus status) {
        if (update.getAnnotation() != null && !update.getAnnotation().isEmpty()) {
            event.setAnnotation(update.getAnnotation());
        }
        if (update.getDescription() != null && !update.getDescription().isEmpty()) {
            event.setDescription(update.getDescription());
        }
        if (update.getCategory() != null) {
            Category category = findEntityIn(categoryRepository, update.getCategory(), Entities.CATEGORY);
            event.setCategory(category);
        }
        if (update.getEventDate() != null) {
            event.setEventDate(update.getEventDate());
        }
        if (update.getLocation() != null) {
            LocationEmbeddable location = LocationMapper.toLocationEmbeddable(update.getLocation());
            event.setLocation(location);
        }

        if (update.getPaid() != null) {
            event.setPaid(update.getPaid());
        }

        if (update.getAnnotation() != null && !update.getAnnotation().isEmpty()) {
            event.setAnnotation(update.getAnnotation());
        }

        if (update.getParticipantLimit() != null) {
            event.setParticipantLimit(update.getParticipantLimit());
        }

        if (update.getRequestModeration() != null) {
            event.setRequestModeration(update.getRequestModeration());
        }

        event.setStatus(status);

        if (update.getTitle() != null && !update.getTitle().isEmpty()) {
            event.setTitle(update.getTitle());
        }
    }

    private void checkEventExistence(long userId, long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new NotFoundException(Entities.EVENT, eventId);
        }
    }

    private void setParamsOnCreation(Event event, NewEventDto newEventDto) {
        event.setCreatedOn(LocalDateTime.now());
        event.setStatus(EventStatus.PENDING);
        event.setRequestModeration(newEventDto.getRequestModerationOrDefault());
    }

    private void checkEventNotCanceled(Event event) {
        if (!event.getStatus().equals(EventStatus.CANCELED) && !event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException(Entities.EVENT.name(), event.getId());
        }
    }

    private void checkEventNotPublished(Event event) {
        if (!event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException(Entities.EVENT.name(), event.getId());
        }
    }

    private EventStatus changeEventStatus(UserUpdateEventDto update) {
        EventStatus status;

        if (update.getStatus() != null && update.getStatus().equals(UserEventUpdateAction.CANCEL_REVIEW)) {
            status = EventStatus.CANCELED;
        } else {
            status = EventStatus.PENDING;
        }

        return status;
    }

    private EventStatus changeEventStatus(Event event, AdminUpdateEventDto update) {
        EventStatus status;

        if (update.getStatus() != null && update.getStatus().equals(AdminEventUpdateAction.PUBLISH_EVENT)) {
            checkEventNotPublished(event);
            status = EventStatus.PUBLISHED;
            event.setPublishedOn(LocalDateTime.now());
        } else if (update.getStatus() != null && update.getStatus().equals(AdminEventUpdateAction.REJECT_EVENT)) {
            checkEventNotPublished(event);
            status = EventStatus.CANCELED;
        } else {
            status = event.getStatus();
        }

        return status;
    }

    private ChangedRequestStatusesDto changeRequestStatuses(Event event, List<Long> requestsToConfirm, List<Long> requestsToReject) {
        List<RequestDto> confirmedRequests;
        if (!requestsToConfirm.isEmpty()) {
            confirmedRequests = requestClient.changeStatuses(
                    UpdateRequestStatusDto.builder()
                            .requestIds(requestsToConfirm)
                            .status(RequestStatus.CONFIRMED)
                            .build()
            );
        } else {
            confirmedRequests = List.of();
        }

        List<RequestDto> rejectedRequests;
        if (!requestsToReject.isEmpty()) {
            rejectedRequests = requestClient.changeStatuses(
                    UpdateRequestStatusDto.builder()
                            .requestIds(requestsToReject)
                            .status(RequestStatus.REJECTED)
                            .build()
            );
        } else {
            rejectedRequests = List.of();
        }

        if (!confirmedRequests.isEmpty()) {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                Event currentEvent = findEntityIn(eventRepository, event.getId(), Entities.EVENT);
                currentEvent.setConfirmedRequests(
                        currentEvent.getConfirmedRequests() + confirmedRequests.size()
                );
                eventRepository.save(currentEvent);
            });
        }

        log.debug(
                "Обновлены статусы запросов: подтвержденные - {}, отклоненные - {}",
                confirmedRequests,
                rejectedRequests
        );

        return ChangedRequestStatusesDto.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private ChangedRequestStatusesDto confirmRequestsIfNoDemandsMade(Event event, UpdateRequestStatusDto update) {
        List<Long> requestIds = update.getRequestIds();
        return changeRequestStatuses(event, requestIds, List.of());
    }

    private ChangedRequestStatusesDto processRequestsWithActionStatusConfirmed(Event event,
                                                                               UpdateRequestStatusDto update,
                                                                               int requestsAvailableToConfirm) {
        //Разделяем запросы на те, которые можем одобрить и отклонить, исходя из количества доступных мест
        List<Long> requestsToConfirm = update.getRequestIds().stream()
                .limit(requestsAvailableToConfirm)
                .toList();

        List<Long> requestsToReject = update.getRequestIds().stream()
                .skip(requestsAvailableToConfirm)
                .toList();

        return changeRequestStatuses(event, requestsToConfirm, requestsToReject);
    }

    private ChangedRequestStatusesDto processRequestsWithActionStatusRejected(UpdateRequestStatusDto update) {
        List<RequestDto> rejectedRequests = requestClient.changeStatuses(UpdateRequestStatusDto.builder().requestIds(update.getRequestIds()).status(RequestStatus.REJECTED).build());

        return ChangedRequestStatusesDto.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(rejectedRequests)
                .build();
    }
}
