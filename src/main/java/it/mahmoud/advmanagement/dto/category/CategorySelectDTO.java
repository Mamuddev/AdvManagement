package it.mahmoud.advmanagement.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified DTO for Category selection in forms
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySelectDTO {
    private Long id;
    private String name;
    private Long parentId;
    private Boolean hasSubcategories;
}
