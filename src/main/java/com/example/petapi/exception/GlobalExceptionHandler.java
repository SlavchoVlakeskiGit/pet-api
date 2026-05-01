package com.example.petapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<PetApiError> handleNotFound(PetNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new PetApiError(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PetApiError> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PetApiError(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PetApiError> handleValidationErrors(MethodArgumentNotValidException exception) {
        List<String> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PetApiError("Validation failed", validationErrors));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<PetApiError> handleConflict(ObjectOptimisticLockingFailureException exception) {
        log.warn("Optimistic locking conflict: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new PetApiError("Pet was modified by another request. Please retry."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PetApiError> handleUnexpected(Exception exception) {
        log.error("Unexpected error occurred", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new PetApiError("An unexpected error occurred"));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PetApiError(String message, List<String> errors) {
        public PetApiError(String message) {
            this(message, null);
        }
    }
}
