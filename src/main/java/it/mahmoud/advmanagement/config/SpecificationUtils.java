package it.mahmoud.advmanagement.config;

import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class for working with specifications
 * Provides general methods that can be used with any entity type
 */
public final class SpecificationUtils {

    private SpecificationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Create a specification that always evaluates to true
     * Useful as a starting point for dynamic specifications
     * @param <T> Entity type
     * @return A specification that always returns true
     */
    public static <T> Specification<T> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    /**
     * Create a specification that always evaluates to false
     * @param <T> Entity type
     * @return A specification that always returns false
     */
    public static <T> Specification<T> alwaysFalse() {
        return (root, query, cb) -> cb.disjunction();
    }

    /**
     * Combine multiple specifications with AND logic
     * @param <T> Entity type
     * @param specs Specifications to combine
     * @return Combined specification
     */
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specs) {
        if (specs == null || specs.length == 0) {
            return alwaysTrue();
        }

        Specification<T> result = specs[0];
        for (int i = 1; i < specs.length; i++) {
            result = result.and(specs[i]);
        }
        return result;
    }

    /**
     * Combine multiple specifications with OR logic
     * @param <T> Entity type
     * @param specs Specifications to combine
     * @return Combined specification
     */
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specs) {
        if (specs == null || specs.length == 0) {
            return alwaysTrue();
        }

        Specification<T> result = specs[0];
        for (int i = 1; i < specs.length; i++) {
            result = result.or(specs[i]);
        }
        return result;
    }

    /**
     * Negate a specification
     * @param <T> Entity type
     * @param spec Specification to negate
     * @return Negated specification
     */
    public static <T> Specification<T> not(Specification<T> spec) {
        return Specification.not(spec);
    }

    /**
     * Apply a specification only if a condition is true
     * @param <T> Entity type
     * @param condition Condition to check
     * @param spec Specification to apply if condition is true
     * @return Conditional specification
     */
    public static <T> Specification<T> where(boolean condition, Specification<T> spec) {
        return condition ? spec : alwaysTrue();
    }

    /**
     * Apply a specification only if an object is not null
     * @param <T> Entity type
     * @param object Object to check for null
     * @param spec Specification to apply if object is not null
     * @return Conditional specification
     */
    public static <T, V> Specification<T> whereNotNull(V object, Specification<T> spec) {
        return object != null ? spec : alwaysTrue();
    }
}