package ru.practicum.explorewithme.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.dto.event.EventStatus;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    boolean existsByCategoryId(long categoryId);

    boolean existsByIdAndInitiatorId(long eventId, long initiatorId);

    List<Event> findByInitiatorId(long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(long eventId, long initiatorId);

    Optional<Event> findById(Long eventId);

    Optional<Event> findByIdAndStatus(long eventId, EventStatus status);

    List<Event> findByIdIn(List<Long> eventIds);
}
