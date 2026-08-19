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
@Table(name = "user_actions")
@IdClass(UserActionId.class)
@Getter
@Setter
@NoArgsConstructor
public class UserActionEntity {
    @Id
    @Column(name = "user_id")
    private long userId;

    @Id
    @Column(name = "event_id")
    private long eventId;

    @Column(nullable = false)
    private double weight;

    @Column(name = "last_action_at", nullable = false)
    private Instant lastActionAt;
}
