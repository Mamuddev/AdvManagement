package it.mahmoud.advmanagement.repo;

import it.mahmoud.advmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations
 * Uses Spring Data JPA to simplify data access and manipulation
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Find a user by email (case-insensitive)
     * @param email User's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if a user with the given email exists
     * @param email User's email address
     * @return true if a user with this email exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Search users by first name, last name, or email
     * @param searchTerm Term to search for
     * @param pageable Pagination information
     * @return Page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find users registered within a date range
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @param pageable Pagination information
     * @return Page of users registered in the date range
     */
    Page<User> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find users who haven't logged in since a specific date
     * @param date Date threshold
     * @param pageable Pagination information
     * @return Page of inactive users
     */
    Page<User> findByLastLoginBefore(LocalDateTime date, Pageable pageable);

    /**
     * Find users with a specific number of ads or more
     * @param count Minimum number of ads
     * @param pageable Pagination information
     * @return Page of active users
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.ads) >= :count")
    Page<User> findUsersWithAdsCountGreaterThanEqual(@Param("count") int count, Pageable pageable);

    /**
     * Find users with most ads
     * @param pageable Pagination information (use PageRequest.of(0, 10) for top 10)
     * @return Page of most active users
     */
    @Query("SELECT u FROM User u ORDER BY SIZE(u.ads) DESC")
    Page<User> findUsersWithMostAds(Pageable pageable);

    /**
     * Update user's last login timestamp
     * @param userId User ID
     * @param timestamp Login timestamp
     * @return Number of updated records (should be 1)
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :timestamp WHERE u.id = :userId")
    int updateLastLogin(@Param("userId") Long userId, @Param("timestamp") LocalDateTime timestamp);
}
