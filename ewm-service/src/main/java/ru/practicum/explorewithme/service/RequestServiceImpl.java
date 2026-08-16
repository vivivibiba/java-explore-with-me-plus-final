package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.event.EventStatus;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.Request;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestServiceImpl extends ServiceBase implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<RequestDto> getRequestsToUsersEvent(long eventId) {
        log.trace("Инициировано получение запросов к событию с id {}", eventId);
        List<Request> requests = requestRepository.findByEventId(eventId);
        log.debug("Получен список запросов {}", requests);
        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .event(request.getEvent().getId())
                        .requester(request.getRequester().getId())
                        .status(request.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public List<RequestDto> changeRequestStatuses(List<Long> requestIds, RequestStatus status) {
        log.trace("Инициировано изменение статусов на {} к списку запросов {}", status, requestIds);
        List<Request> requests = requestRepository.findByIdIn(requestIds);
        Optional<Long> nonPendingRequestId = requests.stream()
                .filter(request -> !request.getStatus().equals(RequestStatus.PENDING))
                .findFirst()
                .map(Request::getId);

        if (nonPendingRequestId.isPresent()) {
            throw new UnavailableUpdateException(Entities.REQUEST.name(), nonPendingRequestId.get());
        }

        requests.forEach(request -> request.setStatus(status));
        log.debug("Изменен статус у запросов {}", requests);
        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .event(request.getEvent().getId())
                        .requester(request.getRequester().getId())
                        .status(request.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public RequestDto create(long userId, long eventId) {
        log.trace("Инициировано создание запроса у пользователя с id {} для события с id {}", userId, eventId);
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DuplicatedDataException(Entities.REQUEST.name(), "eventId and userId", eventId);
        }

        Event event = findEntityIn(eventRepository, eventId, Entities.EVENT);
        requestChecks(event, userId);
        User requester = findEntityIn(userRepository, userId, Entities.USER);
        RequestStatus status = changeRequestStatus(event);

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();

        Request createdRequest = requestRepository.save(request);
        incrementEventConfirmedRequests(createdRequest, event);
        log.debug("Создан запрос {}", createdRequest);
        return RequestDto.builder()
                .id(createdRequest.getId())
                .created(createdRequest.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(createdRequest.getStatus())
                .build();
    }


    @Override
    @Transactional
    public RequestDto cancelRequest(long userId, long requestId) {
        log.trace("Инициирована отмена запроса с id {} у пользователя с id {}", userId, requestId);
        Request request = findEntityIn(requestRepository, requestId, Entities.REQUEST);

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new DuplicatedDataException(Entities.REQUEST.name(), "status", RequestStatus.CONFIRMED);
        }

        request.setStatus(RequestStatus.CANCELED);
        Request updatedRequest = requestRepository.save(request);
        log.debug("Обновлен запрос {}", updatedRequest);
        decrementEventConfirmedRequests(updatedRequest);
        return RequestDto.builder()
                .id(updatedRequest.getId())
                .created(updatedRequest.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(updatedRequest.getStatus())
                .build();
    }

    @Override
    public List<RequestDto> getUserRequests(long requesterId) {
        log.trace("Инициировано получение запросов пользователя с id {}", requesterId);
        List<Request> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(requesterId);
        log.debug("Получены запросы пользователя {}", requests);
        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .event(request.getEvent().getId())
                        .requester(request.getRequester().getId())
                        .status(request.getStatus())
                        .build())
                .toList();
    }

    private void requestChecks(Event event, long userId) {
        if (event.getInitiator().getId() == userId) {
            throw new DuplicatedDataException(Entities.REQUEST.name(), "eventId and userId", event.getId());
        }

        if (!event.getStatus().equals(EventStatus.PUBLISHED)) {
            throw new UnavailableUpdateException(Entities.EVENT.name(), event.getId());
        }

        if (event.getParticipantLimit() - event.getConfirmedRequests() == 0 && event.getParticipantLimit() != 0) {
            throw new UnavailableUpdateException(Entities.EVENT.name(), event.getId());
        }
    }

    private RequestStatus changeRequestStatus(Event event) {
        RequestStatus status;

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }

        return status;
    }

    private void incrementEventConfirmedRequests(Request request, Event event) {
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            int confirmedRequests = event.getConfirmedRequests();
            event.setConfirmedRequests(++confirmedRequests);
            Event e = eventRepository.save(event);
        }
    }

    private void decrementEventConfirmedRequests(Request request) {
        if (request.getStatus().equals(RequestStatus.CANCELED)) {
            Event event = findEntityIn(eventRepository, request.getEvent().getId(), Entities.REQUEST);
            int confirmedRequests = event.getConfirmedRequests();
            event.setConfirmedRequests(--confirmedRequests);
            eventRepository.save(event);
        }
    }
}
