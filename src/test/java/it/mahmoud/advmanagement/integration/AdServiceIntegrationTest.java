package it.mahmoud.advmanagement.integration;

import it.mahmoud.advmanagement.dto.ad.AdCreateDTO;
import it.mahmoud.advmanagement.dto.ad.AdDTO;
import it.mahmoud.advmanagement.dto.ad.AdStatusUpdateDTO;
import it.mahmoud.advmanagement.dto.ad.AdUpdateDTO;
import it.mahmoud.advmanagement.dto.category.CategoryCreateDTO;
import it.mahmoud.advmanagement.dto.tag.TagCreateDTO;
import it.mahmoud.advmanagement.dto.user.UserCreateDTO;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.repo.AdRepository;
import it.mahmoud.advmanagement.repo.CategoryRepository;
import it.mahmoud.advmanagement.repo.TagRepository;
import it.mahmoud.advmanagement.repo.UserRepository;
import it.mahmoud.advmanagement.service.AdService;
import it.mahmoud.advmanagement.service.CategoryService;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AdService.

 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // Each test runs in a transaction that is rolled back at the end
public class AdServiceIntegrationTest {

    @Autowired
    private AdService adService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    private Long userId;
    private Long categoryId;
    private Long tagId;
    private AdCreateDTO adCreateDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Clean repositories to start fresh
        adRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();
        userId = userService.registerUser(userCreateDTO).getId();

        // Create test category
        CategoryCreateDTO categoryCreateDTO = CategoryCreateDTO.builder()
                .name("Electronics")
                .description("Electronic devices and accessories")
                .build();
        categoryId = categoryService.createCategory(categoryCreateDTO).getId();

        // Create test tag
        TagCreateDTO tagCreateDTO = TagCreateDTO.builder()
                .name("sale")
                .build();
        tagId = tagService.createTag(tagCreateDTO).getId();

        // Set up test ad create DTO
        adCreateDTO = AdCreateDTO.builder()
                .title("Test Ad")
                .description("This is a test ad description with sufficient length to pass validation")
                .price(new BigDecimal("99.99"))
                .creatorId(userId)
                .categoryIds(new HashSet<>(Collections.singletonList(categoryId)))
                .tagIds(new HashSet<>(Collections.singletonList(tagId)))
                .status(AdStatus.DRAFT)
                .build();
    }

    @Test
    void createAd_Success() {
        // Act
        AdDTO result = adService.createAd(adCreateDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(adCreateDTO.getTitle(), result.getTitle());
        assertEquals(adCreateDTO.getDescription(), result.getDescription());
        assertEquals(adCreateDTO.getPrice().setScale(2), result.getPrice().setScale(2));
        assertEquals(userId, result.getCreatorId());
        assertTrue(result.getCategoryIds().contains(categoryId));
        assertTrue(result.getTagIds().contains(tagId));
        assertEquals(AdStatus.DRAFT, result.getStatus());
        assertNotNull(result.getCreationDate());
    }

    @Test
    void createAd_UserNotFound_ThrowsException() {
        // Given
        adCreateDTO.setCreatorId(999L); // Non-existent user ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                adService.createAd(adCreateDTO));
    }

    @Test
    void updateAd_Success() {
        // Given - Create an ad first
        AdDTO createdAd = adService.createAd(adCreateDTO);

        AdUpdateDTO updateDTO = AdUpdateDTO.builder()
                .id(createdAd.getId())
                .title("Updated Test Ad")
                .description("This is an updated test ad description with sufficient length to pass validation")
                .price(new BigDecimal("149.99"))
                .status(AdStatus.PUBLISHED)
                .featured(true)
                .categoryIds(new HashSet<>(Collections.singletonList(categoryId)))
                .tagIds(new HashSet<>(Collections.singletonList(tagId)))
                .expirationDate(now.plusDays(30))
                .build();

        // Act
        AdDTO result = adService.updateAd(createdAd.getId(), updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getTitle(), result.getTitle());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getPrice().setScale(2), result.getPrice().setScale(2));
        assertEquals(updateDTO.getStatus(), result.getStatus());
        assertEquals(updateDTO.getFeatured(), result.getFeatured());
        assertEquals(updateDTO.getExpirationDate(), result.getExpirationDate());
        assertNotNull(result.getModificationDate());

        // Check publication date was set when changing to PUBLISHED
        assertNotNull(result.getPublicationDate());
    }

    @Test
    void updateAd_IdMismatch_ThrowsException() {
        // Given - Create an ad first
        AdDTO createdAd = adService.createAd(adCreateDTO);

        AdUpdateDTO updateDTO = AdUpdateDTO.builder()
                .id(createdAd.getId())
                .title("Updated Test Ad")
                .description("This is an updated test ad description with sufficient length")
                .price(new BigDecimal("149.99"))
                .build();

        // Act & Assert - Try with a different ID in the path
        assertThrows(InvalidOperationException.class, () ->
                adService.updateAd(createdAd.getId() + 1, updateDTO));
    }

    @Test
    void getAdById_Success() {
        // Given - Create an ad first
        AdDTO createdAd = adService.createAd(adCreateDTO);

        // Act
        AdDTO result = adService.getAdById(createdAd.getId());

        // Assert
        assertNotNull(result);
        assertEquals(createdAd.getId(), result.getId());
        assertEquals(createdAd.getTitle(), result.getTitle());
        assertEquals(createdAd.getDescription(), result.getDescription());
        assertEquals(createdAd.getStatus(), result.getStatus());
    }

    @Test
    void getAdById_AdNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                adService.getAdById(999L)); // Non-existent ad ID
    }

    @Test
    void deleteAd_Success() {
        // Given - Create an ad first
        AdDTO createdAd = adService.createAd(adCreateDTO);

        // Act - Should not throw exception
        assertDoesNotThrow(() -> adService.deleteAd(createdAd.getId()));

        // Assert - Ad should be soft deleted (status changed to DELETED)
        AdDTO deletedAd = adService.getAdById(createdAd.getId());
        assertEquals(AdStatus.DELETED, deletedAd.getStatus());
    }

    @Test
    void getAllAds_ReturnsPaginatedAds() {
        // Given - Create multiple ads
        adService.createAd(adCreateDTO);

        AdCreateDTO ad2 = AdCreateDTO.builder()
                .title("Second Test Ad")
                .description("This is another test ad description with sufficient length to pass validation")
                .price(new BigDecimal("199.99"))
                .creatorId(userId)
                .categoryIds(new HashSet<>(Collections.singletonList(categoryId)))
                .tagIds(new HashSet<>(Collections.singletonList(tagId)))
                .status(AdStatus.DRAFT)
                .build();
        adService.createAd(ad2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<AdDTO> result = adService.getAllAds(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getAdsByStatus_ReturnsPaginatedAdsByStatus() {
        // Given - Create ads with different statuses
        adService.createAd(adCreateDTO); // DRAFT

        AdCreateDTO publishedAd = AdCreateDTO.builder()
                .title("Published Ad")
                .description("This is a published ad description with sufficient length to pass validation")
                .price(new BigDecimal("299.99"))
                .creatorId(userId)
                .categoryIds(new HashSet<>(Collections.singletonList(categoryId)))
                .tagIds(new HashSet<>(Collections.singletonList(tagId)))
                .status(AdStatus.PUBLISHED)
                .build();
        adService.createAd(publishedAd);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<AdDTO> draftResults = adService.getAdsByStatus(AdStatus.DRAFT, pageable);
        Page<AdDTO> publishedResults = adService.getAdsByStatus(AdStatus.PUBLISHED, pageable);

        // Assert
        assertEquals(1, draftResults.getTotalElements());
        assertEquals(1, publishedResults.getTotalElements());
        assertEquals("Test Ad", draftResults.getContent().get(0).getTitle());
        assertEquals("Published Ad", publishedResults.getContent().get(0).getTitle());
    }

    @Test
    void updateAdStatus_Success() {
        // Given - Create an ad first
        AdDTO createdAd = adService.createAd(adCreateDTO);

        AdStatusUpdateDTO statusUpdateDTO = AdStatusUpdateDTO.builder()
                .id(createdAd.getId())
                .newStatus(AdStatus.PUBLISHED)
                .expirationDate(now.plusDays(30))
                .build();

        // Act
        AdDTO result = adService.updateAdStatus(statusUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AdStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublicationDate());
        assertEquals(statusUpdateDTO.getExpirationDate(), result.getExpirationDate());
    }

    @Test
    void incrementViews_Success() {
        // Given - Create an ad first
        AdDTO createdAd = adService.createAd(adCreateDTO);
        int initialViews = createdAd.getViews();

        // Act - Should not throw exception
        assertDoesNotThrow(() -> adService.incrementViews(createdAd.getId()));

        // Assert - Views should be incremented
        AdDTO updatedAd = adService.getAdById(createdAd.getId());
        assertEquals(initialViews + 1, updatedAd.getViews());
    }

    @Test
    void publishAd_Success() {
        // Given - Create an ad first
        AdDTO createdAd = adService.createAd(adCreateDTO);
        LocalDateTime expirationDate = now.plusDays(30);

        // Act
        AdDTO result = adService.publishAd(createdAd.getId(), expirationDate);

        // Assert
        assertNotNull(result);
        assertEquals(AdStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublicationDate());
        assertEquals(expirationDate, result.getExpirationDate());
    }

}