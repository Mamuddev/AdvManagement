package it.mahmoud.advmanagement.exception;

import it.mahmoud.advmanagement.dto.response.ApiResponseDTO;
import it.mahmoud.advmanagement.dto.response.ValidationErrorDTO;
import org.springframework.http.HttpStatus;
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
 * Global exception handler for the application
 * Provides consistent error responses across all controllers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.<Void>error(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle DuplicateResourceException and UserAlreadyExistsException
     */
    @ExceptionHandler({DuplicateResourceException.class, UserAlreadyExistsException.class})
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.<Void>error(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle InvalidInputException
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidInputException(
            InvalidInputException ex, WebRequest request) {

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.<Void>error(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.<Void>error(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleValidationExceptions(
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

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.validationError(
                "Validation failed", validationErrors);

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.<Void>error(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.<Void>error(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    /**
     * Catch-all handler for any other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGlobalException(
            Exception ex, WebRequest request) {

        // Log the error for debugging (would use proper logging in a real application)
        ex.printStackTrace();

        ApiResponseDTO<Void> apiResponse = ApiResponseDTO.<Void>error(
                "An unexpected error occurred. Please try again later.");

        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}