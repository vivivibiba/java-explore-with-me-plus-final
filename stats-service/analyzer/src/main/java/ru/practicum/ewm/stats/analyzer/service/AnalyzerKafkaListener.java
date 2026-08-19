package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
public class AnalyzerKafkaListener {
    private final UserActionStorageService userActionStorageService;
    private final SimilarityStorageService similarityStorageService;

    @KafkaListener(
            topics = "${analyzer.kafka.user-actions-topic}",
            containerFactory = "analyzerUserActionKafkaListenerContainerFactory"
    )
    public void consumeUserAction(UserActionAvro action) {
        userActionStorageService.store(action);
    }

    @KafkaListener(
            topics = "${analyzer.kafka.events-similarity-topic}",
            containerFactory = "analyzerSimilarityKafkaListenerContainerFactory"
    )
    public void consumeSimilarity(EventSimilarityAvro similarity) {
        similarityStorageService.store(similarity);
    }
}
