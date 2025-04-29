package it.mahmoud.advmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for validation errors
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorDTO {
    private String field;
    private String message;
}
