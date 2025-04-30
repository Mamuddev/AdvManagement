package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.config.TagSpecifications;
import it.mahmoud.advmanagement.dto.tag.TagBulkDTO;
import it.mahmoud.advmanagement.dto.tag.TagCreateDTO;
import it.mahmoud.advmanagement.dto.tag.TagDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.model.Tag;
import it.mahmoud.advmanagement.repo.TagRepository;
import it.mahmoud.advmanagement.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TagService
 */
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public TagDTO createTag(TagCreateDTO tagCreateDTO) {
        // Check if tag with same name already exists
        if (tagRepository.existsByNameIgnoreCase(tagCreateDTO.getName())) {
            throw DuplicateResourceException.tag(tagCreateDTO.getName());
        }

        // Create tag
        Tag tag = Tag.builder()
                .name(tagCreateDTO.getName().toLowerCase())
                .build();

        tag = tagRepository.save(tag);

        return mapToDTO(tag);
    }

    @Override
    @Transactional
    public TagDTO updateTag(Long id, TagCreateDTO tagCreateDTO) {
        // Find tag
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.tag(id.toString()));

        // Check for duplicate name
        tagRepository.findByNameIgnoreCase(tagCreateDTO.getName())
                .ifPresent(existingTag -> {
                    // If another tag with the same name exists
                    if (!existingTag.getId().equals(id)) {
                        throw DuplicateResourceException.tag(tagCreateDTO.getName());
                    }
                });

        // Update name
        tag.setName(tagCreateDTO.getName().toLowerCase());
        tag = tagRepository.save(tag);

        return mapToDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getTagById(Long id) {
        return tagRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> ResourceNotFoundException.tag(id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getTagByName(String name) {
        return tagRepository.findByNameIgnoreCase(name)
                .map(this::mapToDTO)
                .orElseThrow(() -> ResourceNotFoundException.tag(name));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tagExists(String name) {
        return tagRepository.existsByNameIgnoreCase(name);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.tag(id.toString()));

        // Check if tag is used in ads
        if (!tag.getAds().isEmpty()) {
            throw InvalidOperationException.tagHasAds(id.toString());
        }

        tagRepository.delete(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> getAllTags(Pageable pageable) {
        return tagRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> searchTags(String searchTerm, Pageable pageable) {
        Specification<Tag> spec = TagSpecifications.nameContains(searchTerm);
        return tagRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getMostPopularTags(int limit) {
        return tagRepository.findMostPopularTags(limit).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Set<TagDTO> createOrGetTags(TagBulkDTO tagBulkDTO) {
        Set<String> tagNames = tagBulkDTO.getTagNames().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Find existing tags
        Set<Tag> existingTags = tagRepository.findByNameInIgnoreCase(tagNames);

        // Determine which tags need to be created
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> newTagNames = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name.toLowerCase()))
                .collect(Collectors.toSet());

        // Create new tags
        List<Tag> newTags = newTagNames.stream()
                .map(name -> Tag.builder().name(name.toLowerCase()).build())
                .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
        }

        // Combine existing and new tags
        existingTags.addAll(newTags);

        return existingTags.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<TagDTO> getTagsByNames(Set<String> tagNames) {
        Set<String> normalizedNames = tagNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return tagRepository.findByNameInIgnoreCase(normalizedNames).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getTagsByAdId(Long adId) {
        return tagRepository.findTagsByAdId(adId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> getUnusedTags(Pageable pageable) {
        return tagRepository.findUnusedTags(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getRelatedTags(Long tagId, int limit) {
        // Ensure tag exists
        if (!tagRepository.existsById(tagId)) {
            throw ResourceNotFoundException.tag(tagId.toString());
        }

        // Get related tags
        List<Object[]> relatedTags = tagRepository.findRelatedTags(tagId, limit);

        // Convert to DTOs
        return relatedTags.stream()
                .map(row -> {
                    Long id = (Long) row[0];
                    Long count = (Long) row[1];

                    // Fetch tag details
                    return tagRepository.findById(id)
                            .map(tag -> {
                                TagDTO dto = mapToDTO(tag);
                                dto.setAdsCount(count.intValue());
                                return dto;
                            })
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> getTagsByCreator(Long creatorId, Pageable pageable) {
        return tagRepository.findTagsByCreatorId(creatorId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> getTagsByCategory(Long categoryId, Pageable pageable) {
        Specification<Tag> spec = TagSpecifications.usedInCategory(categoryId);
        return tagRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAdsByTag(Long tagId) {
        return tagRepository.countAdsByTagId(tagId);
    }

    /**
     * Map Tag entity to TagDTO
     */
    private TagDTO mapToDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .adsCount(tag.getAds() != null ? tag.getAds().size() : 0)
                .build();
    }
}