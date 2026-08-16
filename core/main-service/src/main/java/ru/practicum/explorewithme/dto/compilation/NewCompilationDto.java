package ru.practicum.explorewithme.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class NewCompilationDto {
    private List<Long> events;
    private boolean pinned;
    @NotBlank(message = "'title' field cannot be empty.")
    @Size(min = 1, max = 50, message = "incorrect title string length")
    private String title;
}
