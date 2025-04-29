package it.mahmoud.advmanagement.service;

import it.mahmoud.advmanagement.dto.tag.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Service interface for operations on Tag entities
 */
public interface TagService {

    /**
     * Create a new tag
     * @param tagCreateDTO Tag creation data
     * @return Created tag data
     */
    TagDTO createTag(TagCreateDTO tagCreateDTO);

    /**
     * Update an existing tag
     * @param id Tag ID
     * @param tagCreateDTO Tag update data
     * @return Updated tag data
     */
    TagDTO updateTag(Long id, TagCreateDTO tagCreateDTO);

    /**
     * Get tag by ID
     * @param id Tag ID
     * @return Tag data
     */
    TagDTO getTagById(Long id);

    /**
     * Get tag by name
     * @param name Tag name
     * @return Tag data
     */
    TagDTO getTagByName(String name);

    /**
     * Check if tag with given name exists
     * @param name Tag name
     * @return true if exists
     */
    boolean tagExists(String name);

    /**
     * Delete a tag
     * @param id Tag ID
     */
    void deleteTag(Long id);

    /**
     * Get all tags with pagination
     * @param pageable Pagination information
     * @return Page of tags
     */
    Page<TagDTO> getAllTags(Pageable pageable);

    /**
     * Search tags by name
     * @param searchTerm Search term
     * @param pageable Pagination information
     * @return Page of matching tags
     */
    Page<TagDTO> searchTags(String searchTerm, Pageable pageable);

    /**
     * Find most popular tags
     * @param limit Maximum number of tags to return
     * @return List of most used tags
     */
    List<TagDTO> getMostPopularTags(int limit);

    /**
     * Create or get multiple tags by name
     * @param tagBulkDTO Tag names
     * @return Set of tags
     */
    Set<TagDTO> createOrGetTags(TagBulkDTO tagBulkDTO);

    /**
     * Get tags by names
     * @param tagNames Set of tag names
     * @return Set of tags
     */
    Set<TagDTO> getTagsByNames(Set<String> tagNames);

    /**
     * Get tags for a specific ad
     * @param adId Ad ID
     * @return List of tags
     */
    List<TagDTO> getTagsByAdId(Long adId);

    /**
     * Find unused tags
     * @param pageable Pagination information
     * @return Page of unused tags
     */
    Page<TagDTO> getUnusedTags(Pageable pageable);

    /**
     * Find related tags (tags that often appear with a given tag)
     * @param tagId Tag ID
     * @param limit Maximum number of related tags to return
     * @return List of related tags
     */
    List<TagDTO> getRelatedTags(Long tagId, int limit);

    /**
     * Get tags used by a specific creator
     * @param creatorId Creator ID
     * @param pageable Pagination information
     * @return Page of tags used by the creator
     */
    Page<TagDTO> getTagsByCreator(Long creatorId, Pageable pageable);

    /**
     * Get tags used in a specific category
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Page of tags used in the category
     */
    Page<TagDTO> getTagsByCategory(Long categoryId, Pageable pageable);

    /**
     * Count ads for a specific tag
     * @param tagId Tag ID
     * @return Number of ads with this tag
     */
    long countAdsByTag(Long tagId);
}