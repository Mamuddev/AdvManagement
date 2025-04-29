package it.mahmoud.advmanagement.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Tag entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDTO {

    private Long id;

    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "Tag can only contain letters, numbers, hyphens, and underscores")
    private String name;

    // Statistics
    private Integer adsCount;
}