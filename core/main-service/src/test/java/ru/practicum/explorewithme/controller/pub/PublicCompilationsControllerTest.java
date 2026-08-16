package ru.practicum.explorewithme.controller.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.explorewithme.exception.handler.ErrorHandler;
import ru.practicum.explorewithme.service.CompilationsService;
import ru.practicum.explorewithme.test.ControllerTest;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static ru.practicum.explorewithme.controller.ControllerConstants.*;

public class PublicCompilationsControllerTest extends ControllerTest {
    @InjectMocks
    private PublicCompilationsController publicCompilationsController;
    @Mock
    private CompilationsService compilationsService;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(publicCompilationsController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    public void getCompilations_StatusOk() {
        // Arrange
        Boolean pinned = false;
        int from = 0;
        int size = 10;

        // Act
        ResultActions result = performGet(createGetCompilationsUrl(pinned, from, size));

        // Assert
        expectStatusOk(result);
        assertMethodCall(compilationsService, service ->
                service.getCompilations(Mockito.eq(pinned), Mockito.eq(from), Mockito.eq(size)));
    }

    @Test
    public void getCompilation_StatusOK() {
        // Arrange
        long compilationId = 1L;

        // Act
        ResultActions result = performGet(createIdUrl(URL_COMPILATIONS, compilationId));

        // Assert
        expectStatusOk(result);
        assertMethodCall(compilationsService, service ->
                service.getCompilation(Mockito.eq(compilationId)));
    }

    private String createGetCompilationsUrl(Boolean pinned, int from, int size) {
        return String.format("%s?%s=%b&%s=%d&%s=%d",
                URL_COMPILATIONS,
                PARAM_PINNED, pinned,
                PARAM_FROM, from,
                PARAM_SIZE, size);
    }
}
