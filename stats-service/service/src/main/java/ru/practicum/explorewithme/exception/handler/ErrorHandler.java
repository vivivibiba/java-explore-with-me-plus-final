package ru.practicum.explorewithme.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explorewithme.exception.WrongDateIntervalException;

import java.time.LocalDateTime;

@RestControllerAdvice
@SuppressWarnings("unused")
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> unexpected(Throwable e) {
        return createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SERVICE: unexpected error",
                e.getMessage());
    }

    private ResponseEntity<ErrorResponse> createResponse(HttpStatus status, String reason, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.toString(),
                reason,
                message,
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> methodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getFieldError();
        if (fieldError == null) {
            return unexpected(e);
        }
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "incorrect field value",
                String.format(
                        "value of field '%s'=%s is incorrect, cause: %s",
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> wrongDateInterval(WrongDateIntervalException e) {
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "wrong date interval",
                e.getMessage()
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> missingServletRequestParameter(MissingServletRequestParameterException e) {
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "missing request parameter",
                e.getBody().getDetail()
        );
    }
}
