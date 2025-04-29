package it.mahmoud.advmanagement.dto.ad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Simplified DTO for displaying ads in listings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdSummaryDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private LocalDateTime publicationDate;
    private Integer views;
    private Boolean featured;
    private String creatorName; // Combined first and last name
    private Set<String> categoryNames;
}
