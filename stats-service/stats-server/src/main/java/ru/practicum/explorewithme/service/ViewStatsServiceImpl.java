package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.exception.WrongDateIntervalException;
import ru.practicum.explorewithme.repository.EndpointHitRepository;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ViewStatsServiceImpl implements ViewStatsService {
    private final EndpointHitRepository endpointHitRepository;

    @Override
    public List<ViewStatsResponse> getStatistics(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        checkDateInterval(start, end);
        if (uris == null) {
            return findStats(start, end, unique);
        }
        return findStatsWithUris(start, end, uris, unique);
    }

    private void checkDateInterval(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new WrongDateIntervalException(start, end);
        }
    }

    private List<ViewStatsResponse> findStats(
            LocalDateTime start,
            LocalDateTime end,
            boolean unique
    ) {
        if (unique) {
            return endpointHitRepository.findUniqueStatsByDate(start, end);
        }
        return endpointHitRepository.findStatsByDate(start, end);
    }

    private List<ViewStatsResponse> findStatsWithUris(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        if (unique) {
            return endpointHitRepository.findUniqueStatsByDateAndUris(start, end, uris);
        }
        return endpointHitRepository.findStatsByDateAndUris(start, end, uris);
    }
}
