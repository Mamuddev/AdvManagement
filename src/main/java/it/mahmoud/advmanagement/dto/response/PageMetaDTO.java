package it.mahmoud.advmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for pagination metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageMetaDTO {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}