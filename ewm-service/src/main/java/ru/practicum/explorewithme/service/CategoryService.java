package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto body);

    void deleteCategory(long catId);

    CategoryDto updateCategory(long catId, UpdateCategoryDto body);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(long catId);
}
