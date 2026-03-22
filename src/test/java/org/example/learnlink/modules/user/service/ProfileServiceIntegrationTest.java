package org.example.learnlink.modules.user.service;

import org.example.learnlink.modules.user.dto.UserProfileCreate;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.dto.UserProfileUpdate;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.entity.AcademicLevel;
import org.example.learnlink.modules.user.repository.StudentSubjectRepository;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileService Integration Tests")
class ProfileServiceIntegrationTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private StudentSubjectRepository studentSubjectRepository;

    private Long testUserId;
    private StudentSubject testSubject1;
    private StudentSubject testSubject2;

    @BeforeEach
    void setUp() {
        testUserId = 1L;

        // Create test subjects
        testSubject1 = StudentSubject.builder().name("Mathematics").build();
        testSubject2 = StudentSubject.builder().name("Physics").build();
        testSubject1 = studentSubjectRepository.save(testSubject1);
        testSubject2 = studentSubjectRepository.save(testSubject2);

        // Clean up any existing profiles for test user
        userProfileRepository.findByUserId(testUserId).ifPresent(userProfileRepository::delete);
    }

    // ============= CREATE PROFILE TESTS =============

    @Test
    @DisplayName("create() - Should create user profile with subjects successfully")
    void testCreateProfileSuccess() {
        // Arrange
        UserProfileCreate request = new UserProfileCreate(
                "John",
                "Doe",
                "Mathematics and Physics enthusiast",
                null,
                List.of(testSubject1.getId(), testSubject2.getId()),
                AcademicLevel.BACHELOR
        );

        // Act
        UserProfileResponse response = profileService.create(testUserId, request, null);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("Mathematics and Physics enthusiast", response.bio());
        assertEquals(AcademicLevel.BACHELOR, response.academicLevel());

        // Verify persistence
        UserProfile savedProfile = userProfileRepository.findByUserId(testUserId).orElse(null);
        assertNotNull(savedProfile);
        assertEquals("John", savedProfile.getFirstName());
        assertEquals("Doe", savedProfile.getLastName());
    }

    @Test
    @DisplayName("create() - Should create profile without subjects")
    void testCreateProfileWithoutSubjects() {
        // Arrange
        UserProfileCreate request = new UserProfileCreate(
                "Jane",
                "Smith",
                "Learning everything",
                null,
                List.of(),
                AcademicLevel.MASTER
        );

        // Act
        UserProfileResponse response = profileService.create(testUserId, request, null);

        // Assert
        assertNotNull(response);
        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals(AcademicLevel.MASTER, response.academicLevel());
    }

    // ============= GET PROFILE TESTS =============

    @Test
    @DisplayName("getProfileByUserId() - Should retrieve existing profile")
    void testGetProfileByUserIdSuccess() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .bio("Test bio")
                .academicLevel(AcademicLevel.BACHELOR)
                .subjects(List.of(testSubject1, testSubject2))
                .build();
        userProfileRepository.save(profile);

        // Act
        UserProfileResponse response = profileService.getProfileByUserId(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(AcademicLevel.BACHELOR, response.academicLevel());
    }

    @Test
    @DisplayName("getProfileByUserId() - Should throw exception when profile not found")
    void testGetProfileByUserIdNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> profileService.getProfileByUserId(nonExistentUserId)
        );
    }

    @Test
    @DisplayName("getMyProfile() - Should retrieve current user's profile")
    void testGetMyProfileSuccess() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .bio("My profile")
                .academicLevel(AcademicLevel.BACHELOR)
                .build();
        userProfileRepository.save(profile);

        // Act
        UserProfileResponse response = profileService.getMyProfile(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
    }

    // ============= UPDATE PROFILE TESTS =============

    @Test
    @DisplayName("updateProfile() - Should update profile fields")
    void testUpdateProfileSuccess() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .bio("Old bio")
                .academicLevel(AcademicLevel.BACHELOR)
                .subjects(List.of(testSubject1))
                .build();
        userProfileRepository.save(profile);

        UserProfileUpdate updateRequest = new UserProfileUpdate(
                "Jane",
                "Smith",
                "New bio",
                List.of(testSubject2.getId()),
                AcademicLevel.MASTER
        );

        // Act
        UserProfileResponse response = profileService.updateProfile(testUserId, updateRequest, null);

        // Assert
        assertNotNull(response);
        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals("New bio", response.bio());
        assertEquals(AcademicLevel.MASTER, response.academicLevel());

        // Verify persistence
        UserProfile updatedProfile = userProfileRepository.findByUserId(testUserId).orElse(null);
        assertNotNull(updatedProfile);
        assertEquals("Jane", updatedProfile.getFirstName());
        assertEquals("Smith", updatedProfile.getLastName());
    }

    @Test
    @DisplayName("updateProfile() - Should update only provided fields")
    void testUpdateProfilePartialUpdate() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .bio("Old bio")
                .academicLevel(AcademicLevel.BACHELOR)
                .build();
        userProfileRepository.save(profile);

        UserProfileUpdate updateRequest = new UserProfileUpdate(
                "Jane",
                null,
                null,
                null,
                null
        );

        // Act
        UserProfileResponse response = profileService.updateProfile(testUserId, updateRequest, null);

        // Assert
        assertEquals("Jane", response.firstName());
        assertEquals("Doe", response.lastName()); // Should remain unchanged
        assertEquals("Old bio", response.bio()); // Should remain unchanged
    }

    @Test
    @DisplayName("updateProfile() - Should throw exception when profile not found")
    void testUpdateProfileNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        UserProfileUpdate updateRequest = new UserProfileUpdate(
                "John", null, null, null, null
        );

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> profileService.updateProfile(nonExistentUserId, updateRequest, null)
        );
    }

    @Test
    @DisplayName("updateProfile() - Should update subjects")
    void testUpdateProfileSubjects() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .bio("Bio")
                .subjects(List.of(testSubject1))
                .build();
        userProfileRepository.save(profile);

        UserProfileUpdate updateRequest = new UserProfileUpdate(
                null,
                null,
                null,
                List.of(testSubject2.getId()),
                null
        );

        // Act
        UserProfileResponse response = profileService.updateProfile(testUserId, updateRequest, null);

        // Assert
        assertNotNull(response);
        // Verify only testSubject2 is associated
        UserProfile updatedProfile = userProfileRepository.findByUserId(testUserId).orElse(null);
        assertNotNull(updatedProfile);
        assertEquals(1, updatedProfile.getSubjects().size());
    }
}
