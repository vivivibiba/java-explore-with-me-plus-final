package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ViewStatsService {
    List<ViewStatsResponse> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
