package ru.practicum.explorewithme.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;
import ru.practicum.explorewithme.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController {
    private final RequestService requestService;

    @GetMapping("/events/{eventId}")
    public List<RequestDto> getByEvent(@PathVariable long eventId) {
        return requestService.getRequestsToUsersEvent(eventId);
    }

    @PatchMapping("/statuses")
    public List<RequestDto> changeStatuses(@RequestBody UpdateRequestStatusDto request) {
        return requestService.changeRequestStatuses(request.getRequestIds(), request.getStatus());
    }
}
