package it.mahmoud.advmanagement.exception;

import it.mahmoud.advmanagement.dto.response.ApiResponseDTO;
import it.mahmoud.advmanagement.dto.response.ValidationErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Exception Handler
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handler for ApiException (e tutte le sottoclassi)
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleApiException(ApiException ex, WebRequest request) {
        // Log dell'errore (più dettagliato per errori server)
        if (ex.getStatus().is5xxServerError()) {
            logger.error("Server error: {}", ex.getMessage(), ex);
        } else {
            logger.warn("Client error: {} - {}", ex.getCode(), ex.getMessage());
        }

        ApiResponseDTO<Object> response = ApiResponseDTO.error(ex.getMessage());

        response.setErrorCode(ex.getCode());
        if (ex.getDetails() != null) {
            response.setErrorDetails(ex.getDetails());
        }

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     *  handler for validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        BindingResult result = ex.getBindingResult();
        List<ValidationErrorDTO> validationErrors = new ArrayList<>();

        for (FieldError fieldError : result.getFieldErrors()) {
            ValidationErrorDTO error = ValidationErrorDTO.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
            validationErrors.add(error);
        }

        ApiResponseDTO<Object> response = ApiResponseDTO.validationError(
                "Validation failed", validationErrors);

        response.setErrorCode(ApiErrorCode.VALIDATION_ERROR.getCode());

        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.getStatus())
                .body(response);
    }

    /**
     * Fallback for unmanaged Exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleGenericException(Exception ex, WebRequest request) {
        // Log dettagliato dell'errore
        logger.error("Unhandled exception", ex);

        ApiResponseDTO<Object> response = ApiResponseDTO.error(
                "An unexpected error occurred. Please try again later.");

        // Imposta il codice errore API per errore interno
        response.setErrorCode(ApiErrorCode.INTERNAL_ERROR.getCode());

        return ResponseEntity.status(ApiErrorCode.INTERNAL_ERROR.getStatus())
                .body(response);
    }
}