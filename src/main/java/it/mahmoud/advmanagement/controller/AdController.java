package it.mahmoud.advmanagement.controller;

import it.mahmoud.advmanagement.dto.ad.*;
import it.mahmoud.advmanagement.dto.response.ApiResponseDTO;
import it.mahmoud.advmanagement.dto.response.PageMetaDTO;
import it.mahmoud.advmanagement.service.AdService;
import it.mahmoud.advmanagement.util.AdStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Ad operations
 */
@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    @Autowired
    public AdController(AdService adService) {
        this.adService = adService;
    }

    /**
     * Create a new advertisement
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<AdDTO>> createAd( @RequestBody AdCreateDTO adCreateDTO) {
        AdDTO createdAd = adService.createAd(adCreateDTO);
        return new ResponseEntity<>(
                ApiResponseDTO.success(createdAd, "Advertisement created successfully"),
                HttpStatus.CREATED);
    }

    /**
     * Update an existing advertisement
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AdDTO>> updateAd(
            @PathVariable Long id,
             @RequestBody AdUpdateDTO adUpdateDTO) {
        AdDTO updatedAd = adService.updateAd(id, adUpdateDTO);
        return ResponseEntity.ok(
                ApiResponseDTO.success(updatedAd, "Advertisement updated successfully"));
    }

    /**
     * Get advertisement by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AdDTO>> getAdById(@PathVariable Long id) {
        // Increment views on GET
        adService.incrementViews(id);
        AdDTO ad = adService.getAdById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(ad, "Advertisement retrieved successfully"));
    }

    /**
     * Delete advertisement (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Advertisement deleted successfully"));
    }

    /**
     * Get all advertisements (paginated)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getAllAds(Pageable pageable) {
        Page<AdDTO> ads = adService.getAllAds(pageable);
        // Create page metadata
        PageMetaDTO pageMeta = PageMetaDTO.builder()
                .pageNumber(ads.getNumber())
                .pageSize(ads.getSize())
                .totalElements(ads.getTotalElements())
                .totalPages(ads.getTotalPages())
                .first(ads.isFirst())
                .last(ads.isLast())
                .build();
        return ResponseEntity.ok(ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Get summary of all advertisements (paginated)
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDTO<Page<AdSummaryDTO>>> getAdsSummary(Pageable pageable) {
        Page<AdSummaryDTO> adSummaries = adService.getAdsSummary(pageable);
        PageMetaDTO pageMeta = PageMetaDTO.builder()
                .pageNumber(adSummaries.getNumber())
                .pageSize(adSummaries.getSize())
                .totalElements(adSummaries.getTotalElements())
                .totalPages(adSummaries.getTotalPages())
                .first(adSummaries.isFirst())
                .last(adSummaries.isLast())
                .build();
        return ResponseEntity.ok(
                ApiResponseDTO.success(adSummaries, pageMeta));
    }

    /**
     * Get advertisements by status (paginated)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getAdsByStatus(
            @PathVariable AdStatus status,
            Pageable pageable) {
        Page<AdDTO> ads = adService.getAdsByStatus(status, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Get advertisements by creator (paginated)
     */
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getAdsByCreator(
            @PathVariable Long creatorId,
            Pageable pageable) {
        Page<AdDTO> ads = adService.getAdsByCreator(creatorId, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Get advertisements by category (paginated)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getAdsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        Page<AdDTO> ads = adService.getAdsByCategory(categoryId, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Get advertisements by tag (paginated)
     */
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getAdsByTag(
            @PathVariable Long tagId,
            Pageable pageable) {
        Page<AdDTO> ads = adService.getAdsByTag(tagId, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Full-text search for advertisements (paginated)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> searchAds(
            @RequestParam String query,
            Pageable pageable) {
        Page<AdDTO> ads = adService.searchAds(query, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Advanced search for advertisements (paginated)
     */
    @PostMapping("/advanced-search")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> advancedSearch(
            @RequestBody AdSearchDTO searchDTO,
            Pageable pageable) {
        Page<AdDTO> ads = adService.advancedSearch(searchDTO, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Update advertisement status
     */
    @PutMapping("/status")
    public ResponseEntity<ApiResponseDTO<AdDTO>> updateAdStatus(
             @RequestBody AdStatusUpdateDTO statusUpdateDTO) {
        AdDTO updatedAd = adService.updateAdStatus(statusUpdateDTO);
        return ResponseEntity.ok(
                ApiResponseDTO.success(updatedAd, "Advertisement status updated successfully"));
    }

    /**
     * Publish advertisement
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponseDTO<AdDTO>> publishAd(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expirationDate) {
        AdDTO publishedAd = adService.publishAd(id, expirationDate);
        return ResponseEntity.ok(
                ApiResponseDTO.success(publishedAd, "Advertisement published successfully"));
    }

    /**
     * Get advertisements expiring soon
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponseDTO<List<AdDTO>>> getAdsExpiringSoon(
            @RequestParam(defaultValue = "7") int days) {
        List<AdDTO> ads = adService.findAdsExpiringSoon(days);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, "Expiring advertisements retrieved successfully"));
    }

    /**
     * Get featured advertisements (paginated)
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getFeaturedAds(Pageable pageable) {
        Page<AdDTO> ads = adService.getFeaturedAds(pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Get recently published advertisements (paginated)
     */
    @GetMapping("/recently-published")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getRecentlyPublishedAds(
            @RequestParam(defaultValue = "7") int days,
            Pageable pageable) {
        Page<AdDTO> ads = adService.getRecentlyPublishedAds(days, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Get advertisements by price range (paginated)
     */
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getAdsByPriceRange(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        Page<AdDTO> ads = adService.getAdsByPriceRange(minPrice, maxPrice, pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Get most viewed advertisements (paginated)
     */
    @GetMapping("/most-viewed")
    public ResponseEntity<ApiResponseDTO<Page<AdDTO>>> getMostViewedAds(Pageable pageable) {
        Page<AdDTO> ads = adService.getMostViewedAds(pageable);
        PageMetaDTO pageMeta = createPageMeta(ads);
        return ResponseEntity.ok(
                ApiResponseDTO.success(ads, pageMeta));
    }

    /**
     * Mark expired advertisements
     */
    @PostMapping("/mark-expired")
    public ResponseEntity<ApiResponseDTO<Integer>> markExpiredAds() {
        int count = adService.markExpiredAds();
        return ResponseEntity.ok(
                ApiResponseDTO.success(count, count + " advertisements marked as expired"));
    }

    /**
     * Helper method to create PageMetaDTO from a Page object
     */
    private <T> PageMetaDTO createPageMeta(Page<T> page) {
        return PageMetaDTO.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}