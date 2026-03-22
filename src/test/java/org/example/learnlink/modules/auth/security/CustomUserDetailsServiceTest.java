package org.example.learnlink.modules.auth.security;

import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.entity.UserRole;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests - AAA Pattern")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    // ============= LOAD USER BY USERNAME TESTS =============

    @Test
    @DisplayName("loadUserByUsername() - Should successfully load user by email")
    void testLoadUserByUsernameSuccess() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email(email)
                .password("hashedPassword")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserByUsername() - Should return UserDetails of type CustomUserDetails")
    void testLoadUserByUsernameReturnsCustomUserDetails() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email(email)
                .password("hashedPassword")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertInstanceOf(CustomUserDetails.class, userDetails);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserByUsername() - Should throw UsernameNotFoundException when user not found")
    void testLoadUserByUsernameUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email)
        );

        assertTrue(exception.getMessage().contains("User not found with email"));
        assertTrue(exception.getMessage().contains(email));
        verify(userRepository).findByEmail(email);
    }

    // ============= USER DETAILS CONTENT TESTS =============

    @Test
    @DisplayName("loadUserByUsername() - Should load user with all required fields")
    void testLoadUserByUsernameWithAllFields() {
        // Arrange
        String email = "student@example.com";
        User user = User.builder()
                .id(123L)
                .username("studentuser")
                .email(email)
                .password("securePassword123")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("securePassword123", userDetails.getPassword());
        assertEquals(123L, customUserDetails.getId());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserByUsername() - Should return user with different roles")
    void testLoadUserByUsernameWithDifferentRoles() {
        // Arrange
        String adminEmail = "admin@example.com";
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email(adminEmail)
                .password("adminPassword")
                .role(UserRole.ADMIN)
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(adminEmail);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        // Assert
        assertEquals(UserRole.ADMIN.name(), customUserDetails.getRole());
        verify(userRepository).findByEmail(adminEmail);
    }

    // ============= MULTIPLE CALLS TESTS =============

    @Test
    @DisplayName("loadUserByUsername() - Should call repository exactly once per invocation")
    void testLoadUserByUsernameRepositoryCalledOnce() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email(email)
                .password("hashedPassword")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        customUserDetailsService.loadUserByUsername(email);

        // Assert
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserByUsername() - Should handle multiple different users")
    void testLoadUserByUsernameMultipleDifferentUsers() {
        // Arrange
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .email(email1)
                .password("password1")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .email(email2)
                .password("password2")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(email1)).thenReturn(Optional.of(user1));
        when(userRepository.findByEmail(email2)).thenReturn(Optional.of(user2));

        // Act
        UserDetails userDetails1 = customUserDetailsService.loadUserByUsername(email1);
        UserDetails userDetails2 = customUserDetailsService.loadUserByUsername(email2);

        // Assert
        assertEquals(email1, userDetails1.getUsername());
        assertEquals(email2, userDetails2.getUsername());
        verify(userRepository).findByEmail(email1);
        verify(userRepository).findByEmail(email2);
    }

    // ============= EDGE CASE TESTS =============

    @Test
    @DisplayName("loadUserByUsername() - Should throw exception with correct message format")
    void testLoadUserByUsernameExceptionMessageFormat() {
        // Arrange
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email)
        );

        String message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("User not found with email"));
        assertTrue(message.contains(email));
    }

    @Test
    @DisplayName("loadUserByUsername() - Should load inactive user (access control is elsewhere)")
    void testLoadUserByUsernameInactiveUser() {
        // Arrange
        String email = "inactive@example.com";
        User inactiveUser = User.builder()
                .id(1L)
                .username("inactiveuser")
                .email(email)
                .password("hashedPassword")
                .role(UserRole.STUDENT)
                .active(false)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserByUsername() - Should load user with special characters in email")
    void testLoadUserByUsernameWithSpecialCharactersInEmail() {
        // Arrange
        String email = "user+test.2024@example.com";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email(email)
                .password("hashedPassword")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        verify(userRepository).findByEmail(email);
    }
}
