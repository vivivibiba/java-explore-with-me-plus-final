package ru.practicum.explorewithme.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.exception.handler.ErrorHandler;
import ru.practicum.explorewithme.service.CompilationsService;
import ru.practicum.explorewithme.test.ControllerTest;

import java.util.List;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static ru.practicum.explorewithme.controller.ControllerConstants.ACCESS_ADMIN;
import static ru.practicum.explorewithme.controller.ControllerConstants.URL_COMPILATIONS;

public class AdminCompilationsControllerTest extends ControllerTest {
    private static final String URL_BASE = ACCESS_ADMIN + URL_COMPILATIONS;
    @InjectMocks
    private AdminCompilationsController adminCompilationsController;
    @Mock
    private CompilationsService compilationsService;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(adminCompilationsController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    public void createCompilation_StatusCreated() {
        // Arrange
        NewCompilationDto body = buildNewCompilationDto(List.of());

        // Act
        ResultActions result = performPost(URL_BASE, body);

        // Assert
        expectStatusCreated(result);
        assertMethodCall(compilationsService, service ->
                service.createCompilation(Mockito.any(NewCompilationDto.class)));
    }

    @Test
    public void deleteCompilation_StatusNoContent() {
        // Arrange
        long compId = 1L;

        // Act
        ResultActions result = performDelete(createIdUrl(URL_BASE, compId));

        // Assert
        expectStatusNoContent(result);
        assertMethodCall(compilationsService, service ->
                service.deleteCompilation(Mockito.eq(compId)));
    }

    @Test
    public void updateCompilation_StatusOk() {
        // Arrange
        long compId = 1L;
        UpdateCompilationRequest body = buildUpdateCompilationRequest(List.of());

        // Act
        ResultActions result = performPatch(createIdUrl(URL_BASE, compId), body);

        // Assert
        expectStatusOk(result);
        assertMethodCall(compilationsService, service ->
                service.updateCompilation(Mockito.eq(compId), Mockito.any(UpdateCompilationRequest.class)));
    }
}
