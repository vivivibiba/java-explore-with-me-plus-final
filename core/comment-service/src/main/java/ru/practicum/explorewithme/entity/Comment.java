package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explorewithme.dto.comment.CommentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments", schema = "public")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "text_on_moderation")
    private String textOnModeration;
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Column(name = "updated")
    private LocalDateTime updated;
    @Column(name = "event_id", nullable = false)
    private long eventId;
    @Column(name = "author_id", nullable = false)
    private long authorId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CommentStatus status;
    @Column(name = "moderator_id")
    private Long moderatorId;
    @Column(name = "moderated")
    private LocalDateTime moderated;
}
