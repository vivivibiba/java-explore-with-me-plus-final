package ru.practicum.explorewithme.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.explorewithme.exception.handler.ErrorHandler;
import ru.practicum.explorewithme.service.ViewStatsService;
import ru.practicum.explorewithme.test.ControllerTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class ViewStatsControllerTest extends ControllerTest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @InjectMocks
    private ViewStatsController viewStatsController;
    @Mock
    private ViewStatsService viewStatsService;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(viewStatsController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    public void getStatistics_StatusOk() {
        // Arrange
        LocalDateTime start = NOW.minusDays(2);
        LocalDateTime end = NOW.minusDays(1);
        List<String> uris = List.of("uri");
        boolean unique = true;

        // Act
        ResultActions result = performGet(createGetStatisticsUrl(start, end, uris, unique));

        // Assert
        expectStatusOk(result);
        assertMethodCall(viewStatsService, service ->
                service.getStatistics(
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(),
                        Mockito.eq(unique)
                ));
    }

    private String createGetStatisticsUrl(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique) {
        StringBuilder builder = new StringBuilder(
                String.format("%s?%s=%s&%s=%s",
                        ViewStatsController.URL_BASE,
                        ViewStatsController.PARAM_START, start.format(FORMATTER),
                        ViewStatsController.PARAM_END, end.format(FORMATTER))
        );
        for (String uri : uris) {
            builder.append(String.format("&%s=%s", ViewStatsController.PARAM_URIS, uri));
        }
        builder.append(String.format("&%s=%s", ViewStatsController.PARAM_UNIQUE, unique));
        return builder.toString();
    }
}
