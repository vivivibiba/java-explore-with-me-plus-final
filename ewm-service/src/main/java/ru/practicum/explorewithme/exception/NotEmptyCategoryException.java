package ru.practicum.explorewithme.exception;

import lombok.Getter;

@Getter
public class NotEmptyCategoryException extends RuntimeException {
    private final long categoryId;

    public NotEmptyCategoryException(long categoryId) {
        super(String.format("category with id='%d' is not empty", categoryId));
        this.categoryId = categoryId;
    }
}

