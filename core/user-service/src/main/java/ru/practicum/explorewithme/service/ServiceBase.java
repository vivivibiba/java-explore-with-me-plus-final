package ru.practicum.explorewithme.service;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;

public class ServiceBase {
    protected <E> E findEntityIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(entity, id));
    }

    protected <E> void checkEntityExistsIn(JpaRepository<E, Long> repository, long id, Entities entity) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(entity, id);
        }
    }
}
