package co.edu.uniquindio.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja conflictos cuando ya existe un valor único en la BD
     * Código HTTP: 409 Conflict
     */
    @ExceptionHandler(ValueConflictException.class)
    public ResponseEntity<ErrorResponse> handleValueConflictException(ValueConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("ERROR", ex.getMessage()));
    }

    /**
     * Maneja errores de validación en los DTOs (@Valid)
     * Código HTTP: 400 Bad Request
     * Retorna mapa con errores por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Extrae los errores de cada campo y los añade al JSON de respuesta
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Maneja errores de autenticación/autorización
     * Código HTTP: 401 Unauthorized
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("ERROR", ex.getMessage()));
    }

    /**
     * Maneja reglas de negocio
     * Código HTTP: 409 Conflict
     * Puede incluir código de error personalizado
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode() != null ? ex.getErrorCode() : "BUSINESS_ERROR",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Estructura estándar para respuestas de error
     * Contiene:
     * - errorCode: Código identificador del error
     * - message: Mensaje descriptivo para el usuario/API
     */
    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private String errorCode;
        private String message;
    }
}
