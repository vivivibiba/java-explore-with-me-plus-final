package ru.practicum.explorewithme.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class CommentDto {
    private long id;
    private String text;
    private LocalDateTime created;
    private LocalDateTime updated;
    private long eventId;
    private long authorId;

    @JsonProperty("state")
    private CommentStatus status;

    private Long moderatorId;
    private LocalDateTime moderated;
    private String moderationReason;
}
