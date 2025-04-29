package it.mahmoud.advmanagement.config;

import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Specifications for Tag entity
 * Used for creating dynamic queries with type safety
 */
@Component
public class TagSpecifications {

    /**
     * Filter tags by name containing text
     */
    public static Specification<Tag> nameContains(String searchText) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + searchText.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), likePattern);
        };
    }

    /**
     * Filter tags by exact name (case insensitive)
     */
    public static Specification<Tag> nameEquals(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction();
            }

            return cb.equal(cb.lower(root.get("name")), name.toLowerCase());
        };
    }

    /**
     * Filter tags by ID
     */
    public static Specification<Tag> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("id"), id);
        };
    }

    /**
     * Filter tags by multiple IDs
     */
    public static Specification<Tag> hasIdIn(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("id").in(ids);
        };
    }

    /**
     * Filter tags with a name starting with a prefix
     */
    public static Specification<Tag> nameStartsWith(String prefix) {
        return (root, query, cb) -> {
            if (prefix == null || prefix.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = prefix.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), likePattern);
        };
    }

    /**
     * Filter tags with a name ending with a suffix
     */
    public static Specification<Tag> nameEndsWith(String suffix) {
        return (root, query, cb) -> {
            if (suffix == null || suffix.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + suffix.toLowerCase();
            return cb.like(cb.lower(root.get("name")), likePattern);
        };
    }

    /**
     * Filter tags by minimum ad count
     */
    public static Specification<Tag> hasMinimumAds(Integer minCount) {
        return (root, query, cb) -> {
            if (minCount == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(cb.size(root.get("ads")), minCount);
        };
    }

    /**
     * Filter unused tags
     */
    public static Specification<Tag> isUnused() {
        return (root, query, cb) -> cb.equal(cb.size(root.get("ads")), 0);
    }

    /**
     * Filter used tags
     */
    public static Specification<Tag> isUsed() {
        return (root, query, cb) -> cb.greaterThan(cb.size(root.get("ads")), 0);
    }

    /**
     * Filter tags used in ads by a specific creator
     */
    public static Specification<Tag> usedByCreator(Long creatorId) {
        return (root, query, cb) -> {
            if (creatorId == null) {
                return cb.conjunction();
            }

            Join<Tag, Ad> adsJoin = root.join("ads");
            return cb.equal(adsJoin.get("creator").get("id"), creatorId);
        };
    }

    /**
     * Filter tags used in ads with a specific category
     */
    public static Specification<Tag> usedInCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }

            Join<Tag, Ad> adsJoin = root.join("ads");
            Join<Object, Object> categoriesJoin = adsJoin.join("categories");
            return cb.equal(categoriesJoin.get("id"), categoryId);
        };
    }

    /**
     * Combine multiple specifications with AND logic
     */
    public static Specification<Tag> all(Specification<Tag>... specifications) {
        return (root, query, cb) -> {
            Predicate[] predicates = new Predicate[specifications.length];
            for (int i = 0; i < specifications.length; i++) {
                predicates[i] = specifications[i].toPredicate(root, query, cb);
            }
            return cb.and(predicates);
        };
    }
}