package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.config.TagSpecifications;
import it.mahmoud.advmanagement.dto.tag.TagBulkDTO;
import it.mahmoud.advmanagement.dto.tag.TagCreateDTO;
import it.mahmoud.advmanagement.dto.tag.TagDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.Tag;
import it.mahmoud.advmanagement.repo.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag testTag;
    private TagCreateDTO testTagCreateDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Set up test tag
        testTag = Tag.builder()
                .id(1L)
                .name("sale")
                .ads(new HashSet<>())
                .build();

        // Set up test tag create DTO
        testTagCreateDTO = TagCreateDTO.builder()
                .name("discount")
                .build();

        // Set up pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createTag_Success() {
        // Given
        when(tagRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            tag.setId(2L);
            return tag;
        });

        // Act
        TagDTO result = tagService.createTag(testTagCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testTagCreateDTO.getName().toLowerCase(), result.getName());

        // Verify interactions
        verify(tagRepository).existsByNameIgnoreCase(testTagCreateDTO.getName());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void createTag_DuplicateName_ThrowsException() {
        // Given
        when(tagRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                tagService.createTag(testTagCreateDTO));

        // Verify interactions
        verify(tagRepository).existsByNameIgnoreCase(testTagCreateDTO.getName());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_Success() {
        // Given
        Long tagId = 1L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        // Create update DTO
        TagCreateDTO updateDTO = TagCreateDTO.builder()
                .name("UPDATED-SALE")  // Different case to test normalization to lowercase
                .build();

        // Act
        TagDTO result = tagService.updateTag(tagId, updateDTO);

        // Assert
        assertNotNull(result);

        // Verify tag was updated with correct values
        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        Tag capturedTag = tagCaptor.getValue();
        assertEquals(updateDTO.getName().toLowerCase(), capturedTag.getName());
    }

    @Test
    void updateTag_DuplicateName_ThrowsException() {
        // Given
        Long tagId = 1L;
        Tag existingTag = Tag.builder()
                .id(2L) // Different ID
                .name("discount")
                .build();

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(existingTag));

        // Create update DTO with a name that already exists for another tag
        TagCreateDTO updateDTO = TagCreateDTO.builder()
                .name("discount")
                .build();

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                tagService.updateTag(tagId, updateDTO));

        // Verify interactions
        verify(tagRepository).findById(tagId);
        verify(tagRepository).findByNameIgnoreCase(updateDTO.getName());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void getTagById_Success() {
        // Given
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));

        // Act
        TagDTO result = tagService.getTagById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        assertEquals(testTag.getName(), result.getName());

        // Verify interactions
        verify(tagRepository).findById(1L);
    }

    @Test
    void getTagById_TagNotFound_ThrowsException() {
        // Given
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tagService.getTagById(999L));

        // Verify interactions
        verify(tagRepository).findById(999L);
    }

    @Test
    void getTagByName_Success() {
        // Given
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(testTag));

        // Act
        TagDTO result = tagService.getTagByName("SALE");  // Mixed case to test case insensitivity

        // Assert
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        assertEquals(testTag.getName(), result.getName());

        // Verify interactions
        verify(tagRepository).findByNameIgnoreCase("SALE");
    }

    @Test
    void getTagByName_TagNotFound_ThrowsException() {
        // Given
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tagService.getTagByName("nonexistent"));

        // Verify interactions
        verify(tagRepository).findByNameIgnoreCase("nonexistent");
    }

    @Test
    void tagExists_ReturnsTrueWhenExists() {
        // Given
        when(tagRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // Act
        boolean result = tagService.tagExists("sale");

        // Assert
        assertTrue(result);

        // Verify interactions
        verify(tagRepository).existsByNameIgnoreCase("sale");
    }

    @Test
    void tagExists_ReturnsFalseWhenNotExists() {
        // Given
        when(tagRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);

        // Act
        boolean result = tagService.tagExists("nonexistent");

        // Assert
        assertFalse(result);

        // Verify interactions
        verify(tagRepository).existsByNameIgnoreCase("nonexistent");
    }

    @Test
    void deleteTag_Success() {
        // Given
        // Empty set of ads (no ads using this tag)
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        doNothing().when(tagRepository).delete(any(Tag.class));

        // Act - Should not throw exception
        assertDoesNotThrow(() -> tagService.deleteTag(1L));

        // Verify interactions
        verify(tagRepository).findById(1L);
        verify(tagRepository).delete(testTag);
    }

    @Test
    void deleteTag_WithAds_ThrowsException() {
        // Given
        // Add an ad to the test tag
        Ad ad = new Ad();
        ad.setId(1L);
        ad.setTitle("Test Ad");
        testTag.getAds().add(ad);

        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                tagService.deleteTag(1L));

        // Verify interactions
        verify(tagRepository).findById(1L);
        verify(tagRepository, never()).delete(any(Tag.class));
    }

    @Test
    void getAllTags_ReturnsPaginatedTags() {
        // Given
        List<Tag> tags = Collections.singletonList(testTag);
        Page<Tag> tagPage = new PageImpl<>(tags, pageable, 1);
        when(tagRepository.findAll(pageable)).thenReturn(tagPage);

        // Act
        Page<TagDTO> result = tagService.getAllTags(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testTag.getId(), result.getContent().get(0).getId());
        assertEquals(testTag.getName(), result.getContent().get(0).getName());

        // Verify interactions
        verify(tagRepository).findAll(pageable);
    }

    @Test
    void searchTags_ReturnsPaginatedSearchResults() {
        // Given
        List<Tag> tags = Collections.singletonList(testTag);
        Page<Tag> tagPage = new PageImpl<>(tags, pageable, 1);
        when(tagRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tagPage);

        // Act
        Page<TagDTO> result = tagService.searchTags("sale", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(tagRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getMostPopularTags_ReturnsLimitedList() {
        // Given
        List<Tag> popularTags = Collections.singletonList(testTag);
        when(tagRepository.findMostPopularTags(anyInt())).thenReturn(popularTags);

        // Act
        List<TagDTO> result = tagService.getMostPopularTags(10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTag.getId(), result.get(0).getId());
        assertEquals(testTag.getName(), result.get(0).getName());

        // Verify interactions
        verify(tagRepository).findMostPopularTags(10);
    }

    @Test
    void createOrGetTags_AllNew_CreatesAndReturnsTags() {
        // Given
        TagBulkDTO bulkDTO = new TagBulkDTO();
        bulkDTO.setTagNames(new HashSet<>(Arrays.asList("new1", "new2")));

        // No existing tags found
        when(tagRepository.findByNameInIgnoreCase(anySet())).thenReturn(new HashSet<>());

        // Save new tags
        when(tagRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Tag> tags = invocation.getArgument(0);
            int id = 1;
            for (Tag tag : tags) {
                tag.setId((long) id++);
            }
            return tags;
        });

        // Act
        Set<TagDTO> result = tagService.createOrGetTags(bulkDTO);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify interactions
        verify(tagRepository).findByNameInIgnoreCase(anySet());
        verify(tagRepository).saveAll(anyList());
    }

    @Test
    void createOrGetTags_MixedExistingAndNew_ReturnsAllTags() {
        // Given
        TagBulkDTO bulkDTO = new TagBulkDTO();
        bulkDTO.setTagNames(new HashSet<>(Arrays.asList("sale", "new")));

        // One existing tag found
        Set<Tag> existingTags = new HashSet<>();
        existingTags.add(testTag); // "sale" tag
        when(tagRepository.findByNameInIgnoreCase(anySet())).thenReturn(existingTags);

        // Save one new tag
        when(tagRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Tag> tags = invocation.getArgument(0);
            for (Tag tag : tags) {
                tag.setId(2L); // ID for "new" tag
            }
            return tags;
        });

        // Act
        Set<TagDTO> result = tagService.createOrGetTags(bulkDTO);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify interactions
        verify(tagRepository).findByNameInIgnoreCase(anySet());
        verify(tagRepository).saveAll(anyList());
    }

    @Test
    void getTagsByNames_ReturnsMatchingTags() {
        // Given
        Set<String> tagNames = new HashSet<>(Arrays.asList("sale", "discount"));

        Set<Tag> foundTags = new HashSet<>();
        foundTags.add(testTag); // Only "sale" tag found
        when(tagRepository.findByNameInIgnoreCase(anySet())).thenReturn(foundTags);

        // Act
        Set<TagDTO> result = tagService.getTagsByNames(tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify interactions
        verify(tagRepository).findByNameInIgnoreCase(anySet());
    }

    @Test
    void getTagsByAdId_ReturnsTagsForAd() {
        // Given
        List<Tag> adTags = Collections.singletonList(testTag);
        when(tagRepository.findTagsByAdId(anyLong())).thenReturn(adTags);

        // Act
        List<TagDTO> result = tagService.getTagsByAdId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTag.getId(), result.get(0).getId());
        assertEquals(testTag.getName(), result.get(0).getName());

        // Verify interactions
        verify(tagRepository).findTagsByAdId(1L);
    }

    @Test
    void getUnusedTags_ReturnsPaginatedUnusedTags() {
        // Given
        List<Tag> unusedTags = Collections.singletonList(testTag);
        Page<Tag> tagPage = new PageImpl<>(unusedTags, pageable, 1);
        when(tagRepository.findUnusedTags(any(Pageable.class))).thenReturn(tagPage);

        // Act
        Page<TagDTO> result = tagService.getUnusedTags(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(tagRepository).findUnusedTags(pageable);
    }

    @Test
    void getRelatedTags_ReturnsRelatedTags() {
        // Given
        Long tagId = 1L;

        // Mock the existence check
        when(tagRepository.existsById(tagId)).thenReturn(true);

        // Mock the related tags query result
        List<Object[]> relatedTagsData = new ArrayList<>();
        relatedTagsData.add(new Object[]{2L, 5L}); // Tag ID 2 with count 5
        when(tagRepository.findRelatedTags(eq(tagId), anyInt())).thenReturn(relatedTagsData);

        // Mock finding the related tag by ID
        Tag relatedTag = Tag.builder()
                .id(2L)
                .name("discount")
                .ads(new HashSet<>())
                .build();
        when(tagRepository.findById(2L)).thenReturn(Optional.of(relatedTag));

        // Act
        List<TagDTO> result = tagService.getRelatedTags(tagId, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(relatedTag.getId(), result.get(0).getId());
        assertEquals(relatedTag.getName(), result.get(0).getName());
        assertEquals(5, result.get(0).getAdsCount());

        // Verify interactions
        verify(tagRepository).existsById(tagId);
        verify(tagRepository).findRelatedTags(tagId, 10);
        verify(tagRepository).findById(2L);
    }

    @Test
    void getRelatedTags_TagNotFound_ThrowsException() {
        // Given
        Long tagId = 999L;
        when(tagRepository.existsById(tagId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tagService.getRelatedTags(tagId, 10));

        // Verify interactions
        verify(tagRepository).existsById(tagId);
        verify(tagRepository, never()).findRelatedTags(anyLong(), anyInt());
    }

    @Test
    void getTagsByCreator_ReturnsPaginatedTags() {
        // Given
        Long creatorId = 1L;
        List<Tag> creatorTags = Collections.singletonList(testTag);
        Page<Tag> tagPage = new PageImpl<>(creatorTags, pageable, 1);
        when(tagRepository.findTagsByCreatorId(eq(creatorId), any(Pageable.class))).thenReturn(tagPage);

        // Act
        Page<TagDTO> result = tagService.getTagsByCreator(creatorId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(tagRepository).findTagsByCreatorId(creatorId, pageable);
    }

    @Test
    void getTagsByCategory_ReturnsPaginatedTags() {
        // Given
        Long categoryId = 1L;
        List<Tag> categoryTags = Collections.singletonList(testTag);
        Page<Tag> tagPage = new PageImpl<>(categoryTags, pageable, 1);
        when(tagRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tagPage);

        // Act
        Page<TagDTO> result = tagService.getTagsByCategory(categoryId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(tagRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void countAdsByTag_ReturnsCorrectCount() {
        // Given
        Long tagId = 1L;
        when(tagRepository.countAdsByTagId(tagId)).thenReturn(5L);

        // Act
        long result = tagService.countAdsByTag(tagId);

        // Assert
        assertEquals(5L, result);

        // Verify interactions
        verify(tagRepository).countAdsByTagId(tagId);
    }
}