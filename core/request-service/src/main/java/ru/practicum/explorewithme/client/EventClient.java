package ru.practicum.explorewithme.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explorewithme.internal.EventInternalDto;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/internal/events/{eventId}")
    EventInternalDto getEvent(@PathVariable("eventId") long eventId);

    @PatchMapping("/internal/events/{eventId}/confirmed-requests")
    EventInternalDto adjustConfirmedRequests(@PathVariable("eventId") long eventId, @RequestParam("delta") int delta);
}
