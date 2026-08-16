package ru.practicum.explorewithme.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;

import java.util.List;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
public class TestBase {
    protected <M> void assertMethodCall(M mock, Consumer<M> methodCall) {
        methodCall.accept(Mockito.verify(
                mock,
                Mockito.times(1))
        );
    }

    protected <M> void assertMethodNotCall(M mock, Consumer<M> methodCall) {
        methodCall.accept(Mockito.verify(
                mock,
                Mockito.never())
        );
    }

    protected NewCategoryDto buildNewCategoryDto() {
        return NewCategoryDto.builder()
                .name("Category Name")
                .build();
    }

    protected UpdateCategoryDto buildUpdateCategoryDto() {
        return UpdateCategoryDto.builder()
                .name("Category Name Update")
                .build();
    }

    protected NewCompilationDto buildNewCompilationDto() {
        return NewCompilationDto.builder()
                .pinned(false)
                .title("Compilation Title")
                .build();
    }

    protected NewCompilationDto buildNewCompilationDto(List<Long> eventIds) {
        return NewCompilationDto.builder()
                .events(eventIds)
                .pinned(false)
                .title("Compilation Title")
                .build();
    }

    protected UpdateCompilationRequest buildUpdateCompilationRequest(List<Long> eventIds) {
        return UpdateCompilationRequest.builder()
                .events(eventIds)
                .pinned(true)
                .title("Compilation Title Update")
                .build();
    }
}
