package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.entity.EndpointHit;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.repository.EndpointHitRepository;
import ru.practicum.explorewithme.service.mapper.EndpointHitMapper;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {
    private final EndpointHitMapper endpointHitMapper;
    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitRequest request) {
        log.trace("Инициировано сохранение статичстических данных {}", request);
        EndpointHit endpointHit = endpointHitMapper.toEndpointHit(request);
        log.debug("Статистические данные {} преобразованы в {}", request, endpointHit);
        EndpointHit savedHit = endpointHitRepository.save(endpointHit);
        log.debug("Статистические данные {} сохранены в базе данных", savedHit);
    }
}
