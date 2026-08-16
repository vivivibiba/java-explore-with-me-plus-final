package ru.practicum.explorewithme.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class UpdateCommentDto {
    @NotBlank
    private String text;
}
