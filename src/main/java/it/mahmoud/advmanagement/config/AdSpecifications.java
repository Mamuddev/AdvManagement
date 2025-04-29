package it.mahmoud.advmanagement.config;

import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.Category;
import it.mahmoud.advmanagement.model.Tag;
import it.mahmoud.advmanagement.util.AdStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications for Ad entity
 * Used for creating dynamic queries with type safety
 */
@Component
public class AdSpecifications {

    /**
     * Filter ads by status
     */
    public static Specification<Ad> hasStatus(AdStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    /**
     * Filter ads by creator
     */
    public static Specification<Ad> createdBy(Long creatorId) {
        return (root, query, cb) -> {
            if (creatorId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("creator").get("id"), creatorId);
        };
    }

    /**
     * Filter ads by category
     */
    public static Specification<Ad> inCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }

            Join<Ad, Category> categoriesJoin = root.join("categories");
            return cb.equal(categoriesJoin.get("id"), categoryId);
        };
    }

    /**
     * Filter ads by one of many categories
     */
    public static Specification<Ad> inAnyCategory(List<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }

            Join<Ad, Category> categoriesJoin = root.join("categories");
            return categoriesJoin.get("id").in(categoryIds);
        };
    }

    /**
     * Filter ads by tag
     */
    public static Specification<Ad> hasTag(Long tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return cb.conjunction();
            }

            Join<Ad, Tag> tagsJoin = root.join("tags");
            return cb.equal(tagsJoin.get("id"), tagId);
        };
    }

    /**
     * Filter ads that have any of the specified tags
     */
    public static Specification<Ad> hasAnyTag(List<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) {
                return cb.conjunction();
            }

            Join<Ad, Tag> tagsJoin = root.join("tags");
            return tagsJoin.get("id").in(tagIds);
        };
    }

    /**
     * Filter ads that have all of the specified tags
     */
    public static Specification<Ad> hasAllTags(List<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) {
                return cb.conjunction();
            }

            // This requires a subquery for each tag ID to check inclusion
            // We're counting the number of matching tags and ensuring it equals the requested count
            query.distinct(true);

            Join<Ad, Tag> tagsJoin = root.join("tags");
            return cb.and(
                    tagsJoin.get("id").in(tagIds),
                    cb.equal(
                            cb.countDistinct(tagsJoin.get("id")),
                            tagIds.size()
                    )
            );
        };
    }

    /**
     * Filter ads by price range
     */
    public static Specification<Ad> priceInRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicates.isEmpty() ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter ads by text search in title and description
     */
    public static Specification<Ad> containsText(String searchText) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + searchText.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern)
            );
        };
    }

    /**
     * Filter featured ads
     */
    public static Specification<Ad> isFeatured(Boolean featured) {
        return (root, query, cb) -> {
            if (featured == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("featured"), featured);
        };
    }

    /**
     * Filter ads by date range (creation date)
     */
    public static Specification<Ad> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("creationDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("creationDate"), endDate));
            }

            return predicates.isEmpty() ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter active ads (published and not expired)
     */
    public static Specification<Ad> isActive(LocalDateTime now) {
        return (root, query, cb) -> {
            LocalDateTime effectiveNow = now != null ? now : LocalDateTime.now();

            return cb.and(
                    cb.equal(root.get("status"), AdStatus.PUBLISHED),
                    cb.or(
                            cb.isNull(root.get("expirationDate")),
                            cb.greaterThan(root.get("expirationDate"), effectiveNow)
                    )
            );
        };
    }

    /**
     * Filter ads by view count range
     */
    public static Specification<Ad> viewsInRange(Integer minViews, Integer maxViews) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minViews != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("views"), minViews));
            }

            if (maxViews != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("views"), maxViews));
            }

            return predicates.isEmpty() ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Combine multiple specifications with AND logic
     */
    public static Specification<Ad> all(Specification<Ad>... specifications) {
        return (root, query, cb) -> {
            Predicate[] predicates = new Predicate[specifications.length];
            for (int i = 0; i < specifications.length; i++) {
                predicates[i] = specifications[i].toPredicate(root, query, cb);
            }
            return cb.and(predicates);
        };
    }
}
