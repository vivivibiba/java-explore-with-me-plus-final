package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.entity.EndpointHit;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
        SELECT NEW ru.practicum.explorewithme.stats.ViewStatsResponse(h.app, h.uri, COUNT(h))
        FROM EndpointHit h
        WHERE h.timestamp BETWEEN :start AND :end
          AND h.uri IN :uris
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h) DESC
        """)
    List<ViewStatsResponse> findStatsByDateAndUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    @Query("""
        SELECT NEW ru.practicum.explorewithme.stats.ViewStatsResponse(h.app, h.uri, COUNT(h))
        FROM EndpointHit h
        WHERE h.timestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h) DESC
        """)
    List<ViewStatsResponse> findStatsByDate(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT NEW ru.practicum.explorewithme.stats.ViewStatsResponse(h.app, h.uri, COUNT(DISTINCT h.ip))
        FROM EndpointHit h
        WHERE h.timestamp BETWEEN :start AND :end
          AND h.uri IN :uris
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h) DESC
        """)
    List<ViewStatsResponse> findUniqueStatsByDateAndUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    @Query("""
        SELECT NEW ru.practicum.explorewithme.stats.ViewStatsResponse(h.app, h.uri, COUNT(DISTINCT h.ip))
        FROM EndpointHit h
        WHERE h.timestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h) DESC
        """)
    List<ViewStatsResponse> findUniqueStatsByDate(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
