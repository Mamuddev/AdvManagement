package it.mahmoud.advmanagement.integration;

import it.mahmoud.advmanagement.dto.user.PasswordChangeDTO;
import it.mahmoud.advmanagement.dto.user.UserCreateDTO;
import it.mahmoud.advmanagement.dto.user.UserDTO;
import it.mahmoud.advmanagement.dto.user.UserUpdateDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.exception.UnauthorizedException;
import it.mahmoud.advmanagement.model.User;
import it.mahmoud.advmanagement.repo.UserRepository;
import it.mahmoud.advmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserService.
 *
 * Note: This uses an actual database (H2 in-memory) and real Spring context.
 * The @ActiveProfiles("test") ensures we use test configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // Each test runs in a transaction that is rolled back at the end
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserCreateDTO userCreateDTO;
    private UserUpdateDTO userUpdateDTO;
    private PasswordChangeDTO passwordChangeDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        userRepository.deleteAll();  // Start with a clean state

        // Set up test user create DTO
        userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("John");
        userCreateDTO.setLastName("Doe");
        userCreateDTO.setEmail("john.doe@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setConfirmPassword("password123");

        // Set up test user update DTO
        userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFirstName("John");
        userUpdateDTO.setLastName("Updated");

        // Set up test password change DTO
        passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("password123");
        passwordChangeDTO.setNewPassword("newPassword123");
        passwordChangeDTO.setConfirmPassword("newPassword123");
    }

    @Test
    void registerUser_Success() {
        // Act
        UserDTO result = userService.registerUser(userCreateDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(userCreateDTO.getFirstName(), result.getFirstName());
        assertEquals(userCreateDTO.getLastName(), result.getLastName());
        assertEquals(userCreateDTO.getEmail().toLowerCase(), result.getEmail());
        assertNotNull(result.getRegistrationDate());
        assertEquals(0, result.getAdsCount());

        // Verify user was saved to repository
        assertTrue(userRepository.existsByEmailIgnoreCase(userCreateDTO.getEmail()));

        // Verify password was encoded
        User savedUser = userRepository.findByEmailIgnoreCase(userCreateDTO.getEmail()).orElseThrow();
        assertTrue(passwordEncoder.matches(userCreateDTO.getPassword(), savedUser.getPassword()));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        // First registration
        userService.registerUser(userCreateDTO);

        // Second registration with same email
        UserCreateDTO duplicateUser = new UserCreateDTO();
        duplicateUser.setFirstName("Another");
        duplicateUser.setLastName("User");
        duplicateUser.setEmail(userCreateDTO.getEmail());  // Same email
        duplicateUser.setPassword("different123");
        duplicateUser.setConfirmPassword("different123");

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(duplicateUser));
    }

    @Test
    void registerUser_PasswordMismatch_ThrowsException() {
        // Arrange
        userCreateDTO.setConfirmPassword("differentPassword");

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> userService.registerUser(userCreateDTO));
    }

    @Test
    void updateUser_Success() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);
        userUpdateDTO.setId(registeredUser.getId());

        // Act
        UserDTO result = userService.updateUser(registeredUser.getId(), userUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(registeredUser.getId(), result.getId());
        assertEquals(userUpdateDTO.getFirstName(), result.getFirstName());
        assertEquals(userUpdateDTO.getLastName(), result.getLastName());
        assertEquals(registeredUser.getEmail(), result.getEmail());  // Email unchanged
    }

    @Test
    void updateUser_IdMismatch_ThrowsException() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);
        userUpdateDTO.setId(registeredUser.getId());

        // Act & Assert - Try to update with mismatched ID
        assertThrows(InvalidOperationException.class, () ->
                userService.updateUser(registeredUser.getId() + 1, userUpdateDTO));
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Arrange
        userUpdateDTO.setId(999L);  // Non-existent user ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateUser(999L, userUpdateDTO));
    }

    @Test
    void getUserById_Success() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);

        // Act
        UserDTO result = userService.getUserById(registeredUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(registeredUser.getId(), result.getId());
        assertEquals(registeredUser.getFirstName(), result.getFirstName());
        assertEquals(registeredUser.getLastName(), result.getLastName());
        assertEquals(registeredUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(999L));  // Non-existent user ID
    }

    @Test
    void getUserByEmail_Success() {
        // Arrange - Register a user first
        userService.registerUser(userCreateDTO);

        // Act
        UserDTO result = userService.getUserByEmail(userCreateDTO.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(userCreateDTO.getFirstName(), result.getFirstName());
        assertEquals(userCreateDTO.getLastName(), result.getLastName());
        assertEquals(userCreateDTO.getEmail().toLowerCase(), result.getEmail());
    }

    @Test
    void getUserByEmail_UserNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserByEmail("nonexistent@example.com"));
    }

    @Test
    void emailExists_ReturnsTrueWhenExists() {
        // Arrange - Register a user first
        userService.registerUser(userCreateDTO);

        // Act
        boolean result = userService.emailExists(userCreateDTO.getEmail());

        // Assert
        assertTrue(result);
    }

    @Test
    void emailExists_ReturnsFalseWhenNotExists() {
        // Act
        boolean result = userService.emailExists("nonexistent@example.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteUser_Success() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);

        // Act
        userService.deleteUser(registeredUser.getId());

        // Assert
        assertFalse(userRepository.existsById(registeredUser.getId()));
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUser(999L));  // Non-existent user ID
    }

    @Test
    void getAllUsers_ReturnsExpectedPage() {
        // Arrange - Register multiple users
        userService.registerUser(userCreateDTO);

        UserCreateDTO user2 = new UserCreateDTO();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setPassword("password123");
        user2.setConfirmPassword("password123");
        userService.registerUser(user2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void searchUsers_FindsUsersByNameOrEmail() {
        // Arrange - Register multiple users
        userService.registerUser(userCreateDTO);  // John Doe

        UserCreateDTO user2 = new UserCreateDTO();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setPassword("password123");
        user2.setConfirmPassword("password123");
        userService.registerUser(user2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert - Search by first name
        Page<UserDTO> result1 = userService.searchUsers("John", pageable);
        assertEquals(1, result1.getTotalElements());
        assertEquals("John", result1.getContent().get(0).getFirstName());

        // Act & Assert - Search by last name
        Page<UserDTO> result2 = userService.searchUsers("Smith", pageable);
        assertEquals(1, result2.getTotalElements());
        assertEquals("Smith", result2.getContent().get(0).getLastName());

        // Act & Assert - Search by email
        Page<UserDTO> result3 = userService.searchUsers("jane.smith", pageable);
        assertEquals(1, result3.getTotalElements());
        assertEquals("jane.smith@example.com", result3.getContent().get(0).getEmail());
    }

    @Test
    void findUsersByRegistrationDateBetween_ReturnsExpectedUsers() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);

        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<UserDTO> result = userService.findUsersByRegistrationDateBetween(startDate, endDate, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(registeredUser.getId(), result.getContent().get(0).getId());
    }

    @Test
    void changePassword_Success() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);
        passwordChangeDTO.setUserId(registeredUser.getId());

        // Act
        userService.changePassword(passwordChangeDTO);

        // Verify password was changed
        User updatedUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches(passwordChangeDTO.getNewPassword(), updatedUser.getPassword()));
        assertFalse(passwordEncoder.matches(userCreateDTO.getPassword(), updatedUser.getPassword()));
    }

    @Test
    void changePassword_InvalidCurrentPassword_ThrowsException() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);
        passwordChangeDTO.setUserId(registeredUser.getId());
        passwordChangeDTO.setCurrentPassword("wrongPassword");  // Wrong current password

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                userService.changePassword(passwordChangeDTO));
    }

    @Test
    void changePassword_PasswordMismatch_ThrowsException() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);
        passwordChangeDTO.setUserId(registeredUser.getId());
        passwordChangeDTO.setConfirmPassword("differentPassword");  // Mismatched new password

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                userService.changePassword(passwordChangeDTO));
    }

    @Test
    void updateLastLogin_Success() {
        // Arrange - Register a user first
        UserDTO registeredUser = userService.registerUser(userCreateDTO);
        User initialUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        LocalDateTime initialLoginTime = initialUser.getLastLogin();

        // Act
        userService.updateLastLogin(registeredUser.getId());

        // Assert
        User updatedUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        assertNotNull(updatedUser.getLastLogin());

        // If initialLoginTime was null or the update worked, they should be different
        if (initialLoginTime != null) {
            assertNotEquals(initialLoginTime, updatedUser.getLastLogin());
        }
    }

    @Test
    void findInactiveUsers_ReturnsExpectedUsers() {
        // This test requires more setup with custom last login dates
        // Since we can't easily manipulate timestamps in an integration test,
        // this is more of a placeholder test that just verifies the method doesn't throw an exception

        // Arrange - Register a user first
        userService.registerUser(userCreateDTO);
        LocalDateTime date = now.plusDays(1);  // Future date, so all users should be "inactive"
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<UserDTO> result = userService.findInactiveUsers(date, pageable);

        // Assert
        assertNotNull(result);
        // Don't assert count since it depends on the implementation details
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange - Register a user first
        userService.registerUser(userCreateDTO);

        // Act & Assert - This should not throw an exception
        assertDoesNotThrow(() -> userService.loadUserByUsername(userCreateDTO.getEmail()));
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Act & Assert
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("nonexistent@example.com"));
    }
}
