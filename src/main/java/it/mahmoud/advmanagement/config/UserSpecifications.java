package it.mahmoud.advmanagement.config;

import it.mahmoud.advmanagement.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications for User entity
 * Used for creating dynamic queries with type safety
 */
@Component
public class UserSpecifications {

    /**
     * Filter users by name or email containing text
     */
    public static Specification<User> containsText(String searchText) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + searchText.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likePattern),
                    cb.like(cb.lower(root.get("lastName")), likePattern),
                    cb.like(cb.lower(root.get("email")), likePattern)
            );
        };
    }

    /**
     * Filter users by first name
     */
    public static Specification<User> firstNameContains(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + firstName.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("firstName")), likePattern);
        };
    }

    /**
     * Filter users by last name
     */
    public static Specification<User> lastNameContains(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + lastName.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("lastName")), likePattern);
        };
    }

    /**
     * Filter users by email pattern
     */
    public static Specification<User> emailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + email.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("email")), likePattern);
        };
    }

    /**
     * Filter users by registration date range
     */
    public static Specification<User> registeredBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("registrationDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("registrationDate"), endDate));
            }

            return predicates.isEmpty() ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter users by last login date
     */
    public static Specification<User> lastLoginBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }

            return cb.lessThan(root.get("lastLogin"), date);
        };
    }

    /**
     * Filter users who haven't logged in within a number of days
     */
    public static Specification<User> inactiveSince(int days) {
        return (root, query, cb) -> {
            LocalDateTime threshold = LocalDateTime.now().minusDays(days);
            return cb.or(
                    cb.isNull(root.get("lastLogin")),
                    cb.lessThan(root.get("lastLogin"), threshold)
            );
        };
    }

    /**
     * Filter users who logged in recently within a number of days
     */
    public static Specification<User> activeWithin(int days) {
        return (root, query, cb) -> {
            LocalDateTime threshold = LocalDateTime.now().minusDays(days);
            return cb.and(
                    cb.isNotNull(root.get("lastLogin")),
                    cb.greaterThanOrEqualTo(root.get("lastLogin"), threshold)
            );
        };
    }

    /**
     * Filter users by ad count
     */
    public static Specification<User> hasMinimumAds(Integer minCount) {
        return (root, query, cb) -> {
            if (minCount == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(cb.size(root.get("ads")), minCount);
        };
    }

    /**
     * Filter users with no ads
     */
    public static Specification<User> hasNoAds() {
        return (root, query, cb) -> cb.equal(cb.size(root.get("ads")), 0);
    }

    /**
     * Filter users by ID
     */
    public static Specification<User> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("id"), id);
        };
    }

    /**
     * Combine multiple specifications with AND logic
     */
    public static Specification<User> all(Specification<User>... specifications) {
        return (root, query, cb) -> {
            Predicate[] predicates = new Predicate[specifications.length];
            for (int i = 0; i < specifications.length; i++) {
                predicates[i] = specifications[i].toPredicate(root, query, cb);
            }
            return cb.and(predicates);
        };
    }
}