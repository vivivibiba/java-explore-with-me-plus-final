package ru.practicum.explorewithme.dto.user;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserShortDto {
    private Long id;
    private String name;
}