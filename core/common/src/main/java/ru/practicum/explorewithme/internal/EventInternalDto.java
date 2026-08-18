package ru.practicum.explorewithme.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.dto.event.EventStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInternalDto {
    private long id;
    private long initiatorId;
    private int confirmedRequests;
    private int participantLimit;
    private boolean requestModeration;
    private EventStatus status;
}
