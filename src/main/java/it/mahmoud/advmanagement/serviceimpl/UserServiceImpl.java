package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.config.UserSpecifications;
import it.mahmoud.advmanagement.dto.user.PasswordChangeDTO;
import it.mahmoud.advmanagement.dto.user.UserCreateDTO;
import it.mahmoud.advmanagement.dto.user.UserDTO;
import it.mahmoud.advmanagement.dto.user.UserUpdateDTO;
import it.mahmoud.advmanagement.exception.*;
import it.mahmoud.advmanagement.model.User;
import it.mahmoud.advmanagement.repo.UserRepository;
import it.mahmoud.advmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * Implementation of UserService
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDTO registerUser(UserCreateDTO userCreateDTO) {
        // Check if user already exists
        if (userRepository.existsByEmailIgnoreCase(userCreateDTO.getEmail())) {
            throw DuplicateResourceException.user(userCreateDTO.getEmail());
        }

        // Validate password match
        if (!userCreateDTO.getPassword().equals(userCreateDTO.getConfirmPassword())) {
            throw InvalidOperationException.passwordMismatch();
        }

        // Create new user
        User user = User.builder()
                .firstName(userCreateDTO.getFirstName())
                .lastName(userCreateDTO.getLastName())
                .email(userCreateDTO.getEmail().toLowerCase())
                .password(passwordEncoder.encode(userCreateDTO.getPassword()))
                .registrationDate(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        // Validate ID match
        if (!id.equals(userUpdateDTO.getId())) {
            throw new InvalidOperationException(
                    ApiErrorCode.VALIDATION_ERROR,
                    "ID in path does not match ID in request body");
        }

        // Find user
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.user(id.toString()));

        // Update fields
        user.setFirstName(userUpdateDTO.getFirstName());
        user.setLastName(userUpdateDTO.getLastName());

        user = userRepository.save(user);

        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> ResourceNotFoundException.user(id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(this::mapToDTO)
                .orElseThrow(() -> ResourceNotFoundException.user(email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.user(id.toString()));

        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String searchTerm, Pageable pageable) {
        Specification<User> spec = UserSpecifications.containsText(searchTerm);
        return userRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findUsersByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<User> spec = UserSpecifications.registeredBetween(startDate, endDate);
        return userRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeDTO passwordChangeDTO) {
        // Find user
        User user = userRepository.findById(passwordChangeDTO.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.user(passwordChangeDTO.getUserId().toString()));

        // Validate current password
        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPassword())) {
            throw UnauthorizedException.invalidCredentials();
        }

        // Validate password match
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            throw InvalidOperationException.passwordMismatch();
        }

        // Update password
        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId.toString()));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findInactiveUsers(LocalDateTime date, Pageable pageable) {
        Specification<User> spec = UserSpecifications.lastLoginBefore(date);
        return userRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findMostActiveUsers(Pageable pageable) {
        return userRepository.findUsersWithMostAds(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserAds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId.toString()));

        return user.getAds().size();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    /**
     * Map User entity to UserDTO
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .registrationDate(user.getRegistrationDate())
                .adsCount(user.getAds() != null ? user.getAds().size() : 0)
                .build();
    }
}
