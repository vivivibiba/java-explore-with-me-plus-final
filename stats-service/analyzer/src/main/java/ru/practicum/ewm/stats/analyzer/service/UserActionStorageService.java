package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.UserActionEntity;
import ru.practicum.ewm.stats.analyzer.model.UserActionId;
import ru.practicum.ewm.stats.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class UserActionStorageService {
    private final UserActionRepository repository;

    @Transactional
    public void store(UserActionAvro action) {
        UserActionId id = new UserActionId(action.getUserId(), action.getEventId());
        UserActionEntity entity = repository.findById(id).orElseGet(UserActionEntity::new);
        entity.setUserId(action.getUserId());
        entity.setEventId(action.getEventId());
        entity.setWeight(Math.max(entity.getWeight(), weightOf(action.getActionType())));
        if (entity.getLastActionAt() == null || action.getTimestamp().isAfter(entity.getLastActionAt())) {
            entity.setLastActionAt(action.getTimestamp());
        }
        repository.save(entity);
    }

    private double weightOf(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
