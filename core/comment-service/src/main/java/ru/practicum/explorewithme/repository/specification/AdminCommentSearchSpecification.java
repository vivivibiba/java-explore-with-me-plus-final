package ru.practicum.explorewithme.repository.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.dto.comment.CommentStatus;
import ru.practicum.explorewithme.entity.Comment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminCommentSearchSpecification implements Specification<Comment> {
    private final String text;
    private final List<Long> authorsIds;
    private final LocalDateTime rangeStart;
    private final LocalDateTime rangeEnd;
    private final List<Long> eventIds;
    private final List<CommentStatus> states;

    public AdminCommentSearchSpecification(String text,
                                           List<Long> authorsIds,
                                           String rangeStart,
                                           String rangeEnd,
                                           List<Long> eventIds,
                                           List<CommentStatus> states) {
        this.text = text;
        this.authorsIds = authorsIds;
        this.rangeStart = parse(rangeStart);
        this.rangeEnd = parse(rangeEnd);
        this.eventIds = eventIds;
        this.states = states;
    }

    private LocalDateTime parse(String value) {
        if (value == null || value.isBlank()) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(value, formatter);
    }

    @Override
    public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (text != null && !text.isEmpty()) {
            Expression<String> textExpr = root.get("text");
            predicates.add(cb.like(textExpr, "%" + text + "%"));
        }

        if (authorsIds != null && !authorsIds.isEmpty()) {
            predicates.add(root.get("authorId").in(authorsIds));
        }

        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("created"), rangeStart));
        }

        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("created"), rangeEnd));
        }

        if (eventIds != null) {
            predicates.add(root.get("eventId").in(eventIds));
        }

        if (states != null && !states.isEmpty()) {
            predicates.add(root.get("status").in(states));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
