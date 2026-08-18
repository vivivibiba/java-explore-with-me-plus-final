package ru.practicum.ewm.stats.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_similarities")
@IdClass(EventSimilarityId.class)
@Getter
@Setter
@NoArgsConstructor
public class EventSimilarityEntity {
    @Id
    @Column(name = "event_a")
    private long eventA;

    @Id
    @Column(name = "event_b")
    private long eventB;

    @Column(nullable = false)
    private double score;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
