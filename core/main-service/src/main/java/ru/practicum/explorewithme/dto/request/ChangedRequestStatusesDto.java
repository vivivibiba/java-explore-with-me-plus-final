package ru.practicum.explorewithme.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class ChangedRequestStatusesDto {
    List<RequestDto> confirmedRequests;
    List<RequestDto> rejectedRequests;
}
