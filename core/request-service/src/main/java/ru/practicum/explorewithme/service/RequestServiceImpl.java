package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.explorewithme.CollectorClient;
import ru.practicum.explorewithme.UserActionType;
import ru.practicum.explorewithme.client.EventClient;
import ru.practicum.explorewithme.client.UserClient;
import ru.practicum.explorewithme.dto.event.EventStatus;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.entity.Request;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.internal.EventInternalDto;
import ru.practicum.explorewithme.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl extends ServiceBase implements RequestService {
    private final RequestRepository requestRepository;
    private final EventClient eventClient;
    private final UserClient userClient;
    private final CollectorClient collectorClient;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsToUsersEvent(long eventId) {
        return requestRepository.findByEventId(eventId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public List<RequestDto> changeRequestStatuses(List<Long> requestIds, RequestStatus status) {
        List<Request> requests = requestRepository.findByIdIn(requestIds);
        Optional<Long> nonPendingRequestId = requests.stream()
                .filter(request -> request.getStatus() != RequestStatus.PENDING)
                .findFirst()
                .map(Request::getId);
        if (nonPendingRequestId.isPresent()) {
            throw new UnavailableUpdateException(Entities.REQUEST.name(), nonPendingRequestId.get());
        }
        requests.forEach(request -> request.setStatus(status));
        return requestRepository.saveAll(requests).stream().map(this::toDto).toList();
    }

    @Override
    public RequestDto create(long userId, long eventId) {
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DuplicatedDataException(Entities.REQUEST.name(), "eventId and userId", eventId);
        }

        EventInternalDto event = eventClient.getEvent(eventId);
        userClient.getUser(userId);
        validateRequest(event, userId);

        RequestStatus status = event.getParticipantLimit() == 0 || !event.isRequestModeration()
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;
        Request created = transactionTemplate.execute(transactionStatus ->
                requestRepository.save(Request.builder()
                        .created(LocalDateTime.now())
                        .eventId(eventId)
                        .requesterId(userId)
                        .status(status)
                        .build())
        );

        collectorClient.collect(userId, eventId, UserActionType.REGISTER);
        if (status == RequestStatus.CONFIRMED) {
            eventClient.adjustConfirmedRequests(eventId, 1);
        }
        return toDto(created);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(long userId, long requestId) {
        Request request = findEntityIn(requestRepository, requestId, Entities.REQUEST);
        if (request.getRequesterId() != userId) {
            throw new UnavailableUpdateException(Entities.REQUEST.name(), requestId);
        }
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new DuplicatedDataException(Entities.REQUEST.name(), "status", RequestStatus.CONFIRMED);
        }
        request.setStatus(RequestStatus.CANCELED);
        return toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getUserRequests(long requesterId) {
        return requestRepository.findByRequesterIdOrderByCreatedDesc(requesterId).stream()
                .map(this::toDto)
                .toList();
    }

    private void validateRequest(EventInternalDto event, long userId) {
        if (event.getInitiatorId() == userId) {
            throw new DuplicatedDataException(Entities.REQUEST.name(), "eventId and userId", event.getId());
        }
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new UnavailableUpdateException(Entities.EVENT.name(), event.getId());
        }
        if (event.getParticipantLimit() != 0
                && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new UnavailableUpdateException(Entities.EVENT.name(), event.getId());
        }
    }

    private RequestDto toDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }
}
