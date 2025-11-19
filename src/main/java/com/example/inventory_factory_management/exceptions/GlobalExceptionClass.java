package com.example.inventory_factory_management.exceptions;


import com.example.inventory_factory_management.dto.BaseResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionClass {


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        BaseResponseDTO error = new BaseResponseDTO(
                false,
                "USER_NOT_FOUND",
                ex.getMessage(),
                null,
                LocalDateTime.now().toString()
//                HttpStatus.NOT_FOUND.value()
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

    // Handle Validation Exceptions
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<BaseResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        String errorMessage = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(error -> error.getField() + ": " + error.getDefaultMessage())
//                .collect(Collectors.joining(", "));
//
//        BaseResponseDTO error = new BaseResponseDTO(
//                false,
//                "VALIDATION_ERROR",
//                ex.getMessage(),
//                null,
//                LocalDateTime.now().toString()
//        );
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }

    // Handle all other exceptions
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<BaseResponseDTO> handleGenericException(Exception ex) {
//        BaseResponseDTO error = new BaseResponseDTO(
//                false,
//                "INTERNAL_SERVER_ERROR",
//                "An unexpected error occurred",
//                null,
//                LocalDateTime.now().toString()
//        );
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
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
