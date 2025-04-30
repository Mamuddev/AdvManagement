package it.mahmoud.advmanagement.controller;

import it.mahmoud.advmanagement.dto.category.CategoryCreateDTO;
import it.mahmoud.advmanagement.dto.category.CategoryDTO;
import it.mahmoud.advmanagement.dto.category.CategorySelectDTO;
import it.mahmoud.advmanagement.dto.response.ApiResponseDTO;
import it.mahmoud.advmanagement.dto.response.PageMetaDTO;
import it.mahmoud.advmanagement.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category operations
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Create a new category
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> createCategory(@RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryCreateDTO);
        return new ResponseEntity<>(
                ApiResponseDTO.success(createdCategory, "Category created successfully"),
                HttpStatus.CREATED);
    }

    /**
     * Update an existing category
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryCreateDTO);
        return ResponseEntity.ok(
                ApiResponseDTO.success(updatedCategory, "Category updated successfully"));
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(category, "Category retrieved successfully"));
    }

    /**
     * Get category by name
     */
    @GetMapping("/by-name")
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> getCategoryByName(@RequestParam String name) {
        CategoryDTO category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(ApiResponseDTO.success(category, "Category retrieved successfully"));
    }

    /**
     * Check if category exists by name
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponseDTO<Boolean>> categoryExists(@RequestParam String name) {
        boolean exists = categoryService.categoryExists(name);
        return ResponseEntity.ok(ApiResponseDTO.success(exists, "Category existence checked"));
    }

    /**
     * Delete a category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Category deleted successfully"));
    }

    /**
     * Get all categories (paginated)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<CategoryDTO>>> getAllCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        PageMetaDTO pageMeta = createPageMeta(categories);
        return ResponseEntity.ok(ApiResponseDTO.success(categories, pageMeta));
    }

    /**
     * Get top-level categories
     */
    @GetMapping("/top-level")
    public ResponseEntity<ApiResponseDTO<List<CategoryDTO>>> getTopLevelCategories() {
        List<CategoryDTO> categories = categoryService.getTopLevelCategories();
        return ResponseEntity.ok(
                ApiResponseDTO.success(categories, "Top-level categories retrieved successfully"));
    }

    /**
     * Get subcategories by parent ID
     */
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<ApiResponseDTO<List<CategoryDTO>>> getSubcategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryDTO> subcategories = categoryService.getSubcategoriesByParentId(parentId);
        return ResponseEntity.ok(
                ApiResponseDTO.success(subcategories, "Subcategories retrieved successfully"));
    }

    /**
     * Search categories by term
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<Page<CategoryDTO>>> searchCategories(
            @RequestParam String term,
            Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.searchCategories(term, pageable);
        PageMetaDTO pageMeta = createPageMeta(categories);
        return ResponseEntity.ok(
                ApiResponseDTO.success(categories, pageMeta));
    }

    /**
     * Get category path (breadcrumbs)
     */
    @GetMapping("/{categoryId}/path")
    public ResponseEntity<ApiResponseDTO<List<CategoryDTO>>> getCategoryPath(@PathVariable Long categoryId) {
        List<CategoryDTO> path = categoryService.getCategoryPath(categoryId);
        return ResponseEntity.ok(ApiResponseDTO.success(path, "Category path retrieved successfully"));
    }

    /**
     * Get category select list for dropdowns
     */
    @GetMapping("/select-list")
    public ResponseEntity<ApiResponseDTO<List<CategorySelectDTO>>> getCategorySelectList() {
        List<CategorySelectDTO> selectList = categoryService.getCategorySelectList();
        return ResponseEntity.ok(
                ApiResponseDTO.success(selectList, "Category select list retrieved successfully"));
    }

    /**
     * Count ads in a category
     */
    @GetMapping("/{categoryId}/count-ads")
    public ResponseEntity<ApiResponseDTO<Long>> countAdsByCategory(@PathVariable Long categoryId) {
        long count = categoryService.countAdsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponseDTO.success(count, "Ad count retrieved successfully"));
    }

    /**
     * Count ads in a category and all its subcategories
     */
    @GetMapping("/{categoryId}/count-ads-hierarchy")
    public ResponseEntity<ApiResponseDTO<Long>> countAdsByCategoryHierarchy(@PathVariable Long categoryId) {
        long count = categoryService.countAdsByCategoryHierarchy(categoryId);
        return ResponseEntity.ok(
                ApiResponseDTO.success(count, "Hierarchical ad count retrieved successfully"));
    }

    /**
     * Get categories with most ads
     */
    @GetMapping("/most-ads")
    public ResponseEntity<ApiResponseDTO<Page<CategoryDTO>>> getCategoriesWithMostAds(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.getCategoriesWithMostAds(pageable);
        PageMetaDTO pageMeta = createPageMeta(categories);
        return ResponseEntity.ok(
                ApiResponseDTO.success(categories, pageMeta));
    }

    /**
     * Get unused categories
     */
    @GetMapping("/unused")
    public ResponseEntity<ApiResponseDTO<Page<CategoryDTO>>> getUnusedCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.getUnusedCategories(pageable);
        PageMetaDTO pageMeta = createPageMeta(categories);
        return ResponseEntity.ok(
                ApiResponseDTO.success(categories, pageMeta));
    }

    /**
     * Get complete category tree
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponseDTO<List<CategoryDTO>>> getCategoryTree() {
        List<CategoryDTO> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(
                ApiResponseDTO.success(tree, "Category tree retrieved successfully"));
    }

    /**
     * Move a category to a new parent
     */
    @PutMapping("/{categoryId}/move")
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> moveCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long newParentId) {
        CategoryDTO movedCategory = categoryService.moveCategory(categoryId, newParentId);
        return ResponseEntity.ok(
                ApiResponseDTO.success(movedCategory, "Category moved successfully"));
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