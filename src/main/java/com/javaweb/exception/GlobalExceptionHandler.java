package com.javaweb.exception;

import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<Map<String, String>> handleLazyInitializationException(LazyInitializationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Database session error");
        error.put("message", "Failed to load related data");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<Map<String, String>> handleClassCastException(ClassCastException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication error");
        error.put("message", "Invalid user session");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Access Denied");
        error.put("message", "You don't have permission to access this resource");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
}