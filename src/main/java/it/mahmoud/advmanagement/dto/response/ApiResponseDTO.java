package it.mahmoud.advmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO per la risposta standard dell'API
 * @param <T> Tipo di dati contenuti nella risposta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;

    // Informazioni sull'errore
    private String errorCode;
    private String errorDetails;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Per risposte paginate
    private PageMetaDTO pageMeta;

    // Per errori di validazione
    @Builder.Default
    private List<ValidationErrorDTO> errors = new ArrayList<>();

    /**
     * Crea una risposta di successo con dati
     */
    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una risposta di successo con dati e messaggio personalizzato
     */
    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una risposta di successo paginata
     */
    public static <T> ApiResponseDTO<T> success(T data, PageMetaDTO pageMeta) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .pageMeta(pageMeta)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una risposta di errore
     */
    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una risposta di errore di validazione
     */
    public static <T> ApiResponseDTO<T> validationError(String message, List<ValidationErrorDTO> errors) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}