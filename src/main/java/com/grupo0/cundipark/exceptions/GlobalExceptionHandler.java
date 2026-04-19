package com.grupo0.cundipark.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.grupo0.cundipark.dtos.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.dao.DataAccessException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        String message = ex.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateResource(DuplicateResourceException ex) {
       String message = ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException ex) {
        String message = ex.getMessage();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
       Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errores.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Errores de validación", errores));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<?>> handleBadRequestExceptions(RuntimeException ex) {
        logger.warn("Solicitud incorrecta: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ApiResponse<?>> handleJpaSystemException(JpaSystemException ex) {
        logger.error("Error de sistema JPA: ", ex);
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error de persistencia: " + rootCause, null));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleDataAccessException(DataAccessException ex) {
        logger.error("Error de acceso a datos: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error de base de datos: " + ex.getMostSpecificCause().getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        String message = "Error interno del servidor: " + ex.getMessage();
        logger.error("Unhandled exception occurred: {}", message, ex); // Log the full stack trace
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message, null));
    }
}
