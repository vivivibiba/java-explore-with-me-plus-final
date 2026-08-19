package ru.practicum.ewm.stats.aggregator.service;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SimilarityCalculator {
    private final Map<Long, Map<Long, Double>> weightsByEvent = new HashMap<>();
    private final Map<Long, Double> eventWeightSums = new HashMap<>();
    private final Map<EventPair, Double> minWeightSums = new HashMap<>();

    public synchronized List<EventSimilarityAvro> update(UserActionAvro action) {
        double incomingWeight = weightOf(action.getActionType());
        long eventId = action.getEventId();
        long userId = action.getUserId();

        Map<Long, Double> eventWeights = weightsByEvent.computeIfAbsent(eventId, ignored -> new HashMap<>());
        double oldWeight = eventWeights.getOrDefault(userId, 0.0);
        if (incomingWeight <= oldWeight) {
            return List.of();
        }

        double deltaWeight = incomingWeight - oldWeight;
        eventWeights.put(userId, incomingWeight);
        eventWeightSums.merge(eventId, deltaWeight, Double::sum);

        List<EventSimilarityAvro> results = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : weightsByEvent.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) {
                continue;
            }

            Double otherWeight = entry.getValue().get(userId);
            if (otherWeight == null) {
                continue;
            }

            EventPair pair = EventPair.of(eventId, otherEventId);
            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(incomingWeight, otherWeight);
            double minSum = minWeightSums.getOrDefault(pair, 0.0) + (newMin - oldMin);
            minWeightSums.put(pair, minSum);

            double denominator = Math.sqrt(eventWeightSums.getOrDefault(pair.eventA(), 0.0))
                    * Math.sqrt(eventWeightSums.getOrDefault(pair.eventB(), 0.0));
            double score = denominator == 0.0 ? 0.0 : minSum / denominator;

            EventSimilarityAvro similarity = new EventSimilarityAvro();
            similarity.setEventA(pair.eventA());
            similarity.setEventB(pair.eventB());
            similarity.setScore(score);
            similarity.setTimestamp(action.getTimestamp());
            results.add(similarity);
        }

        return results;
    }

    private double weightOf(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
