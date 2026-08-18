package ru.practicum.explorewithme.exception.handler;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explorewithme.exception.*;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@SuppressWarnings("unused")
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ApiError> methodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getFieldError();
        if (fieldError == null) {
            return createErrorResponse("", "incorrect field value", HttpStatus.BAD_REQUEST);
        }

        return createErrorResponse(
                String.format(
                        "value of field '%s'=%s is incorrect, cause: %s",
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()),
                "incorrect field value",
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler
    public ResponseEntity<ApiError> httpMessageNotReadable(HttpMessageNotReadableException e) {
        return createErrorResponse(
                e.getMessage(),
                "incorrect request",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> wrongInterval(WrongDateIntervalException e) {
        return createErrorResponse(e.getMessage(), "incorrect request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> constraintViolation(ConstraintViolationException e) {
        return createErrorResponse(e.getMessage(), "incorrect field value", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> methodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return createErrorResponse(e.getMessage(), "incorrect field value", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> badRequest(BadRequestException e) {
        return createErrorResponse(e.getMessage(), "incorrect request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> duplicatedData(DuplicatedDataException e) {
        return createErrorResponse(e.getMessage(), "duplicated data", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return createErrorResponse("required object was not found", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> notEmptyCategory(NotEmptyCategoryException e) {
        return createErrorResponse("trying delete category with events", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> unavailableEventUpdate(UnavailableUpdateException e) {
        return createErrorResponse(e.getMessage(), "update is unavailable", HttpStatus.CONFLICT);
    }


    @ExceptionHandler
    public ResponseEntity<?> downstreamServiceError(FeignException e) {
        log.warn(
                "Downstream service error: status={}, message={}",
                e.status(),
                e.getMessage()
        );

        if (e.status() >= 400 && e.status() < 600 && !e.contentUTF8().isBlank()) {
            return ResponseEntity.status(e.status())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.contentUTF8());
        }
        return createErrorResponse(
                e.getMessage(),
                "downstream service is unavailable",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> unexpected(RuntimeException e) {
        log.error("Unexpected application error", e);

        return createErrorResponse(
                e.getMessage(),
                "unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> earlyDate(EarlyDateException e) {
        return createErrorResponse(e.getMessage(), "date is early", HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiError> createErrorResponse(String message, String reason, HttpStatus status) {
        if (status.is4xxClientError()) {
            log.warn(
                    "Request error: status={}, reason={}, message={}",
                    status,
                    reason,
                    message
            );
        }

        return ResponseEntity.status(status).body(
                new ApiError(
                        message,
                        reason,
                        status.toString(),
                        LocalDateTime.now()
                )
        );
    }
}
