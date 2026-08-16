package ru.practicum.explorewithme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.practicum.explorewithme.entity.EndpointHit;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.repository.EndpointHitRepository;
import ru.practicum.explorewithme.service.mapper.EndpointHitMapper;
import ru.practicum.explorewithme.test.ServiceTest;

public class EndpointHitServiceImplTest extends ServiceTest {
    private final EndpointHitMapper endpointHitMapper = new EndpointHitMapper();

    private EndpointHitServiceImpl hitService;
    @Mock
    private EndpointHitRepository endpointHitRepository;

    @BeforeEach
    public void setUp() {
        hitService = new EndpointHitServiceImpl(endpointHitMapper, endpointHitRepository);
    }

    @Test
    public void saveHit_saveCalls() {
        // Arrange
        EndpointHitRequest request = buildEndpointHitRequest();

        // Act
        hitService.saveHit(request);

        // Assert
        assertMethodCall(endpointHitRepository, repository ->
                repository.save(Mockito.any(EndpointHit.class)));
    }
}
