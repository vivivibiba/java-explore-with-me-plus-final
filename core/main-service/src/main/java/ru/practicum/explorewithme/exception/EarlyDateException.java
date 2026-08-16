package ru.practicum.explorewithme.exception;

import java.time.LocalDateTime;

public class EarlyDateException extends RuntimeException {
    public EarlyDateException(int minOffset, LocalDateTime date) {
        super(String.format("Время начала события должно начинаться не ранее, чем через %d ч. Ваше время %s", minOffset, date));
    }
}