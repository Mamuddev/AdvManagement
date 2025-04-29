package it.mahmoud.advmanagement.dto.tag;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO for bulk operations on tags
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagBulkDTO {

    @NotEmpty(message = "At least one tag is required")
    @Builder.Default
    private Set<String> tagNames = new HashSet<>();
}
