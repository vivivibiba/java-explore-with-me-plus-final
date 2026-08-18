package ru.practicum.explorewithme.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class ChangedRequestStatusesDto {
    List<RequestDto> confirmedRequests;
    List<RequestDto> rejectedRequests;
}
