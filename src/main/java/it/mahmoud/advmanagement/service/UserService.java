package it.mahmoud.advmanagement.service;

import it.mahmoud.advmanagement.dto.user.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service interface for operations on User entities
 */
public interface UserService extends UserDetailsService {

    /**
     * Register a new user
     * @param userCreateDTO User creation data
     * @return Created user data
     */
    UserDTO registerUser(UserCreateDTO userCreateDTO);

    /**
     * Update an existing user
     * @param id User ID
     * @param userUpdateDTO User update data
     * @return Updated user data
     */
    UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);

    /**
     * Get user by ID
     * @param id User ID
     * @return User data
     */
    UserDTO getUserById(Long id);

    /**
     * Get user by email
     * @param email User email
     * @return User data
     */
    UserDTO getUserByEmail(String email);

    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists
     */
    boolean emailExists(String email);

    /**
     * Delete a user
     * @param id User ID
     */
    void deleteUser(Long id);

    /**
     * Get all users with pagination
     * @param pageable Pagination information
     * @return Page of users
     */
    Page<UserDTO> getAllUsers(Pageable pageable);

    /**
     * Search users by name or email
     * @param searchTerm Search term
     * @param pageable Pagination information
     * @return Page of matching users
     */
    Page<UserDTO> searchUsers(String searchTerm, Pageable pageable);

    /**
     * Find users registered between dates
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination information
     * @return Page of users registered in the date range
     */
    Page<UserDTO> findUsersByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Change user password
     * @param passwordChangeDTO Password change data
     */
    void changePassword(PasswordChangeDTO passwordChangeDTO);

    /**
     * Update user last login time
     * @param userId User ID
     */
    void updateLastLogin(Long userId);

    /**
     * Find inactive users (haven't logged in since a specific date)
     * @param date Date threshold
     * @param pageable Pagination information
     * @return Page of inactive users
     */
    Page<UserDTO> findInactiveUsers(LocalDateTime date, Pageable pageable);

    /**
     * Find most active users (with most ads)
     * @param pageable Pagination information
     * @return Page of most active users
     */
    Page<UserDTO> findMostActiveUsers(Pageable pageable);

    /**
     * Count user's ads
     * @param userId User ID
     * @return Number of ads created by the user
     */
    long countUserAds(Long userId);
}