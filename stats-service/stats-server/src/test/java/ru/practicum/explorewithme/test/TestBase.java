package ru.practicum.explorewithme.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.hit.EndpointHitRequest;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
public class TestBase {
    protected static final LocalDateTime NOW = LocalDateTime.now();

    protected EndpointHitRequest buildEndpointHitRequest() {
        return EndpointHitRequest.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(NOW.plusDays(1))
                .build();
    }

    protected <M> void assertMethodCall(M mock, Consumer<M> methodCall) {
        methodCall.accept(Mockito.verify(
                mock,
                Mockito.times(1))
        );
    }
}
