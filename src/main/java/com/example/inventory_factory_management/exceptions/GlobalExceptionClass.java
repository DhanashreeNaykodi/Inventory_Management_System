package com.example.inventory_factory_management.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionClass {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();

        // Get field-specific errors
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        errors.put("message", "Validation failed");
//        errors.put("timestamp", LocalDateTime.now());
        errors.put("errors", errorMessages); // This will show specific field errors
        errors.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Map<String, Object>> errorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
        response.put("message", message);
        response.put("status", status.value());

        return new ResponseEntity<>(response, status);
    }

    // for checking right role - if user is not allowed to access other role endpoint (authorization)
    // not working for me...
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
//        Map<String, Object> errors = new HashMap<>();
//        errors.put("timestamp", LocalDateTime.now());
//        errors.put("message", "Access Denied ");
//        errors.put("status", HttpStatus.FORBIDDEN.value());
//        return new ResponseEntity<>(errors, HttpStatus.FORBIDDEN);
//    }
//
//
//    @ExceptionHandler(EmailAlreadyExistsException.class)
//    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
//        return errorResponse(ex.getMessage(), HttpStatus.CONFLICT);
//    }
//
//    @ExceptionHandler(PostNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handlePostNotFound(PostNotFoundException ex) {
//        return errorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(InvalidPasswordException.class)
//    public ResponseEntity<Map<String, Object>> handleInvalidPassword(InvalidPasswordException ex) {
//        return errorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Map<String, Object>> handleIllegalArgument() {
//        return errorResponse("Invalid status parameter", HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
//        return errorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(CommentNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handleCommentNotFound(CommentNotFoundException ex) {
//        return errorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(UnauthorizedActionException.class)
//    public ResponseEntity<Map<String, Object>> handleUnauthorizedActionException(UnauthorizedActionException ex) {
//        return errorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(InvalidCommentPostAssociationException.class)
//    public ResponseEntity<Map<String, Object>> handleInvalidCommentPostAssociationException(InvalidCommentPostAssociationException ex) {
//        return errorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return errorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


}
