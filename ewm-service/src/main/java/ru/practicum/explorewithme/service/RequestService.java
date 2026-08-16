package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;

import java.util.List;

public interface RequestService {
    List<RequestDto> getRequestsToUsersEvent(long eventId);

    List<RequestDto> changeRequestStatuses(List<Long> requestIds, RequestStatus status);

    RequestDto create(long userId, long eventId);

    RequestDto cancelRequest(long userId, long requestId);

    List<RequestDto> getUserRequests(long requesterId);
}
