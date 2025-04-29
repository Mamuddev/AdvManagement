package it.mahmoud.advmanagement.dto.ad;

import it.mahmoud.advmanagement.util.AdStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO specifically for ad status operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdStatusUpdateDTO {

    @NotNull(message = "Ad ID is required")
    private Long id;

    @NotNull(message = "New status is required")
    private AdStatus newStatus;

    // Additional fields for specific status transitions
    private LocalDateTime expirationDate; // Only for PUBLISHED status
    private String rejectionReason; // Only for SUSPENDED status
}
