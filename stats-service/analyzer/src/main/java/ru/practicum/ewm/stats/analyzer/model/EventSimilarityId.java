package ru.practicum.ewm.stats.analyzer.model;

import java.io.Serializable;
import java.util.Objects;

public class EventSimilarityId implements Serializable {
    private long eventA;
    private long eventB;

    public EventSimilarityId() {
    }

    public EventSimilarityId(long eventA, long eventB) {
        this.eventA = eventA;
        this.eventB = eventB;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EventSimilarityId that)) {
            return false;
        }
        return eventA == that.eventA && eventB == that.eventB;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventA, eventB);
    }
}
