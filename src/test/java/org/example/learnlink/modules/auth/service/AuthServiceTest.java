package org.example.learnlink.modules.auth.service;

import org.example.learnlink.modules.auth.dto.AuthResponse;
import org.example.learnlink.modules.auth.dto.LoginRequest;
import org.example.learnlink.modules.auth.dto.RegisterRequest;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.entity.UserRole;
import org.example.learnlink.modules.auth.event.OnUserRegisteredEvent;
import org.example.learnlink.modules.auth.exception.AuthenticationException;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests - AAA Pattern")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                userDetailsService,
                eventPublisher
        );
    }

    // ============= REGISTER METHOD TESTS =============

    @Test
    @DisplayName("register() - Should successfully register a new user")
    void testRegisterSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password("encodedPassword")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(false)
                .build();
        savedUser.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        authService.register(request);

        // Assert
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByUsername(request.getUsername());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any(OnUserRegisteredEvent.class));
    }

    @Test
    @DisplayName("register() - Should throw exception when email already registered")
    void testRegisterWithDuplicateEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "existing@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.register(request)
        );
        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - Should throw exception when username already taken")
    void testRegisterWithDuplicateUsername() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existinguser", "new@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.register(request)
        );
        assertEquals("Username already taken", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ============= VERIFY METHOD TESTS =============

    @Test
    @DisplayName("verify() - Should successfully verify user email with valid code")
    void testVerifySuccess() {
        // Arrange
        String verificationCode = "valid-code-123";
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .verificationCode(verificationCode)
                .emailVerified(false)
                .build();

        when(userRepository.findByVerificationCode(verificationCode)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        boolean result = authService.verify(verificationCode);

        // Assert
        assertTrue(result);
        verify(userRepository).findByVerificationCode(verificationCode);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("verify() - Should return false when verification code not found")
    void testVerifyWithInvalidCode() {
        // Arrange
        String invalidCode = "invalid-code";
        when(userRepository.findByVerificationCode(invalidCode)).thenReturn(Optional.empty());

        // Act
        boolean result = authService.verify(invalidCode);

        // Assert
        assertFalse(result);
        verify(userRepository).findByVerificationCode(invalidCode);
        verify(userRepository, never()).save(any());
    }

    // ============= LOGIN METHOD TESTS =============

    @Test
    @DisplayName("login() - Should successfully login with valid credentials")
    void testLoginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email(request.getEmail())
                .password("encodedPassword")
                .role(UserRole.STUDENT)
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refreshToken");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        verify(userRepository).findByEmail(request.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(CustomUserDetails.class));
        verify(jwtService).generateRefreshToken(any(CustomUserDetails.class));
    }

    @Test
    @DisplayName("login() - Should throw exception when user not found")
    void testLoginUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );
        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("login() - Should throw exception when email not verified")
    void testLoginEmailNotVerified() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = User.builder()
                .email(request.getEmail())
                .active(true)
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );
        assertTrue(exception.getMessage().contains("Email not verified"));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("login() - Should throw exception when account is deactivated")
    void testLoginAccountDeactivated() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = User.builder()
                .email(request.getEmail())
                .emailVerified(true)
                .active(false)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );
        assertTrue(exception.getMessage().contains("Account is deactivated"));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("login() - Should throw exception with invalid credentials")
    void testLoginInvalidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        User user = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }
}
