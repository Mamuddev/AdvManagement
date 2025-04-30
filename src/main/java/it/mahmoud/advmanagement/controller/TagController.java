package it.mahmoud.advmanagement.controller;

import it.mahmoud.advmanagement.dto.tag.TagBulkDTO;
import it.mahmoud.advmanagement.dto.tag.TagCreateDTO;
import it.mahmoud.advmanagement.dto.tag.TagDTO;
import it.mahmoud.advmanagement.dto.response.ApiResponseDTO;
import it.mahmoud.advmanagement.dto.response.PageMetaDTO;
import it.mahmoud.advmanagement.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST Controller for Tag operations
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Create a new tag
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<TagDTO>> createTag(@RequestBody TagCreateDTO tagCreateDTO) {
        TagDTO createdTag = tagService.createTag(tagCreateDTO);
        return new ResponseEntity<>(
                ApiResponseDTO.success(createdTag, "Tag created successfully"),
                HttpStatus.CREATED);
    }

    /**
     * Update an existing tag
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TagDTO>> updateTag(
            @PathVariable Long id,
             @RequestBody TagCreateDTO tagCreateDTO) {
        TagDTO updatedTag = tagService.updateTag(id, tagCreateDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(updatedTag, "Tag updated successfully"));
    }

    /**
     * Get tag by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TagDTO>> getTagById(@PathVariable Long id) {
        TagDTO tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(tag, "Tag retrieved successfully"));
    }

    /**
     * Get tag by name
     */
    @GetMapping("/by-name")
    public ResponseEntity<ApiResponseDTO<TagDTO>> getTagByName(@RequestParam String name) {
        TagDTO tag = tagService.getTagByName(name);
        return ResponseEntity.ok(ApiResponseDTO.success(tag, "Tag retrieved successfully"));
    }

    /**
     * Check if tag exists by name
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponseDTO<Boolean>> tagExists(@RequestParam String name) {
        boolean exists = tagService.tagExists(name);
        return ResponseEntity.ok(ApiResponseDTO.success(exists, "Tag existence checked"));
    }

    /**
     * Delete a tag
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Tag deleted successfully"));
    }

    /**
     * Get all tags (paginated)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<TagDTO>>> getAllTags(Pageable pageable) {
        Page<TagDTO> tags = tagService.getAllTags(pageable);
        PageMetaDTO pageMeta = createPageMeta(tags);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, pageMeta));
    }

    /**
     * Search tags by term
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<Page<TagDTO>>> searchTags(
            @RequestParam String term,
            Pageable pageable) {
        Page<TagDTO> tags = tagService.searchTags(term, pageable);
        PageMetaDTO pageMeta = createPageMeta(tags);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, pageMeta));
    }

    /**
     * Get most popular tags
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponseDTO<List<TagDTO>>> getMostPopularTags(
            @RequestParam(defaultValue = "10") int limit) {
        List<TagDTO> tags = tagService.getMostPopularTags(limit);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, "Popular tags retrieved successfully"));
    }

    /**
     * Create or get multiple tags in bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponseDTO<Set<TagDTO>>> createOrGetTags(@RequestBody TagBulkDTO tagBulkDTO) {
        Set<TagDTO> tags = tagService.createOrGetTags(tagBulkDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, "Tags created or retrieved successfully"));
    }

    /**
     * Get tags by their names
     */
    @PostMapping("/by-names")
    public ResponseEntity<ApiResponseDTO<Set<TagDTO>>> getTagsByNames(@RequestBody Set<String> tagNames) {
        Set<TagDTO> tags = tagService.getTagsByNames(tagNames);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, "Tags retrieved successfully"));
    }

    /**
     * Get tags for a specific ad
     */
    @GetMapping("/ad/{adId}")
    public ResponseEntity<ApiResponseDTO<List<TagDTO>>> getTagsByAdId(@PathVariable Long adId) {
        List<TagDTO> tags = tagService.getTagsByAdId(adId);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, "Tags retrieved successfully"));
    }

    /**
     * Get unused tags
     */
    @GetMapping("/unused")
    public ResponseEntity<ApiResponseDTO<Page<TagDTO>>> getUnusedTags(Pageable pageable) {
        Page<TagDTO> tags = tagService.getUnusedTags(pageable);
        PageMetaDTO pageMeta = createPageMeta(tags);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, pageMeta));
    }

    /**
     * Get related tags
     */
    @GetMapping("/{tagId}/related")
    public ResponseEntity<ApiResponseDTO<List<TagDTO>>> getRelatedTags(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "5") int limit) {
        List<TagDTO> tags = tagService.getRelatedTags(tagId, limit);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, "Related tags retrieved successfully"));
    }

    /**
     * Get tags used by a specific creator
     */
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<ApiResponseDTO<Page<TagDTO>>> getTagsByCreator(
            @PathVariable Long creatorId,
            Pageable pageable) {
        Page<TagDTO> tags = tagService.getTagsByCreator(creatorId, pageable);
        PageMetaDTO pageMeta = createPageMeta(tags);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, pageMeta));
    }

    /**
     * Get tags used in a specific category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponseDTO<Page<TagDTO>>> getTagsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        Page<TagDTO> tags = tagService.getTagsByCategory(categoryId, pageable);
        PageMetaDTO pageMeta = createPageMeta(tags);
        return ResponseEntity.ok(ApiResponseDTO.success(tags, pageMeta));
    }

    /**
     * Count ads with a specific tag
     */
    @GetMapping("/{tagId}/count-ads")
    public ResponseEntity<ApiResponseDTO<Long>> countAdsByTag(@PathVariable Long tagId) {
        long count = tagService.countAdsByTag(tagId);
        return ResponseEntity.ok(ApiResponseDTO.success(count, "Ad count retrieved successfully"));
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