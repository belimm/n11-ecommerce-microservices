package com.n11bc.cart_service.exception;

import com.n11bc.cart_service.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CartNotFoundException.class, CartItemNotFoundException.class, ProductUnavailableException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        return buildResponse(ex, request, HttpStatus.NOT_FOUND, "Not Found", null);
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCartOperation(InvalidCartOperationException ex, WebRequest request) {
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST, "Bad Request", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildResponse(ex, request, HttpStatus.FORBIDDEN, "Forbidden", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            validationErrors.put(fieldName, error.getDefaultMessage());
        });
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST, "Bad Request", validationErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, WebRequest request) {
        return buildResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            Exception ex,
            WebRequest request,
            HttpStatus status,
            String error,
            Map<String, String> validationErrors
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                validationErrors
        );
        return ResponseEntity.status(status).body(response);
    }
}
