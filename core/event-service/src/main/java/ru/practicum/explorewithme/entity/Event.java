package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explorewithme.dto.event.EventStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@ToString
@Table(name = "events", schema = "public")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "conf_req")
    @Builder.Default
    private int confirmedRequests = 0;

    @Column(name = "created", nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "initiator_id", nullable = false)
    private long initiatorId;

    @Column(name = "initiator_name", nullable = false)
    private String initiatorName;

    @Embedded
    private LocationEmbeddable location;

    @Column(nullable = false)
    private boolean paid;

    @Column(name = "participant_limit")
    private int participantLimit;

    @Column(name = "published")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;

    @Column(nullable = false, length = 120)
    private String title;
}
