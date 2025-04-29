package it.mahmoud.advmanagement.repo;

import it.mahmoud.advmanagement.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for Tag entity operations
 * Uses Spring Data JPA to simplify data access and manipulation
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

    /**
     * Find a tag by name (case-insensitive)
     * @param name Tag name
     * @return Optional containing the tag if found
     */
    Optional<Tag> findByNameIgnoreCase(String name);

    /**
     * Check if a tag with the given name exists
     * @param name Tag name
     * @return true if a tag with this name exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find tags by partial name match
     * @param searchTerm Term to search for
     * @param sort Sorting preferences
     * @return List of matching tags
     */
    List<Tag> findByNameContainingIgnoreCase(String searchTerm, Sort sort);

    /**
     * Find tags by multiple names
     * @param names Set of tag names
     * @return Set of tags with matching names
     */
    Set<Tag> findByNameInIgnoreCase(Set<String> names);

    /**
     * Find the most frequently used tags
     * @param limit Maximum number of tags to return
     * @return List of the most used tags
     */
    @Query(value = "SELECT t.* FROM tags t " +
            "JOIN ad_tag at ON t.id = at.tag_id " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(at.ad_id) DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Tag> findMostPopularTags(@Param("limit") int limit);

    /**
     * Count ads for each tag
     * @return List of tag IDs and their ad counts
     */
    @Query("SELECT t.id, COUNT(a) FROM Tag t LEFT JOIN t.ads a GROUP BY t.id")
    List<Object[]> countAdsByTag();

    /**
     * Count ads for a specific tag
     * @param tagId Tag ID
     * @return Number of ads with this tag
     */
    @Query("SELECT COUNT(a) FROM Tag t JOIN t.ads a WHERE t.id = :tagId")
    long countAdsByTagId(@Param("tagId") Long tagId);

    /**
     * Find unused tags (no associated ads)
     * @param pageable Pagination information
     * @return Page of unused tags
     */
    @Query("SELECT t FROM Tag t WHERE SIZE(t.ads) = 0")
    Page<Tag> findUnusedTags(Pageable pageable);

    /**
     * Find related tags (tags that often appear together with the given tag)
     * @param tagId Source tag ID
     * @param limit Maximum number of related tags to return
     * @return List of related tag IDs and co-occurrence counts
     */
    @Query(value = "SELECT t2.id, COUNT(*) as count " +
            "FROM ad_tag at1 " +
            "JOIN ad_tag at2 ON at1.ad_id = at2.ad_id AND at2.tag_id != at1.tag_id " +
            "JOIN tags t2 ON t2.id = at2.tag_id " +
            "WHERE at1.tag_id = :tagId " +
            "GROUP BY t2.id " +
            "ORDER BY count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findRelatedTags(@Param("tagId") Long tagId, @Param("limit") int limit);

    /**
     * Find tags for a given ad
     * @param adId Ad ID
     * @return List of tags associated with the ad
     */
    @Query("SELECT t FROM Tag t JOIN t.ads a WHERE a.id = :adId")
    List<Tag> findTagsByAdId(@Param("adId") Long adId);

    /**
     * Find tags by creator (tags used in ads by a specific user)
     * @param creatorId User ID
     * @param pageable Pagination information
     * @return Page of tags used by the user
     */
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.ads a WHERE a.creator.id = :creatorId")
    Page<Tag> findTagsByCreatorId(@Param("creatorId") Long creatorId, Pageable pageable);
}
