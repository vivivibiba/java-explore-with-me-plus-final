package ru.practicum.explorewithme.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestClient {
    @GetMapping("/internal/requests/events/{eventId}")
    List<RequestDto> getRequestsByEvent(@PathVariable("eventId") long eventId);

    @PatchMapping("/internal/requests/statuses")
    List<RequestDto> changeStatuses(@RequestBody UpdateRequestStatusDto request);
}
