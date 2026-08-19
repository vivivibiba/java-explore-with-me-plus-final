package ru.practicum.ewm.stats.analyzer.model;

import java.io.Serializable;
import java.util.Objects;

public class UserActionId implements Serializable {
    private long userId;
    private long eventId;

    public UserActionId() {
    }

    public UserActionId(long userId, long eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserActionId that)) {
            return false;
        }
        return userId == that.userId && eventId == that.eventId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, eventId);
    }
}
