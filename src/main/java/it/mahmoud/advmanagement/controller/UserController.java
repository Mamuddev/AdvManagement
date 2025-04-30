package it.mahmoud.advmanagement.controller;

import it.mahmoud.advmanagement.dto.user.PasswordChangeDTO;
import it.mahmoud.advmanagement.dto.user.UserCreateDTO;
import it.mahmoud.advmanagement.dto.user.UserDTO;
import it.mahmoud.advmanagement.dto.user.UserUpdateDTO;
import it.mahmoud.advmanagement.dto.response.ApiResponseDTO;
import it.mahmoud.advmanagement.dto.response.PageMetaDTO;
import it.mahmoud.advmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for User operations
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<UserDTO>> registerUser( @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO registeredUser = userService.registerUser(userCreateDTO);
        return new ResponseEntity<>(
                ApiResponseDTO.success(registeredUser, "User registered successfully"),
                HttpStatus.CREATED);
    }

    /**
     * Update an existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateUser(
            @PathVariable Long id,
             @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(updatedUser, "User updated successfully"));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(user, "User retrieved successfully"));
    }

    /**
     * Get user by email
     */
    @GetMapping("/by-email")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserByEmail(@RequestParam String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponseDTO.success(user, "User retrieved successfully"));
    }

    /**
     * Check if email exists
     */
    @GetMapping("/email-exists")
    public ResponseEntity<ApiResponseDTO<Boolean>> emailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(ApiResponseDTO.success(exists, "Email existence checked"));
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "User deleted successfully"));
    }

    /**
     * Get all users (paginated)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<UserDTO>>> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        PageMetaDTO pageMeta = createPageMeta(users);
        return ResponseEntity.ok(ApiResponseDTO.success(users, pageMeta));
    }

    /**
     * Search users by term
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<Page<UserDTO>>> searchUsers(
            @RequestParam String term,
            Pageable pageable) {
        Page<UserDTO> users = userService.searchUsers(term, pageable);
        PageMetaDTO pageMeta = createPageMeta(users);
        return ResponseEntity.ok(ApiResponseDTO.success(users, pageMeta));
    }

    /**
     * Find users by registration date range
     */
    @GetMapping("/by-registration-date")
    public ResponseEntity<ApiResponseDTO<Page<UserDTO>>> findUsersByRegistrationDateBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<UserDTO> users = userService.findUsersByRegistrationDateBetween(startDate, endDate, pageable);
        PageMetaDTO pageMeta = createPageMeta(users);
        return ResponseEntity.ok(ApiResponseDTO.success(users, pageMeta));
    }

    /**
     * Change user password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponseDTO<Void>> changePassword( @RequestBody PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(passwordChangeDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Password changed successfully"));
    }

    /**
     * Update user's last login timestamp
     */
    @PostMapping("/{userId}/update-last-login")
    public ResponseEntity<ApiResponseDTO<Void>> updateLastLogin(@PathVariable Long userId) {
        userService.updateLastLogin(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Last login updated successfully"));
    }

    /**
     * Find inactive users (no login since specified date)
     */
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponseDTO<Page<UserDTO>>> findInactiveUsers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            Pageable pageable) {
        Page<UserDTO> users = userService.findInactiveUsers(since, pageable);
        PageMetaDTO pageMeta = createPageMeta(users);
        return ResponseEntity.ok(ApiResponseDTO.success(users, pageMeta));
    }

    /**
     * Find most active users (with most ads)
     */
    @GetMapping("/most-active")
    public ResponseEntity<ApiResponseDTO<Page<UserDTO>>> findMostActiveUsers(Pageable pageable) {
        Page<UserDTO> users = userService.findMostActiveUsers(pageable);
        PageMetaDTO pageMeta = createPageMeta(users);
        return ResponseEntity.ok(ApiResponseDTO.success(users, pageMeta));
    }

    /**
     * Count ads for a specific user
     */
    @GetMapping("/{userId}/count-ads")
    public ResponseEntity<ApiResponseDTO<Long>> countUserAds(@PathVariable Long userId) {
        long count = userService.countUserAds(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(count, "Ad count retrieved successfully"));
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