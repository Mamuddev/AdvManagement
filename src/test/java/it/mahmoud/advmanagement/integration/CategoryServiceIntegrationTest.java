package it.mahmoud.advmanagement.integration;

import it.mahmoud.advmanagement.dto.ad.AdCreateDTO;
import it.mahmoud.advmanagement.dto.category.CategoryCreateDTO;
import it.mahmoud.advmanagement.dto.category.CategoryDTO;
import it.mahmoud.advmanagement.dto.category.CategorySelectDTO;
import it.mahmoud.advmanagement.dto.user.UserCreateDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.repo.AdRepository;
import it.mahmoud.advmanagement.repo.CategoryRepository;
import it.mahmoud.advmanagement.repo.UserRepository;
import it.mahmoud.advmanagement.service.AdService;
import it.mahmoud.advmanagement.service.CategoryService;
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
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CategoryService.
 *
 * Note: This uses an actual database (H2 in-memory) and real Spring context.
 * The @ActiveProfiles("test") ensures we use test configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // Each test runs in a transaction that is rolled back at the end
public class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdService adService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    private CategoryCreateDTO parentCategoryCreateDTO;
    private CategoryCreateDTO childCategoryCreateDTO;
    private Long userId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Clean repositories
        adRepository.deleteAll();
        categoryRepository.deleteAll();
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

        // Set up parent category DTO
        parentCategoryCreateDTO = CategoryCreateDTO.builder()
                .name("Electronics")
                .description("Electronic devices and accessories")
                .parentCategoryId(null)  // Top level
                .build();

        // Set up child category DTO
        childCategoryCreateDTO = CategoryCreateDTO.builder()
                .name("Smartphones")
                .description("Mobile phones and accessories")
                .parentCategoryId(null)  // Will be set after parent is created
                .build();

        // Set up pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createCategory_Success() {
        // Act
        CategoryDTO result = categoryService.createCategory(parentCategoryCreateDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(parentCategoryCreateDTO.getName(), result.getName());
        assertEquals(parentCategoryCreateDTO.getDescription(), result.getDescription());
        assertNull(result.getParentCategoryId());
        assertNull(result.getParentCategoryName());
    }

    @Test
    void createCategory_WithParent_Success() {
        // Given - Create parent category first
        CategoryDTO parentCategory = categoryService.createCategory(parentCategoryCreateDTO);
        childCategoryCreateDTO.setParentCategoryId(parentCategory.getId());

        // Act
        CategoryDTO result = categoryService.createCategory(childCategoryCreateDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(childCategoryCreateDTO.getName(), result.getName());
        assertEquals(childCategoryCreateDTO.getDescription(), result.getDescription());
        assertEquals(parentCategory.getId(), result.getParentCategoryId());
        assertEquals(parentCategory.getName(), result.getParentCategoryName());
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        // Given - Create category first
        categoryService.createCategory(parentCategoryCreateDTO);

        // Create another category with the same name
        CategoryCreateDTO duplicateCategory = CategoryCreateDTO.builder()
                .name("Electronics")  // Same name as parentCategoryCreateDTO
                .description("Different description")
                .build();

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                categoryService.createCategory(duplicateCategory));
    }

    @Test
    void createCategory_ParentNotFound_ThrowsException() {
        // Given - Set non-existent parent ID
        childCategoryCreateDTO.setParentCategoryId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.createCategory(childCategoryCreateDTO));
    }

    @Test
    void updateCategory_Success() {
        // Given - Create category first
        CategoryDTO category = categoryService.createCategory(parentCategoryCreateDTO);

        // Create update DTO
        CategoryCreateDTO updateDTO = CategoryCreateDTO.builder()
                .name("Updated Electronics")
                .description("Updated description")
                .parentCategoryId(null)
                .build();

        // Act
        CategoryDTO result = categoryService.updateCategory(category.getId(), updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getDescription(), result.getDescription());
    }

    @Test
    void updateCategory_DuplicateName_ThrowsException() {
        // Given - Create two categories
        CategoryDTO category1 = categoryService.createCategory(parentCategoryCreateDTO);

        CategoryCreateDTO category2DTO = CategoryCreateDTO.builder()
                .name("Computers")
                .description("Desktop and laptop computers")
                .build();
        CategoryDTO category2 = categoryService.createCategory(category2DTO);

        // Create update DTO with name of first category
        CategoryCreateDTO updateDTO = CategoryCreateDTO.builder()
                .name("Electronics")  // Same as category1
                .description("Updated description")
                .build();

        // Act & Assert - Try to update category2 with category1's name
        assertThrows(DuplicateResourceException.class, () ->
                categoryService.updateCategory(category2.getId(), updateDTO));
    }

    @Test
    void updateCategory_CircularDependency_ThrowsException() {
        // Given - Create parent and child categories
        CategoryDTO parentCategory = categoryService.createCategory(parentCategoryCreateDTO);
        childCategoryCreateDTO.setParentCategoryId(parentCategory.getId());
        CategoryDTO childCategory = categoryService.createCategory(childCategoryCreateDTO);

        // Create update DTO that would make parent a child of its own child
        CategoryCreateDTO updateDTO = CategoryCreateDTO.builder()
                .name("Updated Electronics")
                .description("Updated description")
                .parentCategoryId(childCategory.getId())  // Child becomes parent of its own parent
                .build();

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                categoryService.updateCategory(parentCategory.getId(), updateDTO));
    }

    @Test
    void getCategoryById_Success() {
        // Given - Create category first
        CategoryDTO createdCategory = categoryService.createCategory(parentCategoryCreateDTO);

        // Act
        CategoryDTO result = categoryService.getCategoryById(createdCategory.getId());

        // Assert
        assertNotNull(result);
        assertEquals(createdCategory.getId(), result.getId());
        assertEquals(createdCategory.getName(), result.getName());
    }

    @Test
    void getCategoryById_CategoryNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryById(999L));
    }

    @Test
    void getCategoryByName_Success() {
        // Given - Create category first
        categoryService.createCategory(parentCategoryCreateDTO);

        // Act
        CategoryDTO result = categoryService.getCategoryByName("Electronics");

        // Assert
        assertNotNull(result);
        assertEquals(parentCategoryCreateDTO.getName(), result.getName());
    }

    @Test
    void getCategoryByName_CategoryNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryByName("NonExistentCategory"));
    }

    @Test
    void categoryExists_ReturnsTrueWhenExists() {
        // Given - Create category first
        categoryService.createCategory(parentCategoryCreateDTO);

        // Act
        boolean result = categoryService.categoryExists("Electronics");

        // Assert
        assertTrue(result);
    }

    @Test
    void categoryExists_ReturnsFalseWhenNotExists() {
        // Act
        boolean result = categoryService.categoryExists("NonExistentCategory");

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteCategory_Success() {
        // Given - Create category first
        CategoryDTO category = categoryService.createCategory(parentCategoryCreateDTO);

        // Act - Should not throw exception
        assertDoesNotThrow(() -> categoryService.deleteCategory(category.getId()));

        // Assert - Category should no longer exist
        assertFalse(categoryRepository.existsById(category.getId()));
    }

    @Test
    void deleteCategory_WithSubcategories_ThrowsException() {
        // Given - Create parent and child categories
        CategoryDTO parentCategory = categoryService.createCategory(parentCategoryCreateDTO);

        childCategoryCreateDTO.setParentCategoryId(parentCategory.getId());
        CategoryDTO childCategory = categoryService.createCategory(childCategoryCreateDTO);

        // Forza la persistenza e il flush del database per assicurarsi che la relazione sia salvata
        categoryRepository.flush();

        // Opzionale: verifica che la relazione sia effettivamente configurata correttamente
        List<CategoryDTO> subcategories = categoryService.getSubcategoriesByParentId(parentCategory.getId());
        assertFalse(subcategories.isEmpty(), "Parent category should have subcategories");

        // Act & Assert - Try to delete parent
        assertThrows(InvalidOperationException.class, () ->
                categoryService.deleteCategory(parentCategory.getId()));
    }

    @Test
    void deleteCategory_WithAds_ThrowsException() {
        // Given - Create category and ad using it
        CategoryDTO category = categoryService.createCategory(parentCategoryCreateDTO);

        // Create an ad with this category
        AdCreateDTO adCreateDTO = AdCreateDTO.builder()
                .title("Test Ad")
                .description("This is a test ad with sufficient length to pass validation")
                .price(new BigDecimal("99.99"))
                .creatorId(userId)
                .categoryIds(new HashSet<>())
                .status(AdStatus.DRAFT)
                .build();
        adCreateDTO.getCategoryIds().add(category.getId());
        adService.createAd(adCreateDTO);

        // Act & Assert - Try to delete category
        assertThrows(InvalidOperationException.class, () ->
                categoryService.deleteCategory(category.getId()));
    }

    @Test
    void getAllCategories_ReturnsPaginatedCategories() {
        // Given - Create multiple categories
        categoryService.createCategory(parentCategoryCreateDTO);

        CategoryCreateDTO category2DTO = CategoryCreateDTO.builder()
                .name("Computers")
                .description("Desktop and laptop computers")
                .build();
        categoryService.createCategory(category2DTO);

        // Act
        Page<CategoryDTO> result = categoryService.getAllCategories(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getTopLevelCategories_ReturnsTopLevelCategories() {
        // Given - Create parent and child categories
        CategoryDTO parentCategory = categoryService.createCategory(parentCategoryCreateDTO);
        childCategoryCreateDTO.setParentCategoryId(parentCategory.getId());
        categoryService.createCategory(childCategoryCreateDTO);

        // Act
        List<CategoryDTO> result = categoryService.getTopLevelCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(parentCategory.getName(), result.get(0).getName());
    }

    @Test
    void getSubcategoriesByParentId_ReturnsSubcategories() {
        // Given - Create parent and child categories
        CategoryDTO parentCategory = categoryService.createCategory(parentCategoryCreateDTO);
        childCategoryCreateDTO.setParentCategoryId(parentCategory.getId());
        CategoryDTO childCategory = categoryService.createCategory(childCategoryCreateDTO);

        // Act
        List<CategoryDTO> result = categoryService.getSubcategoriesByParentId(parentCategory.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(childCategory.getName(), result.get(0).getName());
    }

    @Test
    void searchCategories_ReturnsPaginatedSearchResults() {
        // Given - Create categories
        categoryService.createCategory(parentCategoryCreateDTO);
        childCategoryCreateDTO.setParentCategoryId(null);
        categoryService.createCategory(childCategoryCreateDTO);

        // Act - Search for "phone" (should match "Smartphones")
        Page<CategoryDTO> result = categoryService.searchCategories("phone", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Smartphones", result.getContent().get(0).getName());
    }


    @Test
    void getCategorySelectList_ReturnsFormattedList() {
        // Given - Create parent and child categories
        CategoryDTO parentCategory = categoryService.createCategory(parentCategoryCreateDTO);
        childCategoryCreateDTO.setParentCategoryId(parentCategory.getId());
        categoryService.createCategory(childCategoryCreateDTO);

        // Act
        List<CategorySelectDTO> result = categoryService.getCategorySelectList();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Find parent in the list
        CategorySelectDTO parentDTO = result.stream()
                .filter(c -> c.getName().equals("Electronics"))
                .findFirst()
                .orElse(null);
        assertNotNull(parentDTO);
        assertTrue(parentDTO.getHasSubcategories());

        // Find child in the list
        CategorySelectDTO childDTO = result.stream()
                .filter(c -> c.getName().equals("Smartphones"))
                .findFirst()
                .orElse(null);
        assertNotNull(childDTO);
        assertEquals(parentCategory.getId(), childDTO.getParentId());
    }

    @Test
    void moveCategory_Success() {
        // Given - Create two top-level categories and one child
        CategoryDTO parentCategory1 = categoryService.createCategory(parentCategoryCreateDTO);

        CategoryCreateDTO category2DTO = CategoryCreateDTO.builder()
                .name("Computers")
                .description("Desktop and laptop computers")
                .build();
        CategoryDTO parentCategory2 = categoryService.createCategory(category2DTO);

        childCategoryCreateDTO.setParentCategoryId(parentCategory1.getId());
        CategoryDTO childCategory = categoryService.createCategory(childCategoryCreateDTO);

        // Act - Move child from first parent to second parent
        CategoryDTO result = categoryService.moveCategory(childCategory.getId(), parentCategory2.getId());

        // Assert
        assertNotNull(result);
        assertEquals(parentCategory2.getId(), result.getParentCategoryId());
        assertEquals(parentCategory2.getName(), result.getParentCategoryName());
    }
}