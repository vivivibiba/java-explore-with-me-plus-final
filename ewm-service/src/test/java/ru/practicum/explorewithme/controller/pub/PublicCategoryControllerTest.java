package ru.practicum.explorewithme.controller.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.explorewithme.exception.handler.ErrorHandler;
import ru.practicum.explorewithme.service.CategoryService;
import ru.practicum.explorewithme.test.ControllerTest;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static ru.practicum.explorewithme.controller.ControllerConstants.*;

public class PublicCategoryControllerTest extends ControllerTest {
    @InjectMocks
    private PublicCategoryController publicCategoryController;
    @Mock
    private CategoryService categoryService;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(publicCategoryController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    public void getCategories_StatusOK() {
        // Arrange
        int from = 0;
        int size = 10;

        // Act
        ResultActions result = performGet(createGetCategoriesUrl(from, size));

        // Assert
        expectStatusOk(result);
        assertMethodCall(categoryService, service ->
                service.getCategories(Mockito.eq(from), Mockito.eq(size)));
    }

    @Test
    public void getCategory_StatusOK() {
        // Arrange
        long categoryId = 1L;

        // Act
        ResultActions result = performGet(createIdUrl(URL_CATEGORIES, categoryId));

        // Assert
        expectStatusOk(result);
        assertMethodCall(categoryService, service ->
                service.getCategory(Mockito.eq(categoryId)));
    }

    private String createGetCategoriesUrl(int from, int size) {
        return String.format("%s?%s=%d&%s=%d",
                URL_CATEGORIES,
                PARAM_FROM, from,
                PARAM_SIZE, size);
    }
}
