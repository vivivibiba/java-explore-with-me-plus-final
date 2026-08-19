package ru.practicum.ewm.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
public class UserActionListener {
    private final SimilarityCalculator similarityCalculator;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @Value("${aggregator.kafka.events-similarity-topic}")
    private String similarityTopic;

    @KafkaListener(
            topics = "${aggregator.kafka.user-actions-topic}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consume(UserActionAvro action) {
        for (EventSimilarityAvro similarity : similarityCalculator.update(action)) {
            String key = similarity.getEventA() + ":" + similarity.getEventB();
            kafkaTemplate.send(similarityTopic, key, similarity).join();
        }
    }
}
