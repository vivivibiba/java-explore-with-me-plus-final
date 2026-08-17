package ru.practicum.explorewithme.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class UpdateRequestStatusDto {
    List<Long> requestIds;
    @NotNull
    RequestStatus status;
}
