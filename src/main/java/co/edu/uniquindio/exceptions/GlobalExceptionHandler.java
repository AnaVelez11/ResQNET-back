package co.edu.uniquindio.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import co.edu.uniquindio.dto.UserResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValueConflictException.class)
    public ResponseEntity<ErrorResponse> handleValueConflictException(ValueConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("ERROR", ex.getMessage()));
    }
}

