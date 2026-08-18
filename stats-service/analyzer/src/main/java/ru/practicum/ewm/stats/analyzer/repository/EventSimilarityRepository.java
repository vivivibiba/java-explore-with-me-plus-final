package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityId;

import java.util.List;
import java.util.Set;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarityEntity, EventSimilarityId> {
    @Query("select similarity from EventSimilarityEntity similarity " +
            "where similarity.eventA = :eventId or similarity.eventB = :eventId")
    List<EventSimilarityEntity> findForEvent(@Param("eventId") long eventId);

    @Query("select similarity from EventSimilarityEntity similarity " +
            "where similarity.eventA in :eventIds or similarity.eventB in :eventIds")
    List<EventSimilarityEntity> findForAnyEvent(@Param("eventIds") Set<Long> eventIds);
}
