package ru.practicum.explorewithme.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.service.CategoryService;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = ACCESS_ADMIN + URL_CATEGORIES)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody @Valid NewCategoryDto body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(body));
    }

    @DeleteMapping("/{" + ID_CATEGORY + "}")
    public ResponseEntity<Void> deleteCategory(@PathVariable(name = ID_CATEGORY) long catId) {
        categoryService.deleteCategory(catId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(null);
    }

    @PatchMapping("/{" + ID_CATEGORY + "}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable(name = ID_CATEGORY) long catId,
            @RequestBody @Valid UpdateCategoryDto body
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.updateCategory(catId, body));
    }
}
