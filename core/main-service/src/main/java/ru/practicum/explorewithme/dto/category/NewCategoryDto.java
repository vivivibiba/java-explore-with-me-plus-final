package ru.practicum.explorewithme.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class NewCategoryDto {
    @NotBlank(message = "'name' field cannot be empty.")
    @Size(max = 50, message = "too long name string")
    private String name;
}
