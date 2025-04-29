package it.mahmoud.advmanagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Eccezione base per tutti gli errori API
 */
public class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;
    private final String details;

    public ApiException(ApiErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public ApiException(ApiErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public ApiException(ApiErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    public String getDetails() {
        return details;
    }
}