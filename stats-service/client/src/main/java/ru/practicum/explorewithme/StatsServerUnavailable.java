package ru.practicum.explorewithme;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String message, Throwable cause) {
        super(message, cause);
    }

    public StatsServerUnavailable(String message) {
        super(message);
    }
}
