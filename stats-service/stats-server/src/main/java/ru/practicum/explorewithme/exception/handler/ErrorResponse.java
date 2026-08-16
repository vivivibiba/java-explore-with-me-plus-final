package ru.practicum.explorewithme.exception.handler;

import java.time.LocalDateTime;

public record ErrorResponse(String status, String reason, String message, LocalDateTime timestamp) {
}
