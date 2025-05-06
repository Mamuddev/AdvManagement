package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.dto.category.CategoryCreateDTO;
import it.mahmoud.advmanagement.dto.category.CategoryDTO;
import it.mahmoud.advmanagement.dto.category.CategorySelectDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.Category;
import it.mahmoud.advmanagement.repo.CategoryRepository;
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
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private Category parentCategory;
    private CategoryCreateDTO testCategoryCreateDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Set up parent category
        parentCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .ads(new HashSet<>())
                .subcategories(new HashSet<>())
                .build();

        // Set up test category
        testCategory = Category.builder()
                .id(2L)
                .name("Smartphones")
                .description("Mobile phones and accessories")
                .parentCategory(parentCategory)
                .ads(new HashSet<>())
                .subcategories(new HashSet<>())
                .build();

        // Add test category as subcategory to parent
        parentCategory.getSubcategories().add(testCategory);

        // Set up test category create DTO
        testCategoryCreateDTO = CategoryCreateDTO.builder()
                .name("Laptops")
                .description("Portable computers")
                .parentCategoryId(1L)
                .build();

        // Set up pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(3L);
            return category;
        });

        // Act
        CategoryDTO result = categoryService.createCategory(testCategoryCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testCategoryCreateDTO.getName(), result.getName());
        assertEquals(testCategoryCreateDTO.getDescription(), result.getDescription());
        assertEquals(parentCategory.getId(), result.getParentCategoryId());
        assertEquals(parentCategory.getName(), result.getParentCategoryName());

        // Verify interactions
        verify(categoryRepository).existsByNameIgnoreCase(testCategoryCreateDTO.getName());
        verify(categoryRepository).findById(testCategoryCreateDTO.getParentCategoryId());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WithoutParent_Success() {
        // Arrange
        testCategoryCreateDTO.setParentCategoryId(null);
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(3L);
            return category;
        });

        // Act
        CategoryDTO result = categoryService.createCategory(testCategoryCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testCategoryCreateDTO.getName(), result.getName());
        assertEquals(testCategoryCreateDTO.getDescription(), result.getDescription());
        assertNull(result.getParentCategoryId());
        assertNull(result.getParentCategoryName());

        // Verify interactions
        verify(categoryRepository).existsByNameIgnoreCase(testCategoryCreateDTO.getName());
        verify(categoryRepository, never()).findById(anyLong());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                categoryService.createCategory(testCategoryCreateDTO));

        // Verify interactions
        verify(categoryRepository).existsByNameIgnoreCase(testCategoryCreateDTO.getName());
        verify(categoryRepository, never()).findById(anyLong());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_ParentNotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.createCategory(testCategoryCreateDTO));

        // Verify interactions
        verify(categoryRepository).existsByNameIgnoreCase(testCategoryCreateDTO.getName());
        verify(categoryRepository).findById(testCategoryCreateDTO.getParentCategoryId());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        Long categoryId = 2L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Create update DTO
        CategoryCreateDTO updateDTO = CategoryCreateDTO.builder()
                .name("Updated Smartphones")
                .description("Updated description")
                .parentCategoryId(1L)
                .build();

        // Act
        CategoryDTO result = categoryService.updateCategory(categoryId, updateDTO);

        // Assert
        assertNotNull(result);

        // Verify category was updated with correct values
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();
        assertEquals(updateDTO.getName(), capturedCategory.getName());
        assertEquals(updateDTO.getDescription(), capturedCategory.getDescription());
        assertEquals(updateDTO.getParentCategoryId(), capturedCategory.getParentCategory().getId());
    }

    @Test
    void updateCategory_DuplicateName_ThrowsException() {
        // Arrange
        Long categoryId = 2L;
        Category existingCategory = Category.builder()
                .id(3L) // Different ID
                .name("Updated Smartphones")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(existingCategory));

        // Create update DTO with a name that already exists for another category
        CategoryCreateDTO updateDTO = CategoryCreateDTO.builder()
                .name("Updated Smartphones")
                .description("Updated description")
                .parentCategoryId(1L)
                .build();

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                categoryService.updateCategory(categoryId, updateDTO));

        // Verify interactions
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).findByNameIgnoreCase(updateDTO.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_CircularDependency_ThrowsException() {
        // Arrange
        Long categoryId = 1L; // Parent ID
        Long newParentId = 2L; // Child ID as new parent

        // Set up parent category
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(newParentId)).thenReturn(Optional.of(testCategory));

        // Create update DTO that would cause circular dependency
        CategoryCreateDTO updateDTO = CategoryCreateDTO.builder()
                .name("Updated Electronics")
                .description("Updated description")
                .parentCategoryId(newParentId) // Try to make child the parent of its own parent
                .build();

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                categoryService.updateCategory(categoryId, updateDTO));

        // Verify interactions
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).findByNameIgnoreCase(updateDTO.getName());
        verify(categoryRepository, times(2)).findById(newParentId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));

        // Act
        CategoryDTO result = categoryService.getCategoryById(2L);

        // Assert
        assertNotNull(result);
        assertEquals(testCategory.getId(), result.getId());
        assertEquals(testCategory.getName(), result.getName());
        assertEquals(testCategory.getDescription(), result.getDescription());
        assertEquals(parentCategory.getId(), result.getParentCategoryId());
        assertEquals(parentCategory.getName(), result.getParentCategoryName());

        // Verify interactions
        verify(categoryRepository).findById(2L);
    }

    @Test
    void getCategoryById_CategoryNotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryById(2L));

        // Verify interactions
        verify(categoryRepository).findById(2L);
    }

    @Test
    void getCategoryByName_Success() {
        // Arrange
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(testCategory));

        // Act
        CategoryDTO result = categoryService.getCategoryByName("Smartphones");

        // Assert
        assertNotNull(result);
        assertEquals(testCategory.getId(), result.getId());
        assertEquals(testCategory.getName(), result.getName());

        // Verify interactions
        verify(categoryRepository).findByNameIgnoreCase("Smartphones");
    }

    @Test
    void getCategoryByName_CategoryNotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryByName("NonExistentCategory"));

        // Verify interactions
        verify(categoryRepository).findByNameIgnoreCase("NonExistentCategory");
    }

    @Test
    void categoryExists_ReturnsTrueWhenExists() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // Act
        boolean result = categoryService.categoryExists("Electronics");

        // Assert
        assertTrue(result);

        // Verify interactions
        verify(categoryRepository).existsByNameIgnoreCase("Electronics");
    }

    @Test
    void categoryExists_ReturnsFalseWhenNotExists() {
        // Arrange
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);

        // Act
        boolean result = categoryService.categoryExists("NonExistentCategory");

        // Assert
        assertFalse(result);

        // Verify interactions
        verify(categoryRepository).existsByNameIgnoreCase("NonExistentCategory");
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        // Create a category with no subcategories and no ads
        Category categoryToDelete = Category.builder()
                .id(3L)
                .name("Category to delete")
                .description("Will be deleted")
                .subcategories(new HashSet<>())
                .ads(new HashSet<>())
                .build();

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryToDelete));
        doNothing().when(categoryRepository).delete(any(Category.class));

        // Act - Should not throw exception
        assertDoesNotThrow(() -> categoryService.deleteCategory(3L));

        // Verify interactions
        verify(categoryRepository).findById(3L);
        verify(categoryRepository).delete(categoryToDelete);
    }

    @Test
    void deleteCategory_WithSubcategories_ThrowsException() {
        // Arrange
        Long categoryId = 1L;

        // Mock the category
        Category category = mock(Category.class);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // IMPORTANT: Create a real list with at least one subcategory
        List<Category> subcategories = new ArrayList<>();
        subcategories.add(new Category()); // Add at least one item

        // This is critical - force the method to return our non-empty list
        when(categoryRepository.findByParentCategoryId(eq(categoryId), any(Sort.class)))
                .thenReturn(subcategories);

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                categoryService.deleteCategory(categoryId));
    }

    @Test
    void deleteCategory_WithAds_ThrowsException() {
        // Arrange
        // Add an ad to the test category
        Ad ad = new Ad();
        ad.setId(1L);
        ad.setTitle("Test Ad");
        testCategory.getAds().add(ad);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                categoryService.deleteCategory(2L));

        // Verify interactions
        verify(categoryRepository).findById(2L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void getAllCategories_ReturnsPaginatedCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(parentCategory, testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 2);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // Act
        Page<CategoryDTO> result = categoryService.getAllCategories(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // Verify interactions
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void getTopLevelCategories_ReturnsTopLevelCategories() {
        // Arrange
        List<Category> topLevelCategories = Collections.singletonList(parentCategory);
        when(categoryRepository.findByParentCategoryIsNull(any(Sort.class))).thenReturn(topLevelCategories);

        // Act
        List<CategoryDTO> result = categoryService.getTopLevelCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(parentCategory.getId(), result.get(0).getId());
        assertEquals(parentCategory.getName(), result.get(0).getName());

        // Verify interactions
        verify(categoryRepository).findByParentCategoryIsNull(any(Sort.class));
    }

    @Test
    void getSubcategoriesByParentId_ReturnsSubcategories() {
        // Arrange
        List<Category> subcategories = Collections.singletonList(testCategory);
        when(categoryRepository.findByParentCategoryId(anyLong(), any(Sort.class))).thenReturn(subcategories);

        // Act
        List<CategoryDTO> result = categoryService.getSubcategoriesByParentId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory.getId(), result.get(0).getId());
        assertEquals(testCategory.getName(), result.get(0).getName());

        // Verify interactions
        verify(categoryRepository).findByParentCategoryId(eq(1L), any(Sort.class));
    }

    @Test
    void searchCategories_ReturnsPaginatedSearchResults() {
        // Arrange
        List<Category> categories = Collections.singletonList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 1);
        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(categoryPage);

        // Act
        Page<CategoryDTO> result = categoryService.searchCategories("smart", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getCategoryPath_ReturnsPathFromRootToCategory() {
        // Arrange
        List<Object[]> pathData = new ArrayList<>();
        pathData.add(new Object[]{1L, "Electronics", null}); // Root category
        pathData.add(new Object[]{2L, "Smartphones", 1L}); // Child category

        when(categoryRepository.findCategoryPath(anyLong())).thenReturn(pathData);

        // Act
        List<CategoryDTO> result = categoryService.getCategoryPath(2L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check first category in path (root)
        assertEquals(1L, result.get(0).getId());
        assertEquals("Electronics", result.get(0).getName());
        assertNull(result.get(0).getParentCategoryId());

        // Check second category in path (child)
        assertEquals(2L, result.get(1).getId());
        assertEquals("Smartphones", result.get(1).getName());
        assertEquals(1L, result.get(1).getParentCategoryId());

        // Verify interactions
        verify(categoryRepository).findCategoryPath(2L);
    }

    @Test
    void getCategoryPath_CategoryNotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.findCategoryPath(anyLong())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryPath(999L));

        // Verify interactions
        verify(categoryRepository).findCategoryPath(999L);
    }

    @Test
    void getCategorySelectList_ReturnsFormattedList() {
        // Arrange
        List<Category> categories = Arrays.asList(parentCategory, testCategory);
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(categories);

        // Act
        List<CategorySelectDTO> result = categoryService.getCategorySelectList();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check parent category
        assertEquals(parentCategory.getId(), result.get(0).getId());
        assertEquals(parentCategory.getName(), result.get(0).getName());
        assertNull(result.get(0).getParentId());
        assertTrue(result.get(0).getHasSubcategories());

        // Check child category
        assertEquals(testCategory.getId(), result.get(1).getId());
        assertEquals(testCategory.getName(), result.get(1).getName());
        assertEquals(parentCategory.getId(), result.get(1).getParentId());
        assertFalse(result.get(1).getHasSubcategories());

        // Verify interactions
        verify(categoryRepository).findAll(any(Sort.class));
    }

    @Test
    void countAdsByCategory_ReturnsCorrectCount() {
        // Arrange
        // Add ads to the test category
        Ad ad1 = new Ad();
        ad1.setId(1L);
        ad1.setTitle("Test Ad 1");

        Ad ad2 = new Ad();
        ad2.setId(2L);
        ad2.setTitle("Test Ad 2");

        testCategory.getAds().add(ad1);
        testCategory.getAds().add(ad2);

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));

        // Act
        long result = categoryService.countAdsByCategory(2L);

        // Assert
        assertEquals(2, result);

        // Verify interactions
        verify(categoryRepository).findById(2L);
    }

    @Test
    void countAdsByCategoryHierarchy_ReturnsCorrectCount() {
        // Arrange
        when(categoryRepository.existsById(anyLong())).thenReturn(true);
        when(categoryRepository.countAdsByCategoryHierarchy(anyLong())).thenReturn(5L);

        // Act
        long result = categoryService.countAdsByCategoryHierarchy(1L);

        // Assert
        assertEquals(5L, result);

        // Verify interactions
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).countAdsByCategoryHierarchy(1L);
    }

    @Test
    void countAdsByCategoryHierarchy_CategoryNotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.countAdsByCategoryHierarchy(999L));

        // Verify interactions
        verify(categoryRepository).existsById(999L);
        verify(categoryRepository, never()).countAdsByCategoryHierarchy(anyLong());
    }

    @Test
    void getCategoriesWithMostAds_ReturnsPaginatedResults() {
        // Arrange
        List<Category> categories = Collections.singletonList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 1);
        when(categoryRepository.findCategoriesWithMostAds(any(Pageable.class))).thenReturn(categoryPage);

        // Act
        Page<CategoryDTO> result = categoryService.getCategoriesWithMostAds(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(categoryRepository).findCategoriesWithMostAds(pageable);
    }

    @Test
    void getUnusedCategories_ReturnsPaginatedResults() {
        // Arrange
        List<Category> categories = Collections.singletonList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 1);
        when(categoryRepository.findUnusedCategories(any(Pageable.class))).thenReturn(categoryPage);

        // Act
        Page<CategoryDTO> result = categoryService.getUnusedCategories(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(categoryRepository).findUnusedCategories(pageable);
    }

    @Test
    void getCategoryTree_ReturnsHierarchicalStructure() {
        // Arrange
        List<Category> allCategories = Arrays.asList(parentCategory, testCategory);
        when(categoryRepository.findAll()).thenReturn(allCategories);

        // Act
        List<CategoryDTO> result = categoryService.getCategoryTree();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only top-level categories are returned directly

        CategoryDTO rootCategory = result.get(0);
        assertEquals(parentCategory.getId(), rootCategory.getId());
        assertEquals(parentCategory.getName(), rootCategory.getName());
        assertNotNull(rootCategory.getSubcategories());
        assertEquals(1, rootCategory.getSubcategories().size());

        CategoryDTO childCategory = rootCategory.getSubcategories().iterator().next();
        assertEquals(testCategory.getId(), childCategory.getId());
        assertEquals(testCategory.getName(), childCategory.getName());

        // Verify interactions
        verify(categoryRepository).findAll();
    }

    @Test
    void moveCategory_Success() {
        // Arrange
        Category category3 = Category.builder()
                .id(3L)
                .name("Tablets")
                .description("Tablet devices")
                .parentCategory(null)
                .subcategories(new HashSet<>())
                .ads(new HashSet<>())
                .build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category3));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act - Move category from parent 1 to parent 3
        CategoryDTO result = categoryService.moveCategory(2L, 3L);

        // Assert
        assertNotNull(result);

        // Verify category was updated with correct parent
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();
        assertEquals(category3, capturedCategory.getParentCategory());
    }

    @Test
    void moveCategory_ToTopLevel_Success() {
        // Arrange
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act - Move category to top level (null parent)
        CategoryDTO result = categoryService.moveCategory(2L, null);

        // Assert
        assertNotNull(result);

        // Verify category was updated with null parent
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();
        assertNull(capturedCategory.getParentCategory());
    }

    @Test
    void moveCategory_CircularDependency_ThrowsException() {
        // Arrange - Try to make a category its own parent
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                categoryService.moveCategory(2L, 2L));

        // Verify interactions
        verify(categoryRepository).findById(2L);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}