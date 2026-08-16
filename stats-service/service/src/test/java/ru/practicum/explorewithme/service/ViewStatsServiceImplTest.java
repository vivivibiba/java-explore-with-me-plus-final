package ru.practicum.explorewithme.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.practicum.explorewithme.exception.WrongDateIntervalException;
import ru.practicum.explorewithme.repository.EndpointHitRepository;
import ru.practicum.explorewithme.test.ServiceTest;

import java.time.LocalDateTime;
import java.util.List;

public class ViewStatsServiceImplTest extends ServiceTest {
    @InjectMocks
    private ViewStatsServiceImpl viewStatsService;
    @Mock
    private EndpointHitRepository endpointHitRepository;

    @Test
    public void getStatistics_StartAfterEnd_WrongDateIntervalException() {
        // Arrange
        LocalDateTime start = NOW.minusDays(1);
        LocalDateTime end = NOW.minusDays(2);
        List<String> uris = List.of("");
        boolean unique = true;

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> viewStatsService.getStatistics(start, end, uris, unique));

        // Assert
        Assertions.assertThat(thrown)
                .isInstanceOf(WrongDateIntervalException.class);
    }

    @Test
    public void getStatistics_UniqueTrue_UniqueMethodCalls() {
        // Arrange
        LocalDateTime start = NOW.minusDays(2);
        LocalDateTime end = NOW.minusDays(1);
        List<String> uris = List.of("");
        boolean unique = true;

        // Act
        viewStatsService.getStatistics(start, end, uris, unique);

        // Assert
        assertMethodCall(endpointHitRepository, repository -> repository.findUniqueStatsByDateAndUris(
                Mockito.eq(start),
                Mockito.eq(end),
                Mockito.eq(uris)
        ));
    }

    @Test
    public void getStatistics_UniqueFalse_UsualMethodCalls() {
        // Arrange
        LocalDateTime start = NOW.minusDays(2);
        LocalDateTime end = NOW.minusDays(1);
        List<String> uris = List.of("");
        boolean unique = false;

        // Act
        viewStatsService.getStatistics(start, end, uris, unique);

        // Assert
        assertMethodCall(endpointHitRepository, repository -> repository.findStatsByDateAndUris(
                Mockito.eq(start),
                Mockito.eq(end),
                Mockito.eq(uris)
        ));
    }

    @Test
    public void getStatistics_NullUris_UniqueFalse_UsualMethodCalls() {
        // Arrange
        LocalDateTime start = NOW.minusDays(2);
        LocalDateTime end = NOW.minusDays(1);
        boolean unique = false;

        // Act
        viewStatsService.getStatistics(start, end, null, unique);

        // Assert
        assertMethodCall(endpointHitRepository, repository -> repository.findStatsByDate(
                Mockito.eq(start),
                Mockito.eq(end)
        ));
    }

    @Test
    public void getStatistics_NullUris_UniqueTrue_UniqueMethodCalls() {
        // Arrange
        LocalDateTime start = NOW.minusDays(2);
        LocalDateTime end = NOW.minusDays(1);
        boolean unique = true;

        // Act
        viewStatsService.getStatistics(start, end, null, unique);

        // Assert
        assertMethodCall(endpointHitRepository, repository -> repository.findUniqueStatsByDate(
                Mockito.eq(start),
                Mockito.eq(end)
        ));
    }
}
