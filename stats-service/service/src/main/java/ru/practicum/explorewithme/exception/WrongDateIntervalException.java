package ru.practicum.explorewithme.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WrongDateIntervalException extends RuntimeException {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public WrongDateIntervalException(LocalDateTime start, LocalDateTime end) {
        super(String.format("start='%s' must be before end='%s'", start, end));
        this.start = start;
        this.end = end;
    }
}
