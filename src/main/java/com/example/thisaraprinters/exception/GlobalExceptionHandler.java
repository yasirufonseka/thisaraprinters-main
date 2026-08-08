package com.example.thisaraprinters.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("message", "File size exceeds the maximum allowed limit!"));
    }

    public ResponseEntity<Map<String,String>> handleIlegalArgument(IllegalArgumentException exc) {
        System.out.println(exc.getMessage());
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(Map.of("message", exc.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            errorMessage.append(error.getDefaultMessage()).append(". ");
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage.toString().trim()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception exc) {
        System.out.println(exc.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", exc.getMessage()));

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException exc) {
        System.out.println(exc.getMessage());
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", exc.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException exc) {
        System.out.println(exc.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Data integrity violation: " + exc.getMostSpecificCause().getMessage()));
    }

}
