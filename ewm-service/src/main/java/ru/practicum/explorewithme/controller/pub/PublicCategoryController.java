package ru.practicum.explorewithme.controller.pub;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.service.CategoryService;

import java.util.List;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = URL_CATEGORIES)
@RequiredArgsConstructor
@Validated
@SuppressWarnings("unused")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(name = PARAM_FROM, required = false, defaultValue = "0")
            @PositiveOrZero int from,
            @RequestParam(name = PARAM_SIZE, required = false, defaultValue = "10")
            @Positive int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.getCategories(from, size));
    }

    @GetMapping("/{" + ID_CATEGORY + "}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable(name = ID_CATEGORY) long catId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.getCategory(catId));
    }
}
