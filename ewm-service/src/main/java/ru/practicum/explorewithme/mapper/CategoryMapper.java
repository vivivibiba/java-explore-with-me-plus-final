package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;

public class CategoryMapper {
    private CategoryMapper() {

    }

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public static Category toCategory(Category category, UpdateCategoryDto updateCategoryDto) {
        category.setName(updateCategoryDto.getName());
        return category;
    }


    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
