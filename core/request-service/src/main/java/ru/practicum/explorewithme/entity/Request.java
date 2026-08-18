package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explorewithme.dto.request.RequestStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "requests", schema = "public")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "event_id", nullable = false)
    private long eventId;

    @Column(name = "requester_id", nullable = false)
    private long requesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;
}
