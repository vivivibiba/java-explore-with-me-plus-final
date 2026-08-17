package ru.practicum.explorewithme.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.dto.event.EventStatus;
import ru.practicum.explorewithme.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public class UsersEventSearchSpecifications {
    private UsersEventSearchSpecifications() {
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Event> textContains(String text) {
        return (root, query, builder) -> {
            if (text == null || text.isBlank()) {
                return builder.conjunction();
            }

            String pattern = "%" + text.toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("annotation")), pattern),
                    builder.like(builder.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> categoryIn(List<Long> categories) {
        return (root, query, builder) -> {
            if (categories == null || categories.isEmpty()) {
                return builder.conjunction();
            }
            return root.get("category").get("id").in(categories);
        };
    }

    public static Specification<Event> paidEquals(Boolean paid) {
        return (root, query, builder) -> paid == null ? builder.conjunction() : builder.equal(root.get("paid"), paid);
    }

    public static Specification<Event> eventDateFrom(LocalDateTime rangeStart) {
        return (root, query, builder) -> rangeStart == null
                ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.<LocalDateTime>get("eventDate"), rangeStart);
    }

    public static Specification<Event> eventDateTo(LocalDateTime rangeEnd) {
        return (root, query, builder) -> rangeEnd == null
                ? builder.conjunction()
                : builder.lessThanOrEqualTo(root.<LocalDateTime>get("eventDate"), rangeEnd);
    }

    public static Specification<Event> onlyAvailable(boolean onlyAvailable) {
        return (root, query, builder) -> {
            if (!onlyAvailable) {
                return builder.conjunction();
            }

            return builder.or(
                    builder.equal(root.get("participantLimit"), 0),
                    builder.lessThan(root.<Integer>get("confirmedRequests"), root.<Integer>get("participantLimit"))
            );
        };
    }
}
