package it.mahmoud.advmanagement.integration;

import it.mahmoud.advmanagement.dto.ad.AdCreateDTO;
import it.mahmoud.advmanagement.dto.tag.TagBulkDTO;
import it.mahmoud.advmanagement.dto.tag.TagCreateDTO;
import it.mahmoud.advmanagement.dto.tag.TagDTO;
import it.mahmoud.advmanagement.dto.user.UserCreateDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.repo.AdRepository;
import it.mahmoud.advmanagement.repo.TagRepository;
import it.mahmoud.advmanagement.repo.UserRepository;
import it.mahmoud.advmanagement.service.AdService;
import it.mahmoud.advmanagement.service.TagService;
import it.mahmoud.advmanagement.service.UserService;
import it.mahmoud.advmanagement.util.AdStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TagService.
 *
 * Note: This uses an actual database (H2 in-memory) and real Spring context.
 * The @ActiveProfiles("test") ensures we use test configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // Each test runs in a transaction that is rolled back at the end
public class TagServiceIntegrationTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdService adService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    private TagCreateDTO tagCreateDTO;
    private Long userId;
    private Long categoryId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Clean repositories
        adRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user for ads
        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();
        userId = userService.registerUser(userCreateDTO).getId();

        // Set up tag create DTO
        tagCreateDTO = TagCreateDTO.builder()
                .name("sale")
                .build();

        // Set up pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createTag_Success() {
        // Act
        TagDTO result = tagService.createTag(tagCreateDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(tagCreateDTO.getName().toLowerCase(), result.getName());
        assertEquals(0, result.getAdsCount());
    }

    @Test
    void createTag_MixedCase_NormalizedToLowercase() {
        // Given
        TagCreateDTO mixedCaseTag = TagCreateDTO.builder()
                .name("SALE-ITEM")
                .build();

        // Act
        TagDTO result = tagService.createTag(mixedCaseTag);

        // Assert
        assertNotNull(result);
        assertEquals("sale-item", result.getName());  // Should be lowercase
    }

    @Test
    void createTag_DuplicateName_ThrowsException() {
        // Given - Create tag first
        tagService.createTag(tagCreateDTO);

        // Create another tag with the same name but different case
        TagCreateDTO duplicateTag = TagCreateDTO.builder()
                .name("SALE")  // Same as "sale" but uppercase
                .build();

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                tagService.createTag(duplicateTag));
    }

    @Test
    void updateTag_Success() {
        // Given - Create tag first
        TagDTO tag = tagService.createTag(tagCreateDTO);

        // Create update DTO
        TagCreateDTO updateDTO = TagCreateDTO.builder()
                .name("discount")
                .build();

        // Act
        TagDTO result = tagService.updateTag(tag.getId(), updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName().toLowerCase(), result.getName());
    }

    @Test
    void updateTag_DuplicateName_ThrowsException() {
        // Given - Create two tags
        TagDTO tag1 = tagService.createTag(tagCreateDTO);  // "sale"

        TagCreateDTO tag2CreateDTO = TagCreateDTO.builder()
                .name("discount")
                .build();
        TagDTO tag2 = tagService.createTag(tag2CreateDTO);

        // Create update DTO with name of first tag
        TagCreateDTO updateDTO = TagCreateDTO.builder()
                .name("sale")  // Same as tag1
                .build();

        // Act & Assert - Try to update tag2 with tag1's name
        assertThrows(DuplicateResourceException.class, () ->
                tagService.updateTag(tag2.getId(), updateDTO));
    }

    @Test
    void getTagById_Success() {
        // Given - Create tag first
        TagDTO createdTag = tagService.createTag(tagCreateDTO);

        // Act
        TagDTO result = tagService.getTagById(createdTag.getId());

        // Assert
        assertNotNull(result);
        assertEquals(createdTag.getId(), result.getId());
        assertEquals(createdTag.getName(), result.getName());
    }

    @Test
    void getTagById_TagNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tagService.getTagById(999L));
    }

    @Test
    void getTagByName_Success() {
        // Given - Create tag first
        tagService.createTag(tagCreateDTO);

        // Act - Try to get with different case
        TagDTO result = tagService.getTagByName("SALE");

        // Assert
        assertNotNull(result);
        assertEquals(tagCreateDTO.getName().toLowerCase(), result.getName());
    }

    @Test
    void getTagByName_TagNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tagService.getTagByName("nonexistent"));
    }

    @Test
    void tagExists_ReturnsTrueWhenExists() {
        // Given - Create tag first
        tagService.createTag(tagCreateDTO);

        // Act - Check with different case
        boolean result = tagService.tagExists("SALE");

        // Assert
        assertTrue(result);
    }
}