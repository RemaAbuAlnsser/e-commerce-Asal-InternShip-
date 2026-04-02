package com.asal.ecommerce.exception;

import com.asal.ecommerce.dto.AdminLoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AdminLoginResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        String errorMessage = "Validation failed: " + errors.toString();
        AdminLoginResponse response = new AdminLoginResponse(false, errorMessage, null);
        
        System.out.println("Validation error: " + errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AdminLoginResponse> handleGenericException(Exception ex) {
        String errorMessage = "An error occurred: " + ex.getMessage();
        AdminLoginResponse response = new AdminLoginResponse(false, errorMessage, null);
        
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
