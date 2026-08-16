package ru.practicum.explorewithme.hit;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class EndpointHitRequest {
    @NotBlank(message = "'app' field cannot be empty.")
    @Size(max = 512, message = "too long app string")
    private String app;
    @NotBlank(message = "'uri' field cannot be empty.")
    @Size(max = 512, message = "too long uri string")
    private String uri;
    @NotBlank(message = "'ip' field cannot be empty.")
    @Size(max = 512, message = "too long ip string")
    private String ip;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
