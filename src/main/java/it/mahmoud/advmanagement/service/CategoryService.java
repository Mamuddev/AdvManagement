package it.mahmoud.advmanagement.service;

import it.mahmoud.advmanagement.dto.category.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for operations on Category entities
 */
public interface CategoryService {

    /**
     * Create a new category
     * @param categoryCreateDTO Category creation data
     * @return Created category data
     */
    CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO);

    /**
     * Update an existing category
     * @param id Category ID
     * @param categoryCreateDTO Category update data
     * @return Updated category data
     */
    CategoryDTO updateCategory(Long id, CategoryCreateDTO categoryCreateDTO);

    /**
     * Get category by ID
     * @param id Category ID
     * @return Category data
     */
    CategoryDTO getCategoryById(Long id);

    /**
     * Get category by name
     * @param name Category name
     * @return Category data
     */
    CategoryDTO getCategoryByName(String name);

    /**
     * Check if category with given name exists
     * @param name Category name
     * @return true if exists
     */
    boolean categoryExists(String name);

    /**
     * Delete a category
     * @param id Category ID
     */
    void deleteCategory(Long id);

    /**
     * Get all categories with pagination
     * @param pageable Pagination information
     * @return Page of categories
     */
    Page<CategoryDTO> getAllCategories(Pageable pageable);

    /**
     * Get top-level categories (no parent)
     * @return List of top-level categories
     */
    List<CategoryDTO> getTopLevelCategories();

    /**
     * Get subcategories of a given parent category
     * @param parentId Parent category ID
     * @return List of subcategories
     */
    List<CategoryDTO> getSubcategoriesByParentId(Long parentId);

    /**
     * Search categories by name
     * @param searchTerm Search term
     * @param pageable Pagination information
     * @return Page of matching categories
     */
    Page<CategoryDTO> searchCategories(String searchTerm, Pageable pageable);

    /**
     * Get the hierarchical path to a category
     * @param categoryId Category ID
     * @return List of categories in the path from root to target
     */
    List<CategoryDTO> getCategoryPath(Long categoryId);

    /**
     * Get simplified category list for dropdowns
     * @return List of simplified categories
     */
    List<CategorySelectDTO> getCategorySelectList();

    /**
     * Count ads in a category
     * @param categoryId Category ID
     * @return Number of ads in the category
     */
    long countAdsByCategory(Long categoryId);

    /**
     * Count ads in a category and its subcategories
     * @param categoryId Category ID
     * @return Number of ads in the category hierarchy
     */
    long countAdsByCategoryHierarchy(Long categoryId);

    /**
     * Get categories with most ads
     * @param pageable Pagination information
     * @return Page of categories ordered by ad count
     */
    Page<CategoryDTO> getCategoriesWithMostAds(Pageable pageable);

    /**
     * Get unused categories (no ads)
     * @param pageable Pagination information
     * @return Page of unused categories
     */
    Page<CategoryDTO> getUnusedCategories(Pageable pageable);

    /**
     * Get complete category tree
     * @return Hierarchical list of categories
     */
    List<CategoryDTO> getCategoryTree();

    /**
     * Move a category under a new parent
     * @param categoryId Category ID to move
     * @param newParentId New parent category ID (null for top level)
     * @return Updated category data
     */
    CategoryDTO moveCategory(Long categoryId, Long newParentId);
}