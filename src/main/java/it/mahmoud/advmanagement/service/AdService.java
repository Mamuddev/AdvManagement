package it.mahmoud.advmanagement.service;

import it.mahmoud.advmanagement.dto.ad.*;
import it.mahmoud.advmanagement.util.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for operations on Ad entities
 */
public interface AdService {

    /**
     * Create a new ad
     * @param adCreateDTO Ad creation data
     * @return Created ad data
     */
    AdDTO createAd(AdCreateDTO adCreateDTO);

    /**
     * Update an existing ad
     * @param id Ad ID
     * @param adUpdateDTO Ad update data
     * @return Updated ad data
     */
    AdDTO updateAd(Long id, AdUpdateDTO adUpdateDTO);

    /**
     * Get ad by ID
     * @param id Ad ID
     * @return Ad data
     */
    AdDTO getAdById(Long id);

    /**
     * Delete an ad
     * @param id Ad ID
     */
    void deleteAd(Long id);

    /**
     * Get all ads with pagination
     * @param pageable Pagination information
     * @return Page of ads
     */
    Page<AdDTO> getAllAds(Pageable pageable);

    /**
     * Get ads by status
     * @param status Ad status
     * @param pageable Pagination information
     * @return Page of ads with the given status
     */
    Page<AdDTO> getAdsByStatus(AdStatus status, Pageable pageable);

    /**
     * Get ads by creator
     * @param creatorId Creator ID
     * @param pageable Pagination information
     * @return Page of ads by the given creator
     */
    Page<AdDTO> getAdsByCreator(Long creatorId, Pageable pageable);

    /**
     * Get ads by category
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Page of ads in the given category
     */
    Page<AdDTO> getAdsByCategory(Long categoryId, Pageable pageable);

    /**
     * Get ads by tag
     * @param tagId Tag ID
     * @param pageable Pagination information
     * @return Page of ads with the given tag
     */
    Page<AdDTO> getAdsByTag(Long tagId, Pageable pageable);

    /**
     * Full text search in ad title and description
     * @param query Search query
     * @param pageable Pagination information
     * @return Page of matching ads
     */
    Page<AdDTO> searchAds(String query, Pageable pageable);

    /**
     * Advanced search with multiple criteria
     * @param searchDTO Search criteria
     * @param pageable Pagination information
     * @return Page of ads matching the criteria
     */
    Page<AdDTO> advancedSearch(AdSearchDTO searchDTO, Pageable pageable);

    /**
     * Update ad status
     * @param statusUpdateDTO Status update data
     * @return Updated ad data
     */
    AdDTO updateAdStatus(AdStatusUpdateDTO statusUpdateDTO);

    /**
     * Increment ad views count
     * @param adId Ad ID
     */
    void incrementViews(Long adId);

    /**
     * Publish an ad
     * @param adId Ad ID
     * @param expirationDate Expiration date
     * @return Published ad data
     */
    AdDTO publishAd(Long adId, LocalDateTime expirationDate);

    /**
     * Find ads expiring soon
     * @param days Number of days to look ahead
     * @return List of ads expiring within the given number of days
     */
    List<AdDTO> findAdsExpiringSoon(int days);

    /**
     * Find featured ads
     * @param pageable Pagination information
     * @return Page of featured ads
     */
    Page<AdDTO> getFeaturedAds(Pageable pageable);

    /**
     * Find recently published ads
     * @param days Number of days to look back
     * @param pageable Pagination information
     * @return Page of recently published ads
     */
    Page<AdDTO> getRecentlyPublishedAds(int days, Pageable pageable);

    /**
     * Find ads by price range
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @param pageable Pagination information
     * @return Page of ads within the price range
     */
    Page<AdDTO> getAdsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Get most viewed ads
     * @param pageable Pagination information
     * @return Page of most viewed ads
     */
    Page<AdDTO> getMostViewedAds(Pageable pageable);

    /**
     * Get summary of ads for listing
     * @param pageable Pagination information
     * @return Page of ad summaries
     */
    Page<AdSummaryDTO> getAdsSummary(Pageable pageable);

    /**
     * Schedule a job to mark expired ads
     * @return Number of ads marked as expired
     */
    int markExpiredAds();
}