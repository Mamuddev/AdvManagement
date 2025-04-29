package it.mahmoud.advmanagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Codici di errore standardizzati per l'API
 */
public enum ApiErrorCode {
    // Errori generici
    INTERNAL_ERROR("GEN-001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("GEN-002", "Validation error", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("GEN-003", "Unauthorized access", HttpStatus.FORBIDDEN),

    // Errori utente (USER)
    USER_NOT_FOUND("USER-001", "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER-002", "User already exists", HttpStatus.CONFLICT),
    USER_INVALID_CREDENTIALS("USER-003", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    USER_PASSWORD_MISMATCH("USER-004", "Passwords do not match", HttpStatus.BAD_REQUEST),

    // Errori annuncio (AD)
    AD_NOT_FOUND("AD-001", "Ad not found", HttpStatus.NOT_FOUND),
    AD_INVALID_STATUS("AD-002", "Invalid ad status transition", HttpStatus.BAD_REQUEST),
    AD_EXPIRED("AD-003", "Ad has expired", HttpStatus.BAD_REQUEST),
    AD_PERMISSION_DENIED("AD-004", "No permission to modify this ad", HttpStatus.FORBIDDEN),

    // Errori categoria (CAT)
    CATEGORY_NOT_FOUND("CAT-001", "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS("CAT-002", "Category already exists", HttpStatus.CONFLICT),
    CATEGORY_CIRCULAR_REFERENCE("CAT-003", "Circular category reference detected", HttpStatus.BAD_REQUEST),
    CATEGORY_HAS_SUBCATEGORIES("CAT-004", "Category has subcategories and cannot be deleted", HttpStatus.CONFLICT),
    CATEGORY_HAS_ADS("CAT-005", "Category has ads and cannot be deleted", HttpStatus.CONFLICT),

    // Errori tag (TAG)
    TAG_NOT_FOUND("TAG-001", "Tag not found", HttpStatus.NOT_FOUND),
    TAG_ALREADY_EXISTS("TAG-002", "Tag already exists", HttpStatus.CONFLICT),
    TAG_HAS_ADS("TAG-003", "Tag has ads and cannot be deleted", HttpStatus.CONFLICT),
    TAG_INVALID_NAME("TAG-004", "Invalid tag name format", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ApiErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}