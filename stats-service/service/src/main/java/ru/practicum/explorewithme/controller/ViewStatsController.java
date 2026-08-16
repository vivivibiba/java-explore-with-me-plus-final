package ru.practicum.explorewithme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.service.ViewStatsService;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(ViewStatsController.URL_BASE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ViewStatsController {
    public static final String URL_BASE = "/stats";
    public static final String PARAM_START = "start";
    public static final String PARAM_END = "end";
    public static final String PARAM_URIS = "uris";
    public static final String PARAM_UNIQUE = "unique";

    private final ViewStatsService viewStatsService;

    @GetMapping
    public ResponseEntity<List<ViewStatsResponse>> getStatistics(
            @RequestParam(name = PARAM_START)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(name = PARAM_END)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(name = PARAM_URIS, required = false) List<String> uris,
            @RequestParam(name = PARAM_UNIQUE, required = false, defaultValue = "false") boolean unique) {
        ResponseEntity<List<ViewStatsResponse>> statsList = ResponseEntity.status(HttpStatus.OK)
                .body(viewStatsService.getStatistics(start, end, uris, unique));
        log.debug("Статистические данные {} извлечены из базы данных", statsList);
        return statsList;
    }
}
