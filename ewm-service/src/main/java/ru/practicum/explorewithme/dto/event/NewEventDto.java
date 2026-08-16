package ru.practicum.explorewithme.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explorewithme.validation.DateIsNotEarly;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class NewEventDto {
    @NotBlank(message = "'annotation' field cannot be empty.")
    @Size(min = 20, max = 2000, message = "Title must be between 20 and 2000 characters")
    private String annotation;

    @NotNull(message = "'category' field cannot be null.")
    @Positive(message = "'category' must be positive.")
    private Long category;

    @NotBlank(message = "'description' field cannot be empty.")
    @Size(min = 20, max = 7000, message = "Title must be between 20 and 7000 characters")
    private String description;

    @NotNull(message = "'eventDate' field cannot be null.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateIsNotEarly
    private LocalDateTime eventDate;

    @NotNull(message = "'location' field cannot be null.")
    private Location location;

    @Builder.Default
    private boolean paid = false;

    @PositiveOrZero(message = "'participantLimit' must be positive or zero.")
    @Builder.Default
    private int participantLimit = 0;

    private Boolean requestModeration;

    @NotBlank(message = "'title' field cannot be empty.")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;

    public boolean getRequestModerationOrDefault() {
        return this.requestModeration == null ? true : this.requestModeration;
    }
}
