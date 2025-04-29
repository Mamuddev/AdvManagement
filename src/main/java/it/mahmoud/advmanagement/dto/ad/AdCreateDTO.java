package it.mahmoud.advmanagement.dto.ad;

import it.mahmoud.advmanagement.util.AdStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO for creating a new ad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdCreateDTO {

    @NotBlank(message = "Ad title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Ad description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expirationDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price can have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    private Boolean featured;
    private AdStatus status; // Often set to DRAFT by default

    @NotNull(message = "Creator ID is required")
    private Long creatorId;

    @Builder.Default
    private Set<Long> categoryIds = new HashSet<>();

    @Builder.Default
    private Set<Long> tagIds = new HashSet<>();
}