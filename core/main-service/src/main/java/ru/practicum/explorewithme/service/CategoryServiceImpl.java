package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotEmptyCategoryException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceBase implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto body) {
        log.trace("Инициировано сохранение категории. Тело запроса: {}", body);
        checkNameUniqueness(body.getName());
        Category category = CategoryMapper.toCategory(body);
        log.debug("Тело запроса {} преобразовано в категорию {}", body, category);
        Category result = categoryRepository.save(category);
        log.debug("Категория {} сохранена", body);
        return CategoryMapper.toCategoryDto(result);
    }

    private void checkNameUniqueness(String name) {
        if (categoryRepository.existsByName(name)) {
            log.info("Категория с именем '{}' уже существует", name);
            throw new DuplicatedDataException(Entities.CATEGORY.name(), "name", name);
        }
    }

    @Override
    public void deleteCategory(long catId) {
        log.trace("Инициировано удаление категории с id={}", catId);
        checkEntityExistsIn(categoryRepository, catId, Entities.CATEGORY);
        checkCategoryEventsExistsBy(catId);
        categoryRepository.deleteById(catId);
        log.debug("Категория с id={} удалена", catId);
    }

    private void checkCategoryEventsExistsBy(long id) {
        if (eventRepository.existsByCategoryId(id)) {
            log.info("Категория с id={} содержит события", id);
            throw new NotEmptyCategoryException(id);
        }

    }

    @Override
    public CategoryDto updateCategory(long catId, UpdateCategoryDto body) {
        log.trace("Инициировано сохранение категории с id={}. Тело запроса: {}", catId, body);
        Category category = findEntityIn(categoryRepository, catId, Entities.CATEGORY);
        log.trace("Категория найдена: {}", category);
        if (category.getName().equals(body.getName())) {
            log.trace("Имя категории не изменилось");
            return CategoryMapper.toCategoryDto(category);
        }
        checkNameUniqueness(body.getName());
        Category update = CategoryMapper.toCategory(category, body);
        log.debug("Создано обновление категории: {}", update);
        Category result = categoryRepository.save(update);
        log.debug("Обновление {} сохранено", update);
        return CategoryMapper.toCategoryDto(result);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.trace("Инициировано получение категорий с параметрами from={} и size={}", from, size);
        List<Category> result = categoryRepository.findWithOffset(from, size);
        log.debug("Найдено {} категорий", result.size());
        return result.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(long catId) {
        log.trace("Инициировано получение категории с id={}", catId);
        Category result = findEntityIn(categoryRepository, catId, Entities.CATEGORY);
        log.debug("Найдена категория {}", result);
        return CategoryMapper.toCategoryDto(result);
    }
}
