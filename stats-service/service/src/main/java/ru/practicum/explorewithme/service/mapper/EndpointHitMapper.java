package ru.practicum.explorewithme.service.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.entity.EndpointHit;
import ru.practicum.explorewithme.hit.EndpointHitRequest;

@Component
public class EndpointHitMapper {
    public EndpointHit toEndpointHit(EndpointHitRequest request) {
        return EndpointHit.builder()
                .app(request.getApp())
                .uri(request.getUri())
                .ip(request.getIp())
                .timestamp(request.getTimestamp())
                .build();
    }
}
