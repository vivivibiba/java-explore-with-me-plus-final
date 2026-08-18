package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationsService {
    CompilationDto createCompilation(NewCompilationDto body);

    void deleteCompilation(long compId);

    CompilationDto updateCompilation(long compId, UpdateCompilationRequest body);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(long compId);
}
