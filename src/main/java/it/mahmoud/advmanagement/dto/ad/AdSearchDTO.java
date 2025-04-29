package it.mahmoud.advmanagement.dto.ad;

import it.mahmoud.advmanagement.util.AdStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO for search filters when querying ads
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdSearchDTO {
    private String query; // Text search in title and description
    private Long categoryId;
    private Set<Long> tagIds;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private AdStatus status;
    private Boolean featured;
    private Long creatorId;

    // Pagination and sorting parameters
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}