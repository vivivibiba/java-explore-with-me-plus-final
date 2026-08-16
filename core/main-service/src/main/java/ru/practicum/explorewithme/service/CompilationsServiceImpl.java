package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.entity.Compilation;
import ru.practicum.explorewithme.entity.CompilationEvent;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.repository.CompilationRepository;
import ru.practicum.explorewithme.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationsServiceImpl extends ServiceBase implements CompilationsService {
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto body) {
        log.trace("Инициировано сохранение подборки. Тело запроса: {}", body);
        List<Event> events = findEvents(body.getEvents());
        log.trace("Найдено {} событий", events.size());
        Compilation compilation = CompilationMapper.toCompilation(body);
        linkEvents(compilation, events);
        Compilation result = compilationRepository.save(compilation);
        log.debug("Подборка {} сохранена", compilation);
        return composeComplicationResponse(result);
    }

    private List<Event> findEvents(List<Long> eventIds) {
        if (eventIds == null) {
            return List.of();
        }
        return eventRepository.findByIdIn(eventIds);
    }

    private void linkEvents(Compilation compilation, List<Event> events) {
        events.forEach(event -> {
                    CompilationEvent compilationEvent = CompilationEvent.builder()
                            .compilation(compilation)
                            .event(event)
                            .build();
                    compilation.getEvents().add(compilationEvent);
                }
        );
    }

    private CompilationDto composeComplicationResponse(Compilation compilation) {
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);
        List<Event> events = compilation.getEvents().stream()
                .map(CompilationEvent::getEvent)
                .toList();
        if (events.isEmpty()) {
            return compilationDto;
        }
        List<EventDto> eventWithStats = getEventsWithStats(events, statsClient);
        List<EventShortDto> shortEvents = eventWithStats.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        compilationDto.setEvents(shortEvents);
        return compilationDto;
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        log.trace("Инициировано удаление подборки с id={}", compId);
        checkEntityExistsIn(compilationRepository, compId, Entities.COMPILATION);
        compilationRepository.deleteById(compId);
        log.debug("Подборка с id={} удалена", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest body) {
        log.trace("Инициировано обновление сборки с id={}. Тело запроса: {}", compId, body);
        Compilation compilation = findEntityIn(compilationRepository, compId, Entities.COMPILATION);
        log.trace("Подборка найдена: {}", compilation);
        Compilation update = updateCompilationData(compilation, body);
        log.debug("Создано обновление подборки: {}", update);
        Compilation result = compilationRepository.save(update);
        log.debug("Обновление {} сохранено", update);
        return composeComplicationResponse(result);
    }

    private Compilation updateCompilationData(Compilation compilation, UpdateCompilationRequest update) {
        List<Long> eventIdsUpdate = update.getEvents();
        if (eventIdsUpdate != null) {
            List<Event> events = findEvents(eventIdsUpdate);
            linkEvents(compilation, events);
        }
        Boolean pinnedUpdate = update.getPinned();
        if (pinnedUpdate != null) {
            compilation.setPinned(pinnedUpdate);
        }
        String titleUpdate = update.getTitle();
        if (titleUpdate != null) {
            compilation.setTitle(titleUpdate);
        }
        return compilation;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.trace("Инициировано получение подборок с параметрами pinned={}, from={} и size={}", pinned, from, size);
        List<Compilation> result = compilationRepository.findWithOffset(pinned, from, size);
        log.debug("Найдено {} подборок", result.size());
        return result.stream()
                .map(this::composeComplicationResponse)
                .toList();
    }

    @Override
    public CompilationDto getCompilation(long compId) {
        log.trace("Инициировано получение подборки с id={}", compId);
        Compilation result = findEntityIn(compilationRepository, compId, Entities.COMPILATION);
        log.debug("Найдена подборка {}", result);
        return composeComplicationResponse(result);
    }
}
