package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "hits", schema = "public")
public class EndpointHit {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "app", nullable = false, length = 512)
    private String app;
    @Column(name = "uri", nullable = false, length = 512)
    private String uri;
    @Column(name = "ip", nullable = false, length = 512)
    private String ip;
    @Column(name = "timestamp_", nullable = false)
    private LocalDateTime timestamp;
}
