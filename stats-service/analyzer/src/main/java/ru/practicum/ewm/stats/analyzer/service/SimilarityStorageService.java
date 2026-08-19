package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarityId;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
public class SimilarityStorageService {
    private final EventSimilarityRepository repository;

    @Transactional
    public void store(EventSimilarityAvro message) {
        long first = Math.min(message.getEventA(), message.getEventB());
        long second = Math.max(message.getEventA(), message.getEventB());
        EventSimilarityId id = new EventSimilarityId(first, second);
        EventSimilarityEntity entity = repository.findById(id).orElseGet(EventSimilarityEntity::new);

        if (entity.getUpdatedAt() != null && message.getTimestamp().isBefore(entity.getUpdatedAt())) {
            return;
        }

        entity.setEventA(first);
        entity.setEventB(second);
        entity.setScore(message.getScore());
        entity.setUpdatedAt(message.getTimestamp());
        repository.save(entity);
    }
}
