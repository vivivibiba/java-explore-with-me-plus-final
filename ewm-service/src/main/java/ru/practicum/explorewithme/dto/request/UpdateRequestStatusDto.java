package ru.practicum.explorewithme.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class UpdateRequestStatusDto {
    List<Long> requestIds;
    @NotNull
    RequestStatus status;
}
