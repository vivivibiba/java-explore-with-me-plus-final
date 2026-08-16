package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.hit.EndpointHitRequest;

public interface EndpointHitService {
    void saveHit(EndpointHitRequest request);
}
