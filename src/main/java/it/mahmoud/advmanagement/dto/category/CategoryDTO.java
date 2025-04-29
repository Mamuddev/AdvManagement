package it.mahmoud.advmanagement.dto.category;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO for Category entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    // Parent category information
    private Long parentCategoryId;
    private String parentCategoryName;

    // Statistics
    private Integer adsCount;

    // Subcategories (for hierarchical representation)
    @Builder.Default
    private Set<CategoryDTO> subcategories = new HashSet<>();
}
