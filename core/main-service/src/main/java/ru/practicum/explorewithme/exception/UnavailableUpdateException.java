package ru.practicum.explorewithme.exception;

public class UnavailableUpdateException extends RuntimeException {
    public UnavailableUpdateException(String className, long eventId) {
        super(String.format("Update to %s with id %d is unavailable", className, eventId));
    }
}
