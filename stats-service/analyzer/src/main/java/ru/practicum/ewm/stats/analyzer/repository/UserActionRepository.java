package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.UserActionEntity;
import ru.practicum.ewm.stats.analyzer.model.UserActionId;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserActionEntity, UserActionId> {
    List<UserActionEntity> findByUserIdOrderByLastActionAtDesc(long userId);

    @Query("select coalesce(sum(action.weight), 0.0) from UserActionEntity action where action.eventId = :eventId")
    Double sumWeightsByEventId(@Param("eventId") long eventId);
}
