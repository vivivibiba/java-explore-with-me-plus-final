package ru.practicum.explorewithme.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.NotEmptyCategoryException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.test.ServiceTest;

import java.util.List;

public class CategoryServiceImplTest extends ServiceTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRepository eventRepository;

    @Test
    public void createCategory_ReturnsObject() {
        // Arrange
        NewCategoryDto body = buildNewCategoryDto();
        long savedId = 1L;
        Category saved = buildCategory(savedId, body);
        whenCategoryNotExistBy(body.getName());
        whenSaveReturns(categoryRepository, saved);

        // Act
        CategoryDto actual = categoryService.createCategory(body);

        // Assert
        CategoryDto expected = buildCategoryDto(saved);
        assertEquals(actual, expected);
    }

    @Test
    public void createCategory_ExistingName_DuplicatedDataException() {
        // Arrange
        NewCategoryDto body = buildNewCategoryDto();
        whenCategoryExistBy(body.getName());

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.createCategory(body));

        // Assert
        assertException(thrown, DuplicatedDataException.class);
    }

    @Test
    public void deleteCategory() {
        // Arrange
        long id = 1L;
        whenEntityExistIn(categoryRepository);
        whenEventsNotExistIn(id);
        doNothingOnDeleteIn(categoryRepository);

        // Act
        categoryService.deleteCategory(id);

        // Assert
        assertMethodCall(categoryRepository, repository -> repository.deleteById(Mockito.eq(id)));
    }

    @Test
    public void deleteCategory_AbsentCategory_NotFoundException() {
        // Arrange
        long absentId = 1L;
        whenEntityAbsentIn(categoryRepository);

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.deleteCategory(absentId));

        // Assert
        assertException(thrown, NotFoundException.class);
    }

    @Test
    public void deleteCategory_CategoryWithEvents_NotEmptyCategoryException() {
        // Arrange
        long id = 1L;
        whenEntityExistIn(categoryRepository);
        whenEventsExistIn(id);

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.deleteCategory(id));

        // Assert
        assertException(thrown, NotEmptyCategoryException.class);
    }

    @Test
    public void updateCategory_ReturnsObject() {
        // Arrange
        long savedId = 1L;
        UpdateCategoryDto body = buildUpdateCategoryDto();
        Category saved = buildCategory(savedId);
        whenEntityFoundIn(categoryRepository, saved);
        whenCategoryNotExistBy(body.getName());
        whenSaveReturns(categoryRepository, saved);

        // Act
        CategoryDto actual = categoryService.updateCategory(savedId, body);

        // Assert
        CategoryDto expected = buildCategoryDto(saved);
        assertEquals(actual, expected);
    }

    @Test
    public void updateCategory_NameNotChange_ReturnsObject() {
        // Arrange
        long savedId = 1L;
        UpdateCategoryDto body = buildUpdateCategoryDto();
        String existingName = body.getName();
        Category saved = buildCategory(savedId, existingName);
        whenEntityFoundIn(categoryRepository, saved);

        // Act
        CategoryDto actual = categoryService.updateCategory(savedId, body);

        // Assert
        CategoryDto expected = buildCategoryDto(saved);
        assertEquals(actual, expected);
        assertMethodNotCall(categoryRepository, repository ->
                repository.save(Mockito.any(Category.class)));
    }

    @Test
    public void updateCategory_AbsentCategory_NotFoundException() {
        // Arrange
        long absentId = 1L;
        UpdateCategoryDto body = buildUpdateCategoryDto();
        whenEntityNotFoundIn(categoryRepository);

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.updateCategory(absentId, body));

        // Assert
        assertException(thrown, NotFoundException.class);
    }

    @Test
    public void updateCategory_ExistingName_DuplicatedDataException() {
        // Arrange
        long savedId = 1L;
        UpdateCategoryDto body = buildUpdateCategoryDto();
        Category saved = buildCategory(savedId);
        whenEntityFoundIn(categoryRepository, saved);
        whenCategoryExistBy(body.getName());

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.updateCategory(savedId, body));

        // Assert
        assertException(thrown, DuplicatedDataException.class);
    }

    @Test
    public void getCategories_ReturnsArray() {
        // Arrange
        int from = 0;
        int size = 10;
        List<Category> categories = List.of(
                buildCategory(1),
                buildCategory(2)
        );
        whenFindWithOffsetReturns(categories);

        // Act
        List<CategoryDto> actual = categoryService.getCategories(from, size);

        // Assert
        List<CategoryDto> expected = categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
        assertEquals(actual, expected);
    }

    @Test
    public void getCategory_ReturnsObject() {
        // Arrange
        long categoryId = 1L;
        Category category = buildCategory(categoryId);
        whenEntityFoundIn(categoryRepository, category);

        // Act
        CategoryDto actual = categoryService.getCategory(categoryId);

        // Assert
        CategoryDto expected = buildCategoryDto(category);
        assertEquals(actual, expected);
    }

    @Test
    public void getCategory_AbsentCategory_NotFoundException() {
        // Arrange
        long absentId = 1L;
        whenEntityNotFoundIn(categoryRepository);

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.getCategory(absentId));

        // Assert
        assertException(thrown, NotFoundException.class);
    }

    private void whenCategoryExistBy(String name) {
        Mockito.when(categoryRepository.existsByName(Mockito.eq(name)))
                .thenReturn(true);
    }

    private void whenCategoryNotExistBy(String name) {
        Mockito.when(categoryRepository.existsByName(Mockito.eq(name)))
                .thenReturn(false);
    }

    private void whenEventsExistIn(long categoryId) {
        Mockito.when(eventRepository.existsByCategoryId(Mockito.eq(categoryId)))
                .thenReturn(true);
    }

    private void whenEventsNotExistIn(long categoryId) {
        Mockito.when(eventRepository.existsByCategoryId(Mockito.eq(categoryId)))
                .thenReturn(false);
    }

    private void whenFindWithOffsetReturns(List<Category> categories) {
        Mockito.when(categoryRepository.findWithOffset(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(categories);
    }

    private CategoryDto buildCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    private Category buildCategory(long id, String name) {
        return Category.builder()
                .id(id)
                .name(name)
                .build();
    }

    private Category buildCategory(long id) {
        return buildCategory(id, "Category Name");
    }

    private Category buildCategory(long id, NewCategoryDto newCategoryDto) {
        return buildCategory(id, newCategoryDto.getName());
    }
}
