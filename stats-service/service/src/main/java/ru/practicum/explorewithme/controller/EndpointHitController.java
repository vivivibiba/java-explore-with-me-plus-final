package ru.practicum.explorewithme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.service.EndpointHitService;

@RestController
@RequestMapping(EndpointHitController.URL_BASE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class EndpointHitController {
    public static final String URL_BASE = "/hit";

    private final EndpointHitService endpointHitService;

    @PostMapping
    public ResponseEntity<Void> saveHit(@RequestBody @Valid EndpointHitRequest request) {
        endpointHitService.saveHit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}
