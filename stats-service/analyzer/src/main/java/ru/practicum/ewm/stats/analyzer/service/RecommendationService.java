package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.stats.analyzer.model.UserActionEntity;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.repository.UserActionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {
    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository similarityRepository;

    @Value("${analyzer.recommendations.neighbors:10}")
    private int neighborsLimit;

    public List<Recommendation> getSimilarEvents(long eventId, long userId, int maxResults) {
        if (maxResults <= 0) {
            return List.of();
        }

        Set<Long> interactedEvents = getInteractedEventIds(userId);
        Map<Long, Double> candidates = new HashMap<>();
        for (EventSimilarityEntity similarity : similarityRepository.findForEvent(eventId)) {
            long candidate = otherEvent(similarity, eventId);
            if (!interactedEvents.contains(candidate)) {
                candidates.merge(candidate, similarity.getScore(), Math::max);
            }
        }

        return candidates.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(maxResults)
                .map(entry -> new Recommendation(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<Recommendation> getRecommendationsForUser(long userId, int maxResults) {
        if (maxResults <= 0) {
            return List.of();
        }

        List<UserActionEntity> interactions = userActionRepository.findByUserIdOrderByLastActionAtDesc(userId);
        if (interactions.isEmpty()) {
            return List.of();
        }

        Set<Long> interactedIds = new HashSet<>();
        Map<Long, Double> userWeights = new HashMap<>();
        for (UserActionEntity interaction : interactions) {
            interactedIds.add(interaction.getEventId());
            userWeights.put(interaction.getEventId(), interaction.getWeight());
        }

        Set<Long> recentIds = interactions.stream()
                .limit(maxResults)
                .map(UserActionEntity::getEventId)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        List<EventSimilarityEntity> similarities = similarityRepository.findForAnyEvent(recentIds);
        Map<Long, Double> candidateScores = new HashMap<>();
        for (EventSimilarityEntity similarity : similarities) {
            boolean aIsRecent = recentIds.contains(similarity.getEventA());
            boolean bIsRecent = recentIds.contains(similarity.getEventB());

            if (aIsRecent && !interactedIds.contains(similarity.getEventB())) {
                candidateScores.merge(similarity.getEventB(), similarity.getScore(), Math::max);
            }
            if (bIsRecent && !interactedIds.contains(similarity.getEventA())) {
                candidateScores.merge(similarity.getEventA(), similarity.getScore(), Math::max);
            }
        }

        List<Long> candidates = candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();

        List<Recommendation> predictions = new ArrayList<>();
        for (Long candidate : candidates) {
            double predicted = predict(candidate, userWeights);
            if (predicted > 0.0) {
                predictions.add(new Recommendation(candidate, predicted));
            }
        }

        return predictions.stream()
                .sorted(Comparator.comparingDouble(Recommendation::score).reversed()
                        .thenComparingLong(Recommendation::eventId))
                .limit(maxResults)
                .toList();
    }

    public List<Recommendation> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Recommendation> unique = new LinkedHashMap<>();
        for (Long eventId : eventIds) {
            if (eventId == null) {
                continue;
            }
            double score = userActionRepository.sumWeightsByEventId(eventId);
            unique.putIfAbsent(eventId, new Recommendation(eventId, score));
        }
        return List.copyOf(unique.values());
    }

    private double predict(long candidate, Map<Long, Double> userWeights) {
        List<Neighbor> neighbors = new ArrayList<>();
        for (EventSimilarityEntity similarity : similarityRepository.findForEvent(candidate)) {
            long neighborId = otherEvent(similarity, candidate);
            Double weight = userWeights.get(neighborId);
            if (weight != null && similarity.getScore() > 0.0) {
                neighbors.add(new Neighbor(weight, similarity.getScore()));
            }
        }

        neighbors.sort(Comparator.comparingDouble(Neighbor::similarity).reversed());
        int limit = Math.min(Math.max(neighborsLimit, 1), neighbors.size());
        double weightedSum = 0.0;
        double similaritySum = 0.0;
        for (int index = 0; index < limit; index++) {
            Neighbor neighbor = neighbors.get(index);
            weightedSum += neighbor.weight() * neighbor.similarity();
            similaritySum += neighbor.similarity();
        }
        return similaritySum == 0.0 ? 0.0 : weightedSum / similaritySum;
    }

    private Set<Long> getInteractedEventIds(long userId) {
        return userActionRepository.findByUserIdOrderByLastActionAtDesc(userId).stream()
                .map(UserActionEntity::getEventId)
                .collect(java.util.stream.Collectors.toSet());
    }

    private long otherEvent(EventSimilarityEntity similarity, long eventId) {
        return similarity.getEventA() == eventId ? similarity.getEventB() : similarity.getEventA();
    }

    private record Neighbor(double weight, double similarity) {
    }
}
