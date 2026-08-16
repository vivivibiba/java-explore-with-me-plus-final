package ru.practicum.explorewithme.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.explorewithme.exception.handler.ErrorHandler;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.service.EndpointHitService;
import ru.practicum.explorewithme.test.ControllerTest;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class EndpointHitControllerTest extends ControllerTest {
    @InjectMocks
    private EndpointHitController endpointHitController;
    @Mock
    private EndpointHitService endpointHitService;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(endpointHitController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    public void create_StatusOk() {
        // Arrange
        EndpointHitRequest request = buildEndpointHitRequest();

        // Act
        ResultActions result = performPost(EndpointHitController.URL_BASE, request);

        // Assert
        expectStatusCreated(result);
        assertMethodCall(endpointHitService, service ->
                service.saveHit(Mockito.any(EndpointHitRequest.class)));
    }
}
