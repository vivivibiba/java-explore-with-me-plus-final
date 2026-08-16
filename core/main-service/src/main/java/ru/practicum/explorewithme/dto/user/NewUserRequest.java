package ru.practicum.explorewithme.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class NewUserRequest {
    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    private String email;
    @NotBlank
    @Size(min = 2, max = 250)
    private String name;
}
