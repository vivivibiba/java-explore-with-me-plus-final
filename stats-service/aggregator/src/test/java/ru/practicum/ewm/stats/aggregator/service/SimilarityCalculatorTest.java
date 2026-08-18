package ru.practicum.ewm.stats.aggregator.service;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimilarityCalculatorTest {
    private final SimilarityCalculator calculator = new SimilarityCalculator();

    @Test
    void shouldUseMaximumActionWeightAndOrderedEventIds() {
        calculator.update(action(1, 20, ActionTypeAvro.VIEW));
        List<EventSimilarityAvro> first = calculator.update(action(1, 10, ActionTypeAvro.REGISTER));

        assertThat(first).hasSize(1);
        assertThat(first.getFirst().getEventA()).isEqualTo(10L);
        assertThat(first.getFirst().getEventB()).isEqualTo(20L);

        List<EventSimilarityAvro> ignored = calculator.update(action(1, 10, ActionTypeAvro.VIEW));
        assertThat(ignored).isEmpty();
    }

    @Test
    void shouldNotRecalculatePairWhenUserDidNotInteractWithOtherEvent() {
        calculator.update(action(1, 1, ActionTypeAvro.VIEW));
        calculator.update(action(2, 2, ActionTypeAvro.VIEW));

        assertThat(calculator.update(action(1, 1, ActionTypeAvro.LIKE))).isEmpty();
    }

    private UserActionAvro action(long userId, long eventId, ActionTypeAvro type) {
        UserActionAvro action = new UserActionAvro();
        action.setUserId(userId);
        action.setEventId(eventId);
        action.setActionType(type);
        action.setTimestamp(Instant.parse("2026-08-18T00:00:00Z"));
        return action;
    }
}
