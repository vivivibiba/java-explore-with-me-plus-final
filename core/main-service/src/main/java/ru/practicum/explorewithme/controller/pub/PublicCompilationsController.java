package ru.practicum.explorewithme.controller.pub;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.service.CompilationsService;

import java.util.List;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = URL_COMPILATIONS)
@RequiredArgsConstructor
@Validated
@SuppressWarnings("unused")
public class PublicCompilationsController {

    private final CompilationsService compilationsService;

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(name = PARAM_PINNED, required = false) Boolean pinned,
            @RequestParam(name = PARAM_FROM, required = false, defaultValue = "0")
            @PositiveOrZero int from,
            @RequestParam(name = PARAM_SIZE, required = false, defaultValue = "10")
            @Positive int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationsService.getCompilations(pinned, from, size));
    }

    @GetMapping("/{" + ID_COMPILATION + "}")
    public ResponseEntity<CompilationDto> getCompilation(@PathVariable(name = ID_COMPILATION) long compId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationsService.getCompilation(compId));
    }
}
