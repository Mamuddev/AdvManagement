package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.dto.user.PasswordChangeDTO;
import it.mahmoud.advmanagement.dto.user.UserCreateDTO;
import it.mahmoud.advmanagement.dto.user.UserDTO;
import it.mahmoud.advmanagement.dto.user.UserUpdateDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.exception.UnauthorizedException;
import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.User;
import it.mahmoud.advmanagement.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateDTO testUserCreateDTO;
    private UserUpdateDTO testUserUpdateDTO;
    private PasswordChangeDTO testPasswordChangeDTO;
    private Pageable pageable;
    private LocalDateTime now;
    private Set<Ad> testAds;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Set up test user with HashSet instead of ArrayList
        testAds = new HashSet<>();

        // Set up test user
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("newEncodedPassword")
                .registrationDate(now)
                .lastLogin(now.minusDays(5))
                .ads(testAds)
                .build();

        // Set up test user create DTO with valid password format
        testUserCreateDTO = UserCreateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("Password1@")  // Meets password requirements
                .confirmPassword("Password1@")
                .build();

        // Set up test user update DTO
        testUserUpdateDTO = UserUpdateDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Updated")
                .build();

        // Set up test password change DTO with valid password format
        testPasswordChangeDTO = PasswordChangeDTO.builder()
                .userId(1L)
                .currentPassword("CurrentPass1@")
                .newPassword("NewPassword1@")  // Meets password requirements
                .confirmPassword("NewPassword1@")
                .build();

        // Set up pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        UserDTO result = userService.registerUser(testUserCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserCreateDTO.getFirstName(), result.getFirstName());
        assertEquals(testUserCreateDTO.getLastName(), result.getLastName());
        assertEquals(testUserCreateDTO.getEmail().toLowerCase(), result.getEmail());

        // Verify interactions
        verify(userRepository).existsByEmailIgnoreCase(testUserCreateDTO.getEmail());
        verify(passwordEncoder).encode(testUserCreateDTO.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                userService.registerUser(testUserCreateDTO));

        // Verify interactions
        verify(userRepository).existsByEmailIgnoreCase(testUserCreateDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_PasswordMismatch_ThrowsException() {
        // Given
        testUserCreateDTO.setConfirmPassword("mismatchedPassword");
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                userService.registerUser(testUserCreateDTO));

        // Verify interactions
        verify(userRepository).existsByEmailIgnoreCase(testUserCreateDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.updateUser(1L, testUserUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserUpdateDTO.getFirstName(), result.getFirstName());
        assertEquals(testUserUpdateDTO.getLastName(), result.getLastName());

        // Verify interactions
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_IdMismatch_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                userService.updateUser(2L, testUserUpdateDTO));

        // Verify interactions
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateUser(1L, testUserUpdateDTO));

        // Verify interactions
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
        assertEquals(testUser.getEmail(), result.getEmail());

        // Verify interactions
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(1L));

        // Verify interactions
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserByEmail("john.doe@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());

        // Verify interactions
        verify(userRepository).findByEmailIgnoreCase("john.doe@example.com");
    }

    @Test
    void getUserByEmail_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserByEmail("nonexistent@example.com"));

        // Verify interactions
        verify(userRepository).findByEmailIgnoreCase("nonexistent@example.com");
    }

    @Test
    void emailExists_ReturnsTrueWhenExists() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        // Act
        boolean result = userService.emailExists("existing@example.com");

        // Assert
        assertTrue(result);

        // Verify interactions
        verify(userRepository).existsByEmailIgnoreCase("existing@example.com");
    }

    @Test
    void emailExists_ReturnsFalseWhenNotExists() {
        // Given
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        // Act
        boolean result = userService.emailExists("nonexistent@example.com");

        // Assert
        assertFalse(result);

        // Verify interactions
        verify(userRepository).existsByEmailIgnoreCase("nonexistent@example.com");
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(anyLong());

        // Act
        userService.deleteUser(1L);

        // Verify interactions
        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUser(1L));

        // Verify interactions
        verify(userRepository).findById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllUsers_ReturnsPaginatedUsers() {
        // Given
        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(userRepository).findAll(pageable);
    }

    @Test
    void searchUsers_ReturnsPaginatedUsersMatchingSearchTerm() {
        // Given
        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.searchUsers("John", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findUsersByRegistrationDateBetween_ReturnsPaginatedUsersRegisteredInDateRange() {
        // Given
        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        LocalDateTime startDate = now.minusDays(7);
        LocalDateTime endDate = now;
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.findUsersByRegistrationDateBetween(startDate, endDate, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void changePassword_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.changePassword(testPasswordChangeDTO);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("newEncodedPassword", userCaptor.getValue().getPassword());

        // Verify interactions
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(
                eq(testPasswordChangeDTO.getCurrentPassword()),
                eq(testUser.getPassword())
        );
        verify(passwordEncoder).encode(testPasswordChangeDTO.getNewPassword());
    }

    @Test
    void changePassword_InvalidCurrentPassword_ThrowsException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                userService.changePassword(testPasswordChangeDTO));

        // Verify interactions
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(testPasswordChangeDTO.getCurrentPassword(), testUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_PasswordMismatch_ThrowsException() {
        // Given
        testPasswordChangeDTO.setConfirmPassword("mismatchedPassword");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidOperationException.class, () ->
                userService.changePassword(testPasswordChangeDTO));

        // Verify interactions
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(testPasswordChangeDTO.getCurrentPassword(), testUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateLastLogin_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateLastLogin(1L);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getLastLogin());

        // Verify interactions
        verify(userRepository).findById(1L);
    }

    @Test
    void findInactiveUsers_ReturnsPaginatedInactiveUsers() {
        // Given
        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        LocalDateTime date = now.minusDays(3);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.findInactiveUsers(date, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findMostActiveUsers_ReturnsPaginatedMostActiveUsers() {
        // Given
        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        when(userRepository.findUsersWithMostAds(pageable)).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.findMostActiveUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(userRepository).findUsersWithMostAds(pageable);
    }

    @Test
    void countUserAds_ReturnsCorrectCount() {
        // Given
        Set<Ad> ads = new HashSet<>();
        Ad ad1 = new Ad();
        ad1.setId(1L);

        Ad ad2 = new Ad();
        ad2.setId(2L);

        ads.add(ad1);
        ads.add(ad2);
        testUser.setAds(ads);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // Act
        long result = userService.countUserAds(1L);

        // Assert
        assertEquals(2, result);

        // Verify interactions
        verify(userRepository).findById(1L);
    }

    @Test
    void loadUserByUsername_Success() {
        // Given
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userService.loadUserByUsername("john.doe@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());

        // Verify interactions
        verify(userRepository).findByEmailIgnoreCase("john.doe@example.com");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("nonexistent@example.com"));

        // Verify interactions
        verify(userRepository).findByEmailIgnoreCase("nonexistent@example.com");
    }
}