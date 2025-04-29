package it.mahmoud.advmanagement.config;

import it.mahmoud.advmanagement.model.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Specifications for Category entity
 * Used for creating dynamic queries with type safety
 */
@Component
public class CategorySpecifications {

    /**
     * Filter categories by name containing text
     */
    public static Specification<Category> nameContains(String searchText) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + searchText.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), likePattern);
        };
    }

    /**
     * Filter categories by description containing text
     */
    public static Specification<Category> descriptionContains(String searchText) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + searchText.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("description")), likePattern);
        };
    }

    /**
     * Filter categories by ID
     */
    public static Specification<Category> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("id"), id);
        };
    }

    /**
     * Filter top-level categories (no parent)
     */
    public static Specification<Category> isTopLevel() {
        return (root, query, cb) -> cb.isNull(root.get("parentCategory"));
    }

    /**
     * Filter subcategories (has parent)
     */
    public static Specification<Category> isSubcategory() {
        return (root, query, cb) -> cb.isNotNull(root.get("parentCategory"));
    }

    /**
     * Filter categories by parent ID
     */
    public static Specification<Category> hasParent(Long parentId) {
        return (root, query, cb) -> {
            if (parentId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("parentCategory").get("id"), parentId);
        };
    }

    /**
     * Filter categories with a minimum number of ads
     */
    public static Specification<Category> hasMinimumAds(Integer minCount) {
        return (root, query, cb) -> {
            if (minCount == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(cb.size(root.get("ads")), minCount);
        };
    }

    /**
     * Filter categories with no ads
     */
    public static Specification<Category> hasNoAds() {
        return (root, query, cb) -> cb.equal(cb.size(root.get("ads")), 0);
    }

    /**
     * Filter categories that have ads
     */
    public static Specification<Category> hasAnyAds() {
        return (root, query, cb) -> cb.greaterThan(cb.size(root.get("ads")), 0);
    }

    /**
     * Filter categories with subcategories
     */
    public static Specification<Category> hasSubcategories() {
        return (root, query, cb) -> cb.greaterThan(cb.size(root.get("subcategories")), 0);
    }

    /**
     * Filter categories with no subcategories (leaf categories)
     */
    public static Specification<Category> isLeafCategory() {
        return (root, query, cb) -> cb.equal(cb.size(root.get("subcategories")), 0);
    }

    /**
     * Filter categories by depth level in hierarchy
     * Note: This is complex to implement with JPA Criteria directly
     * A custom approach or native SQL might be more appropriate for this case
     */
    public static Specification<Category> atHierarchyLevel(int level) {
        return (root, query, cb) -> {
            if (level == 0) {
                return cb.isNull(root.get("parentCategory"));
            } else {
                // This is a simplified approach that only works for level 1
                // For deeper levels, a recursive CTE or similar approach is needed
                return cb.isNotNull(root.get("parentCategory"));
            }
        };
    }

    /**
     * Combine multiple specifications with AND logic
     */
    public static Specification<Category> all(Specification<Category>... specifications) {
        return (root, query, cb) -> {
            Predicate[] predicates = new Predicate[specifications.length];
            for (int i = 0; i < specifications.length; i++) {
                predicates[i] = specifications[i].toPredicate(root, query, cb);
            }
            return cb.and(predicates);
        };
    }
}
