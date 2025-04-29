package it.mahmoud.advmanagement.repo;

import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.Category;
import it.mahmoud.advmanagement.model.Tag;
import it.mahmoud.advmanagement.model.User;
import it.mahmoud.advmanagement.util.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Ad entity operations
 * Uses Spring Data JPA to simplify data access and manipulation
 */
@Repository
public interface AdRepository extends JpaRepository<Ad, Long>, JpaSpecificationExecutor<Ad> {

    /**
     * Find an ad by ID and status
     * @param id Ad ID
     * @param status Ad status
     * @return Optional containing the ad if found
     */
    Optional<Ad> findByIdAndStatus(Long id, AdStatus status);

    /**
     * Find ads by status
     * @param status Ad status
     * @param pageable Pagination information
     * @return Page of ads with the given status
     */
    Page<Ad> findByStatus(AdStatus status, Pageable pageable);

    /**
     * Find ads by creator
     * @param creator User who created the ads
     * @param pageable Pagination information
     * @return Page of ads by the given creator
     */
    Page<Ad> findByCreator(User creator, Pageable pageable);

    /**
     * Find ads by creator ID
     * @param creatorId ID of the user who created the ads
     * @param pageable Pagination information
     * @return Page of ads by the given creator
     */
    @Query("SELECT a FROM Ad a WHERE a.creator.id = :creatorId")
    Page<Ad> findByCreatorId(@Param("creatorId") Long creatorId, Pageable pageable);

    /**
     * Find ads by category
     * @param category Category of the ads
     * @param pageable Pagination information
     * @return Page of ads in the given category
     */
    Page<Ad> findByCategories(Category category, Pageable pageable);

    /**
     * Find ads by category ID
     * @param categoryId ID of the category
     * @param pageable Pagination information
     * @return Page of ads in the given category
     */
    @Query("SELECT a FROM Ad a JOIN a.categories c WHERE c.id = :categoryId")
    Page<Ad> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Find ads by tag
     * @param tag Tag of the ads
     * @param pageable Pagination information
     * @return Page of ads with the given tag
     */
    Page<Ad> findByTags(Tag tag, Pageable pageable);

    /**
     * Find ads by tag ID
     * @param tagId ID of the tag
     * @param pageable Pagination information
     * @return Page of ads with the given tag
     */
    @Query("SELECT a FROM Ad a JOIN a.tags t WHERE t.id = :tagId")
    Page<Ad> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    /**
     * Find ads by status and tag
     * @param status Ad status
     * @param tag Tag of the ads
     * @param pageable Pagination information
     * @return Page of ads with the given status and tag
     */
    Page<Ad> findByStatusAndTags(AdStatus status, Tag tag, Pageable pageable);

    /**
     * Find ads by price range
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @param pageable Pagination information
     * @return Page of ads within the price range
     */
    Page<Ad> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find active ads (published and not expired)
     * @param now Current date and time
     * @param pageable Pagination information
     * @return Page of active ads
     */
    @Query("SELECT a FROM Ad a WHERE a.status = 'PUBLISHED' AND " +
            "(a.expirationDate IS NULL OR a.expirationDate > :now)")
    Page<Ad> findActiveAds(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find ads with full-text search in title and description
     * @param query Search query
     * @param pageable Pagination information
     * @return Page of matching ads
     */
    @Query("SELECT a FROM Ad a WHERE " +
            "a.status = 'PUBLISHED' AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Ad> fullTextSearch(@Param("query") String query, Pageable pageable);

    /**
     * Advanced search with multiple criteria
     * @param status Ad status (default is PUBLISHED)
     * @param categoryId Category ID (optional)
     * @param minPrice Minimum price (optional)
     * @param maxPrice Maximum price (optional)
     * @param query Search query (optional)
     * @param featured Featured ads only (optional)
     * @param pageable Pagination information
     * @return Page of ads matching the criteria
     */
    @Query("SELECT DISTINCT a FROM Ad a LEFT JOIN a.categories c WHERE " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:categoryId IS NULL OR c.id = :categoryId) AND " +
            "(:minPrice IS NULL OR a.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR a.price <= :maxPrice) AND " +
            "(:query IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:featured IS NULL OR a.featured = :featured)")
    Page<Ad> advancedSearch(
            @Param("status") AdStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("query") String query,
            @Param("featured") Boolean featured,
            Pageable pageable);

    /**
     * Find recently published ads
     * @param startDate Number of days to look back
     * @param pageable Pagination information
     * @return Page of recent ads
     */
    @Query("SELECT a FROM Ad a WHERE a.status = 'PUBLISHED' AND " +
            "a.publicationDate >= :startDate")
    Page<Ad> findRecentlyPublished(
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable);

    /**
     * Find featured ads
     * @param status Ad status (typically PUBLISHED)
     * @param pageable Pagination information
     * @return Page of featured ads
     */
    Page<Ad> findByFeaturedTrueAndStatus(AdStatus status, Pageable pageable);

    /**
     * Find ads expiring soon
     * @param status Ad status (typically PUBLISHED)
     * @param startDate Start of expiration range
     * @param endDate End of expiration range
     * @return List of ads expiring soon
     */
    List<Ad> findByStatusAndExpirationDateBetween(
            AdStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Count ads by status
     * @param status Ad status
     * @return Number of ads with the given status
     */
    long countByStatus(AdStatus status);

    /**
     * Count ads by user
     * @param creatorId User ID
     * @return Number of ads created by the user
     */
    @Query("SELECT COUNT(a) FROM Ad a WHERE a.creator.id = :creatorId")
    long countByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * Increment ad views
     * @param adId Ad ID
     * @return Number of updated records (should be 1)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Ad a SET a.views = a.views + 1 WHERE a.id = :adId")
    int incrementViews(@Param("adId") Long adId);

    /**
     * Update ad status
     * @param adId Ad ID
     * @param status New status
     * @return Number of updated records (should be 1)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Ad a SET a.status = :status WHERE a.id = :adId")
    int updateStatus(@Param("adId") Long adId, @Param("status") AdStatus status);

    /**
     * Mark expired ads as EXPIRED
     * @param now Current date and time
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Ad a SET a.status = 'EXPIRED' " +
            "WHERE a.status = 'PUBLISHED' AND a.expirationDate < :now")
    int updateExpiredAds(@Param("now") LocalDateTime now);

    /**
     * Get most viewed ads
     * @param status Ad status (typically PUBLISHED)
     * @param pageable Pagination information
     * @return Page of most viewed ads
     */
    Page<Ad> findByStatusOrderByViewsDesc(AdStatus status, Pageable pageable);

    /**
     * Find ads by multiple categories
     * @param categoryIds List of category IDs
     * @param pageable Pagination information
     * @return Page of ads in any of the given categories
     */
    @Query("SELECT DISTINCT a FROM Ad a JOIN a.categories c WHERE c.id IN :categoryIds")
    Page<Ad> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    /**
     * Find ads by multiple tags
     * @param tagIds List of tag IDs
     * @param pageable Pagination information
     * @return Page of ads having any of the given tags
     */
    @Query("SELECT DISTINCT a FROM Ad a JOIN a.tags t WHERE t.id IN :tagIds")
    Page<Ad> findByTagIds(@Param("tagIds") List<Long> tagIds, Pageable pageable);
}