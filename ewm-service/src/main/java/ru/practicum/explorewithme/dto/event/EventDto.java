package ru.practicum.explorewithme.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.dto.category.CategoryDto;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class EventDto {
    private long id;
    private String annotation;
    @Setter
    private CategoryDto category;
    private int confirmedRequests;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @Setter
    private UserShortDto initiator;
    @Setter
    private Location location;
    private boolean paid;
    private int participantLimit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private boolean requestModeration;
    @JsonProperty("state")
    private EventStatus status;
    private String title;
    @Setter
    private long views;
}
