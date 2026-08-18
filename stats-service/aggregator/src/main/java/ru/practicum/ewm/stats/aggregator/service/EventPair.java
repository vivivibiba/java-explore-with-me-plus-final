package ru.practicum.ewm.stats.aggregator.service;

public record EventPair(long eventA, long eventB) {
    public EventPair {
        if (eventA >= eventB) {
            throw new IllegalArgumentException("eventA must be less than eventB");
        }
    }

    public static EventPair of(long first, long second) {
        return first < second ? new EventPair(first, second) : new EventPair(second, first);
    }
}
