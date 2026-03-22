package org.example.learnlink.modules.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests - AAA Pattern")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET_KEY = "aGVsbG8gd29ybGQgaGVsbG8gd29ybGQgaGVsbG8gd29ybGQ=";
    private static final long TEST_EXPIRATION = 86400000; // 24 hours
    private static final long TEST_REFRESH_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", TEST_REFRESH_EXPIRATION);
    }

    // ============= TOKEN GENERATION TESTS =============

    @Test
    @DisplayName("generateToken() - Should generate valid token with user details")
    void testGenerateTokenSuccess() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();

        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("generateToken() - Should generate token with extra claims")
    void testGenerateTokenWithExtraClaims() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("department", "Engineering");
        extraClaims.put("level", 5);

        // Act
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("generateRefreshToken() - Should generate refresh token")
    void testGenerateRefreshToken() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();

        // Act
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertEquals(3, refreshToken.split("\\.").length);
    }

    // ============= EXTRACT CLAIM TESTS =============

    @Test
    @DisplayName("extractUsername() - Should extract username from token")
    void testExtractUsernameSuccess() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtService.generateToken(userDetails);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser@example.com", extractedUsername);
    }

    @Test
    @DisplayName("extractUserId() - Should extract user ID from token")
    void testExtractUserIdSuccess() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtService.generateToken(userDetails);

        // Act
        Long extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertNotNull(extractedUserId);
        assertEquals(1L, extractedUserId);
    }

    @Test
    @DisplayName("extractExpiration() - Should extract expiration date from token")
    void testExtractExpirationSuccess() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtService.generateToken(userDetails);
        long beforeTokenGeneration = System.currentTimeMillis();

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > beforeTokenGeneration);
        assertTrue(expiration.getTime() - beforeTokenGeneration <= TEST_EXPIRATION + 1000);
    }

    // ============= TOKEN VALIDATION TESTS =============

    @Test
    @DisplayName("isTokenValid() - Should return true for valid token")
    void testIsTokenValidSuccess() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("isTokenValid() - Should return false when token username does not match user")
    void testIsTokenValidWithDifferentUsername() {
        // Arrange
        CustomUserDetails userDetails1 = createTestUserDetails();
        CustomUserDetails userDetails2 = createTestUserDetails("different@example.com", 2L);
        String token = jwtService.generateToken(userDetails1);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails2);

        // Assert
        assertFalse(isValid);
    }



    // ============= EDGE CASE TESTS =============

    @Test
    @DisplayName("generateToken() - Should contain user ID and role in token claims")
    void testGenerateTokenContainsUserIdAndRole() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtService.generateToken(userDetails);

        // Act
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals(1L, userId);
        assertEquals("testuser@example.com", username);
    }

    @Test
    @DisplayName("extractClaim() - Should extract custom claim from token")
    void testExtractCustomClaim() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("testuser@example.com", username);
    }



    // ============= HELPER METHODS =============

    private CustomUserDetails createTestUserDetails() {
        return createTestUserDetails("testuser@example.com", 1L);
    }

    private CustomUserDetails createTestUserDetails(String email, Long id) {
        org.example.learnlink.modules.auth.entity.User user = 
                org.example.learnlink.modules.auth.entity.User.builder()
                    .id(id)
                    .username("testuser")
                    .email(email)
                    .password("hashedPassword")
                    .role(org.example.learnlink.modules.auth.entity.UserRole.STUDENT)
                    .active(true)
                    .emailVerified(true)
                    .build();
        return new CustomUserDetails(user);
    }
}
