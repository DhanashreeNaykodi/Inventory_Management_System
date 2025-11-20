package com.example.inventory_factory_management.exceptions;


import com.example.inventory_factory_management.dto.BaseResponseDTO;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionClass {

    // Validation errors for @RequestParam, @ValidImage, etc.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .iterator()
                .next()
                .getMessage();

        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "VALIDATION_FAILED",
                message,
                null,
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<BaseResponseDTO> handleUnexpectedTypeException(UnexpectedTypeException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "VALIDATION_ANNOTATION_MISCONFIGURED",
                "Validation failed",
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid input");

        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "VALIDATION_ERROR",
                errorMessage,
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<BaseResponseDTO> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "USER_ALREADY_PRESENT",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "USER_NOT_FOUND",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<BaseResponseDTO> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "UNAUTHORIZED_ACCESS",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<BaseResponseDTO> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<BaseResponseDTO> handleOperationNotPermittedException(OperationNotPermittedException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "OPERATION_NOT_PERMITTED",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle Built-in Spring Exceptions
//    @ExceptionHandler(DataAccessException.class)
//    public ResponseEntity<BaseResponseDTO> handleDataAccessException(DataAccessException ex) {
//        BaseResponseDTO error = new BaseResponseDTO(
//                false,
//                "DATABASE_ERROR",
//                "A database error occurred",
//                null,
//                LocalDateTime.now().toString()
//        );
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "INVALID_INPUT",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponseDTO> handleIllegalStateException(IllegalStateException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "ILLEGAL_OPERATION",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<BaseResponseDTO> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "RESOURCE_ALREADY_EXISTS",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        errorResponse.put("error", HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
        errorResponse.put("message", "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint. Supported methods: " +
                (ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods() : "None"));
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<BaseResponseDTO<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
//
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//                errors.put(error.getField(), error.getDefaultMessage())
//        );
//
//        return ResponseEntity.badRequest()
//                .body(BaseResponseDTO.error("Validation failed: " + errors));
//    }
//
//    public ResponseEntity<Map<String, Object>> errorResponse(String message, HttpStatus status) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
//        response.put("message", message);
//        response.put("status", status.value());
//
//        return new ResponseEntity<>(response, status);
//    }
//
////    @ExceptionHandler(InvalidActionException.class)
////    public ResponseEntity<BaseResponseDTO<?>> handleInvalidActonExceptions(InvalidActionException ex) {
////
////        return errorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
////    }
//
//    //  handle other exceptions too
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<BaseResponseDTO<?>> handleAllExceptions(Exception ex) {
//        return ResponseEntity.badRequest()
//                .body(BaseResponseDTO.error("Error: " + ex.getMessage()));
//    }


}
