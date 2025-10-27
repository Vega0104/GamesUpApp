package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private LocalDateTime birthDate;

    @BeforeEach
    void setUp() {
        birthDate = LocalDateTime.of(1990, 1, 1, 0, 0);

        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPasswordHash("HASHED_password123");
        user.setBirthDate(birthDate);
        user.setRole(Role.CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());
    }

    // ========== REGISTER TESTS ==========

    @Test
    void register_ShouldCreateUser_WhenValidData() {
        // Arrange
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        // Act
        User result = userService.register("Jane Doe", "jane@example.com", "password123", birthDate);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", result.getFullName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals(Role.CUSTOMER, result.getRole());
        assertTrue(result.getPasswordHash().startsWith("HASHED_"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldNormalizeEmail_WhenEmailHasUpperCase() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.register("Test User", "TEST@EXAMPLE.COM", "password123", birthDate);

        // Assert
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void register_ShouldThrowException_WhenFullNameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(null, "test@example.com", "password123", birthDate)
        );
        assertEquals("Full name cannot be null or empty", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenFullNameIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("   ", "test@example.com", "password123", birthDate)
        );
        assertEquals("Full name cannot be null or empty", exception.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenEmailIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("Test User", null, "password123", birthDate)
        );
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenEmailIsInvalid() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("Test User", "invalidemail", "password123", birthDate)
        );
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenPasswordIsTooShort() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("Test User", "test@example.com", "short", birthDate)
        );
        assertEquals("Password must be at least 8 characters long", exception.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("Test User", "existing@example.com", "password123", birthDate)
        );
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ========== CREATE ADMIN TESTS ==========

    @Test
    void createAdmin_ShouldCreateAdminUser_WhenValidData() {
        // Arrange
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L);
            return u;
        });

        // Act
        User result = userService.createAdmin("Admin User", "admin@example.com", "adminpass123");

        // Assert
        assertNotNull(result);
        assertEquals("Admin User", result.getFullName());
        assertEquals("admin@example.com", result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAdmin_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createAdmin("Admin", "existing@example.com", "adminpass123")
        );
        assertEquals("Email already exists", exception.getMessage());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnUser_WhenIdExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getFullName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findById(null)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findById(0L)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    // ========== FIND BY EMAIL TESTS ==========

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Arrange
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByEmail("john.doe@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getFullName());
    }

    @Test
    void findByEmail_ShouldNormalizeEmail() {
        // Arrange
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByEmail("JOHN.DOE@EXAMPLE.COM");

        // Assert
        assertTrue(result.isPresent());
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void findByEmail_ShouldThrowException_WhenEmailIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findByEmail(null)
        );
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setFullName("Jane Doe");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        // Act
        List<User> result = userService.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    // ========== FIND BY ROLE TESTS ==========

    @Test
    void findByRole_ShouldReturnUsers_WhenRoleMatches() {
        // Arrange
        when(userRepository.findByRole(Role.CUSTOMER)).thenReturn(Arrays.asList(user));

        // Act
        List<User> result = userService.findByRole(Role.CUSTOMER);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Role.CUSTOMER, result.get(0).getRole());
    }

    @Test
    void findByRole_ShouldThrowException_WhenRoleIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findByRole(null)
        );
        assertEquals("Role cannot be null", exception.getMessage());
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_ShouldUpdateUser_WhenValidData() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new.email@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.update(1L, "John Updated", "new.email@example.com", birthDate);

        // Assert
        assertEquals("John Updated", result.getFullName());
        assertEquals("new.email@example.com", result.getEmail());
    }

    @Test
    void update_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.update(99L, "Name", "email@example.com", birthDate)
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenNewEmailAlreadyExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.update(1L, "John", "existing@example.com", birthDate)
        );
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenEmailIsInvalid() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.update(1L, "John", "invalidemail", birthDate)
        );
        assertEquals("Invalid email format", exception.getMessage());
    }

    // ========== CHANGE PASSWORD TESTS ==========

    @Test
    void changePassword_ShouldUpdatePassword_WhenOldPasswordIsCorrect() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.changePassword(1L, "password123", "newpassword123");

        // Assert
        assertEquals("HASHED_newpassword123", result.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordIsIncorrect() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1L, "wrongpassword", "newpassword123")
        );
        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ShouldThrowException_WhenNewPasswordIsTooShort() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(1L, "password123", "short")
        );
        assertEquals("New password must be at least 8 characters long", exception.getMessage());
    }

    @Test
    void changePassword_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(99L, "oldpass", "newpassword123")
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeleteUser_WhenIdExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.delete(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.delete(99L)
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.delete(null)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }
}