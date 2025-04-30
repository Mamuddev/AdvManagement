package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.config.AdSpecifications;
import it.mahmoud.advmanagement.dto.ad.*;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.Category;
import it.mahmoud.advmanagement.model.Tag;
import it.mahmoud.advmanagement.model.User;
import it.mahmoud.advmanagement.repo.AdRepository;
import it.mahmoud.advmanagement.repo.CategoryRepository;
import it.mahmoud.advmanagement.repo.TagRepository;
import it.mahmoud.advmanagement.repo.UserRepository;
import it.mahmoud.advmanagement.service.AdService;
import it.mahmoud.advmanagement.util.AdStatus;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServiceImplTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private AdServiceImpl adService;

    private User testUser;
    private Category testCategory;
    private Tag testTag;
    private Ad testAd;
    private AdCreateDTO testAdCreateDTO;
    private AdUpdateDTO testAdUpdateDTO;
    private AdStatusUpdateDTO testStatusUpdateDTO;
    private Pageable pageable;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Set up test user
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .registrationDate(now.minusDays(30))
                .lastLogin(now.minusDays(5))
                .ads(new HashSet<>())
                .build();

        // Set up test category
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .ads(new HashSet<>())
                .subcategories(new HashSet<>())
                .build();

        // Set up test tag
        testTag = Tag.builder()
                .id(1L)
                .name("sale")
                .ads(new HashSet<>())
                .build();

        // Set up test ad
        testAd = Ad.builder()
                .id(1L)
                .title("Test Ad")
                .description("This is a test ad description")
                .price(new BigDecimal("99.99"))
                .status(AdStatus.DRAFT)
                .creator(testUser)
                .creationDate(now.minusDays(1))
                .views(0)
                .featured(false)
                .categories(new HashSet<>(Collections.singletonList(testCategory)))
                .tags(new HashSet<>(Collections.singletonList(testTag)))
                .build();

        // Add the ad to the user's ads
        testUser.getAds().add(testAd);

        // Add the ad to the category's ads
        testCategory.getAds().add(testAd);

        // Add the ad to the tag's ads
        testTag.getAds().add(testAd);

        // Set up test ad create DTO
        testAdCreateDTO = AdCreateDTO.builder()
                .title("New Test Ad")
                .description("This is a new test ad description")
                .price(new BigDecimal("199.99"))
                .creatorId(1L)
                .categoryIds(new HashSet<>(Collections.singletonList(1L)))
                .tagIds(new HashSet<>(Collections.singletonList(1L)))
                .build();

        // Set up test ad update DTO
        testAdUpdateDTO = AdUpdateDTO.builder()
                .id(1L)
                .title("Updated Test Ad")
                .description("This is an updated test ad description")
                .price(new BigDecimal("149.99"))
                .status(AdStatus.PUBLISHED)
                .featured(true)
                .categoryIds(new HashSet<>(Collections.singletonList(1L)))
                .tagIds(new HashSet<>(Collections.singletonList(1L)))
                .expirationDate(now.plusDays(30))
                .build();

        // Set up test status update DTO
        testStatusUpdateDTO = AdStatusUpdateDTO.builder()
                .id(1L)
                .newStatus(AdStatus.PUBLISHED)
                .expirationDate(now.plusDays(30))
                .build();

        // Set up pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createAd_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> {
            Ad ad = invocation.getArgument(0);
            ad.setId(1L);
            return ad;
        });

        // Act
        AdDTO result = adService.createAd(testAdCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testAdCreateDTO.getTitle(), result.getTitle());
        assertEquals(testAdCreateDTO.getDescription(), result.getDescription());
        assertEquals(testAdCreateDTO.getPrice().setScale(2), result.getPrice().setScale(2));
        assertEquals(testUser.getId(), result.getCreatorId());
        assertTrue(result.getCategoryIds().contains(testCategory.getId()));
        assertTrue(result.getTagIds().contains(testTag.getId()));

        // Verify interactions
        verify(userRepository).findById(testAdCreateDTO.getCreatorId());
        verify(categoryRepository).findById(anyLong());
        verify(tagRepository).findById(anyLong());
        verify(adRepository).save(any(Ad.class));
    }

    @Test
    void createAd_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                adService.createAd(testAdCreateDTO));

        // Verify interactions
        verify(userRepository).findById(testAdCreateDTO.getCreatorId());
        verify(categoryRepository, never()).findById(anyLong());
        verify(tagRepository, never()).findById(anyLong());
        verify(adRepository, never()).save(any(Ad.class));
    }

    @Test
    void updateAd_Success() {
        // Given
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(testAd));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(adRepository.save(any(Ad.class))).thenReturn(testAd);

        // Act
        AdDTO result = adService.updateAd(1L, testAdUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testAdUpdateDTO.getTitle(), result.getTitle());
        assertEquals(testAdUpdateDTO.getDescription(), result.getDescription());
        assertEquals(testAdUpdateDTO.getPrice().setScale(2), result.getPrice().setScale(2));
        assertEquals(testAdUpdateDTO.getStatus(), result.getStatus());
        assertEquals(testAdUpdateDTO.getFeatured(), result.getFeatured());

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(categoryRepository).findById(anyLong());
        verify(tagRepository).findById(anyLong());
        verify(adRepository).save(any(Ad.class));

        // Verify ad was updated with correct values
        ArgumentCaptor<Ad> adCaptor = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).save(adCaptor.capture());
        Ad capturedAd = adCaptor.getValue();
        assertEquals(testAdUpdateDTO.getTitle(), capturedAd.getTitle());
        assertEquals(testAdUpdateDTO.getDescription(), capturedAd.getDescription());
        assertEquals(testAdUpdateDTO.getPrice().setScale(2), capturedAd.getPrice().setScale(2));
        assertEquals(testAdUpdateDTO.getStatus(), capturedAd.getStatus());
        assertEquals(testAdUpdateDTO.getFeatured(), capturedAd.getFeatured());
        assertEquals(testAdUpdateDTO.getExpirationDate(), capturedAd.getExpirationDate());
        assertNotNull(capturedAd.getModificationDate());
    }

    @Test
    void updateAd_IdMismatch_ThrowsException() {
        // Given - mismatch between path ID and DTO ID
        Long pathId = 2L;

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                adService.updateAd(pathId, testAdUpdateDTO));

        // Verify interactions
        verify(adRepository, never()).findById(anyLong());
        verify(adRepository, never()).save(any(Ad.class));
    }

    @Test
    void updateAd_AdNotFound_ThrowsException() {
        // Given
        when(adRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                adService.updateAd(1L, testAdUpdateDTO));

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository, never()).save(any(Ad.class));
    }

    @Test
    void getAdById_Success() {
        // Given
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(testAd));

        // Act
        AdDTO result = adService.getAdById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testAd.getId(), result.getId());
        assertEquals(testAd.getTitle(), result.getTitle());
        assertEquals(testAd.getDescription(), result.getDescription());
        assertEquals(testAd.getStatus(), result.getStatus());

        // Verify interactions
        verify(adRepository).findById(1L);
    }

    @Test
    void getAdById_AdNotFound_ThrowsException() {
        // Given
        when(adRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                adService.getAdById(1L));

        // Verify interactions
        verify(adRepository).findById(1L);
    }

    @Test
    void deleteAd_Success() {
        // Given
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(testAd));
        when(adRepository.save(any(Ad.class))).thenReturn(testAd);

        // Act
        adService.deleteAd(1L);

        // Verify ad was soft deleted (status changed to DELETED)
        ArgumentCaptor<Ad> adCaptor = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).save(adCaptor.capture());
        Ad capturedAd = adCaptor.getValue();
        assertEquals(AdStatus.DELETED, capturedAd.getStatus());

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository).save(any(Ad.class));
    }

    @Test
    void deleteAd_AdNotFound_ThrowsException() {
        // Given
        when(adRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                adService.deleteAd(1L));

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository, never()).save(any(Ad.class));
    }

    @Test
    void getAllAds_ReturnsPaginatedAds() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findAll(pageable)).thenReturn(adPage);

        // Act
        Page<AdDTO> result = adService.getAllAds(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(adRepository).findAll(pageable);
    }

    @Test
    void getAdsByStatus_ReturnsPaginatedAdsByStatus() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findByStatus(any(AdStatus.class), any(Pageable.class))).thenReturn(adPage);

        // Act
        Page<AdDTO> result = adService.getAdsByStatus(AdStatus.DRAFT, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(adRepository).findByStatus(AdStatus.DRAFT, pageable);
    }

    @Test
    void getAdsByCreator_ReturnsPaginatedAdsByCreator() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findByCreatorId(anyLong(), any(Pageable.class))).thenReturn(adPage);

        // Act
        Page<AdDTO> result = adService.getAdsByCreator(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(adRepository).findByCreatorId(1L, pageable);
    }

    @Test
    void getAdsByCategory_ReturnsPaginatedAdsByCategory() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findByCategoryId(anyLong(), any(Pageable.class))).thenReturn(adPage);

        // Act
        Page<AdDTO> result = adService.getAdsByCategory(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(adRepository).findByCategoryId(1L, pageable);
    }

    @Test
    void getAdsByTag_ReturnsPaginatedAdsByTag() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findByTagId(anyLong(), any(Pageable.class))).thenReturn(adPage);

        // Act
        Page<AdDTO> result = adService.getAdsByTag(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(adRepository).findByTagId(1L, pageable);
    }

    @Test
    void searchAds_ReturnsPaginatedSearchResults() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.fullTextSearch(anyString(), any(Pageable.class))).thenReturn(adPage);

        // Act
        Page<AdDTO> result = adService.searchAds("test", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(adRepository).fullTextSearch("test", pageable);
    }

    @Test
    void advancedSearch_ReturnsPaginatedSearchResults() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);

        // Create search DTO
        AdSearchDTO searchDTO = AdSearchDTO.builder()
                .status(AdStatus.PUBLISHED)
                .categoryId(1L)
                .tagIds(new HashSet<>(Collections.singletonList(1L)))
                .minPrice(new BigDecimal("50.00"))
                .maxPrice(new BigDecimal("150.00"))
                .query("test")
                .featured(true)
                .creatorId(1L)
                .build();

        // Act
        Page<AdDTO> result = adService.advancedSearch(searchDTO, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(adRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void updateAdStatus_Success() {
        // Given
        testAd.setStatus(AdStatus.DRAFT); // Ensure we're testing from DRAFT to PUBLISHED
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(testAd));
        when(adRepository.save(any(Ad.class))).thenReturn(testAd);

        // Act
        AdDTO result = adService.updateAdStatus(testStatusUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(AdStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublicationDate());

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository).save(any(Ad.class));

        // Verify ad was updated with correct values
        ArgumentCaptor<Ad> adCaptor = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).save(adCaptor.capture());
        Ad capturedAd = adCaptor.getValue();
        assertEquals(AdStatus.PUBLISHED, capturedAd.getStatus());
        assertNotNull(capturedAd.getPublicationDate());
    }

    @Test
    void updateAdStatus_InvalidTransition_ThrowsException() {
        // Given - Set ad to DELETED, which can't transition to anything else
        testAd.setStatus(AdStatus.DELETED);
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(testAd));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                adService.updateAdStatus(testStatusUpdateDTO));

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository, never()).save(any(Ad.class));
    }

    @Test
    void incrementViews_Success() {
        // Given
        Ad mockAd = mock(Ad.class);
        when(mockAd.getViews()).thenReturn(0);

        when(adRepository.findById(1L)).thenReturn(Optional.of(mockAd));

        // Act - Should not throw exception
        assertDoesNotThrow(() -> adService.incrementViews(1L));

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(mockAd).setViews(1);
        verify(adRepository).save(mockAd);
    }

    @Test
    void incrementViews_AdNotFound_ThrowsException() {
        // Given
        when(adRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                adService.incrementViews(1L));

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository, never()).incrementViews(anyLong());
    }

    @Test
    void publishAd_Success() {
        // Given
        testAd.setStatus(AdStatus.DRAFT); // Ensure we're testing from DRAFT to PUBLISHED
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(testAd));
        when(adRepository.save(any(Ad.class))).thenReturn(testAd);
        LocalDateTime expirationDate = now.plusDays(30);

        // Act
        AdDTO result = adService.publishAd(1L, expirationDate);

        // Assert
        assertNotNull(result);
        assertEquals(AdStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublicationDate());
        assertEquals(expirationDate, result.getExpirationDate());

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository).save(any(Ad.class));
    }

    @Test
    void publishAd_InvalidTransition_ThrowsException() {
        // Given - Set ad to DELETED, which can't transition to PUBLISHED
        testAd.setStatus(AdStatus.DELETED);
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(testAd));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                adService.publishAd(1L, now.plusDays(30)));

        // Verify interactions
        verify(adRepository).findById(1L);
        verify(adRepository, never()).save(any(Ad.class));
    }

    @Test
    void findAdsExpiringSoon_ReturnsExpiringAds() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        when(adRepository.findByStatusAndExpirationDateBetween(any(AdStatus.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(ads);

        // Act
        List<AdDTO> result = adService.findAdsExpiringSoon(7);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAd.getId(), result.get(0).getId());

        // Verify interactions
        verify(adRepository).findByStatusAndExpirationDateBetween(
                eq(AdStatus.PUBLISHED),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void getFeaturedAds_ReturnsPaginatedFeaturedAds() {
        // Given
        testAd.setFeatured(true);
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findByFeaturedTrueAndStatus(any(AdStatus.class), any(Pageable.class))).thenReturn(adPage);

        // Act
        Page<AdDTO> result = adService.getFeaturedAds(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());
        assertTrue(result.getContent().get(0).getFeatured());

        // Verify interactions
        verify(adRepository).findByFeaturedTrueAndStatus(AdStatus.PUBLISHED, pageable);
    }

    @Test
    void markExpiredAds_UpdatesExpiredAds() {
        // Given
        when(adRepository.updateExpiredAds(any(LocalDateTime.class))).thenReturn(5);

        // Act
        int result = adService.markExpiredAds();

        // Assert
        assertEquals(5, result);

        // Verify interactions
        verify(adRepository).updateExpiredAds(any(LocalDateTime.class));
    }

    @Test
    void getAdsSummary_ReturnsPaginatedAdSummaries() {
        // Given
        List<Ad> ads = Collections.singletonList(testAd);
        Page<Ad> adPage = new PageImpl<>(ads, pageable, 1);
        when(adRepository.findByStatus(any(AdStatus.class), any(Pageable.class))).thenReturn(adPage);

        // Act
        Page<AdSummaryDTO> result = adService.getAdsSummary(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAd.getId(), result.getContent().get(0).getId());
        assertEquals(testAd.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(testAd.getPrice(), result.getContent().get(0).getPrice());

        // Verify interactions
        verify(adRepository).findByStatus(AdStatus.PUBLISHED, pageable);
    }
}