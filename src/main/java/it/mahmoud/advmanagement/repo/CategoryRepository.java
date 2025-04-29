package it.mahmoud.advmanagement.repo;

import it.mahmoud.advmanagement.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity operations
 * Uses Spring Data JPA to simplify data access and manipulation
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    /**
     * Find a category by name (case-insensitive)
     * @param name Category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Check if a category with the given name exists
     * @param name Category name
     * @return true if a category with this name exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find top-level categories (no parent)
     * @param sort Sorting preferences
     * @return List of top-level categories
     */
    List<Category> findByParentCategoryIsNull(Sort sort);

    /**
     * Find subcategories of a given parent category
     * @param parentId Parent category ID
     * @param sort Sorting preferences
     * @return List of subcategories
     */
    List<Category> findByParentCategoryId(Long parentId, Sort sort);

    /**
     * Find categories by name containing a search term
     * @param searchTerm Term to search for
     * @param pageable Pagination information
     * @return Page of matching categories
     */
    Page<Category> findByNameContainingIgnoreCase(String searchTerm, Pageable pageable);

    /**
     * Find parent category by subcategory ID
     * @param subcategoryId Subcategory ID
     * @return Optional containing the parent category if found
     */
    @Query("SELECT c.parentCategory FROM Category c WHERE c.id = :subcategoryId")
    Optional<Category> findParentBySubcategoryId(@Param("subcategoryId") Long subcategoryId);

    /**
     * Find all categories in the path from root to the specified category
     * @param categoryId Target category ID
     * @return List of categories in the hierarchy path (ordered from root to target)
     */
    @Query(value = "WITH RECURSIVE category_path AS (" +
            "  SELECT id, name, parent_category_id, 0 as level FROM categories WHERE id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.id, c.name, c.parent_category_id, cp.level + 1 " +
            "  FROM categories c, category_path cp " +
            "  WHERE c.id = cp.parent_category_id " +
            ") " +
            "SELECT * FROM category_path ORDER BY level DESC",
            nativeQuery = true)
    List<Object[]> findCategoryPath(@Param("categoryId") Long categoryId);

    /**
     * Count ads in a category
     * @param categoryId Category ID
     * @return Number of ads in the category
     */
    @Query("SELECT COUNT(a) FROM Category c JOIN c.ads a WHERE c.id = :categoryId")
    long countAdsByCategory(@Param("categoryId") Long categoryId);

    /**
     * Count ads in a category and its subcategories
     * @param categoryId Category ID
     * @return Number of ads in the category hierarchy
     */
    @Query(value = "WITH RECURSIVE subcategories AS (" +
            "  SELECT id FROM categories WHERE id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.id FROM categories c JOIN subcategories sc ON c.parent_category_id = sc.id " +
            ") " +
            "SELECT COUNT(*) FROM ad_category ac WHERE ac.category_id IN (SELECT id FROM subcategories)",
            nativeQuery = true)
    long countAdsByCategoryHierarchy(@Param("categoryId") Long categoryId);

    /**
     * Find categories with most ads
     * @param pageable Pagination information
     * @return Page of categories ordered by ad count
     */
    @Query("SELECT c FROM Category c LEFT JOIN c.ads a GROUP BY c ORDER BY COUNT(a) DESC")
    Page<Category> findCategoriesWithMostAds(Pageable pageable);

    /**
     * Find categories that have no ads
     * @param pageable Pagination information
     * @return Page of unused categories
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.ads) = 0")
    Page<Category> findUnusedCategories(Pageable pageable);

    /**
     * Find categories with at least one subcategory
     * @param pageable Pagination information
     * @return Page of parent categories
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.subcategories) > 0")
    Page<Category> findCategoriesWithSubcategories(Pageable pageable);
}
