package com.example.inventory_factory_management.exceptions;


import com.example.inventory_factory_management.dto.BaseResponseDTO;
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
    public ResponseEntity<BaseResponseDTO<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest()
                .body(BaseResponseDTO.error("Validation failed: " + errors));
    }

    //  handle other exceptions too
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponseDTO<?>> handleAllExceptions(Exception ex) {
        return ResponseEntity.badRequest()
                .body(BaseResponseDTO.error("Error: " + ex.getMessage()));
    }


}
