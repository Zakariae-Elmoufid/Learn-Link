package org.example.learnlink.modules.matching.service;

import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.matching.dto.response.MatchSuggestionResponse;
import org.example.learnlink.modules.matching.repository.ConnectionRepository;
import org.example.learnlink.modules.matching.repository.ConnectionRequestRepository;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.entity.AcademicLevel;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.example.learnlink.modules.user.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.learnlink.modules.user.dto.StudentSubjectResponse;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchingService Unit Tests - AAA Pattern")
class MatchingServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private ProfileService profileService;

    private MatchingServiceImpl matchingService;

    private static final AcademicLevel DEFAULT_LEVEL = AcademicLevel.BACHELOR;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingServiceImpl(
                userProfileRepository,
                connectionRepository,
                connectionRequestRepository,
                profileService
        );
    }

    // ============= GET SUGGESTIONS TESTS =============

    @Test
    @DisplayName("getSuggestions() - Should return match suggestions for valid user")
    void testGetSuggestionsSuccess() {
        // Arrange
        Long userId = 1L;
        int limit = 5;
        
        UserProfile currentUser = createUserProfile(userId, "John", "Doe", AcademicLevel.BACHELOR);
        currentUser.setSubjects(createSubjects(1L, 2L, 3L));

        UserProfile candidate1 = createUserProfile(2L, "Jane", "Smith", AcademicLevel.BACHELOR);
        candidate1.setSubjects(createSubjects(1L, 2L));

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(currentUser));
        when(connectionRepository.findConnectedUserIds(userId)).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findBySenderIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findByReceiverIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(userProfileRepository.findBySimilarSubjects(any(), any())).thenReturn(List.of(candidate1));
        when(profileService.toPresignedUrl(anyString())).thenReturn("https://presigned-url.com");

        // Act
        List<MatchSuggestionResponse> suggestions = matchingService.getSuggestions(userId, limit);

        // Assert
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        verify(userProfileRepository).findByUserId(userId);
        verify(connectionRepository).findConnectedUserIds(userId);
    }

    @Test
    @DisplayName("getSuggestions() - Should throw exception when user not found")
    void testGetSuggestionsUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> matchingService.getSuggestions(userId, 5)
        );
        verify(userProfileRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("getSuggestions() - Should return empty list when no candidates found")
    void testGetSuggestionsNoCandidates() {
        // Arrange
        Long userId = 1L;
        UserProfile currentUser = createUserProfile(userId, "John", "Doe", AcademicLevel.BACHELOR);
        currentUser.setSubjects(createSubjects(1L, 2L));

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(currentUser));
        when(connectionRepository.findConnectedUserIds(userId)).thenReturn(Collections.emptyList());
        when(userProfileRepository.findBySimilarSubjects(any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<MatchSuggestionResponse> suggestions = matchingService.getSuggestions(userId, 5);

        // Assert
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    @DisplayName("getSuggestions() - Should respect limit parameter")
    void testGetSuggestionsRespectLimit() {
        // Arrange
        Long userId = 1L;
        int limit = 2;
        
        UserProfile currentUser = createUserProfile(userId, "John", "Doe", AcademicLevel.BACHELOR);
        currentUser.setSubjects(createSubjects(1L));

        List<UserProfile> candidates = new ArrayList<>();
        for (int i = 2; i <= 5; i++) {
            UserProfile candidate = createUserProfile((long)i, "Candidate" + i, "Test", AcademicLevel.BACHELOR);
            candidate.setSubjects(createSubjects(1L));
            candidates.add(candidate);
        }

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(currentUser));
        when(connectionRepository.findConnectedUserIds(userId)).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findBySenderIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findByReceiverIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(userProfileRepository.findBySimilarSubjects(any(), any())).thenReturn(candidates);
        when(profileService.toPresignedUrl(anyString())).thenReturn("https://presigned-url.com");

        // Act
        List<MatchSuggestionResponse> suggestions = matchingService.getSuggestions(userId, limit);

        // Assert
        assertEquals(limit, suggestions.size());
    }

    // ============= CALCULATE COMPATIBILITY TESTS =============

    @Test
    @DisplayName("calculateCompatibility() - Should return zero when users not found")
    void testCalculateCompatibilityUsersNotFound() {
        // Arrange
        Long user1Id = 1L;
        Long user2Id = 2L;
        when(userProfileRepository.findByUserId(user1Id)).thenReturn(Optional.empty());
        when(userProfileRepository.findByUserId(user2Id)).thenReturn(Optional.empty());

        // Act
        BigDecimal compatibility = matchingService.calculateCompatibility(user1Id, user2Id);

        // Assert
        assertEquals(BigDecimal.ZERO, compatibility);
    }

    @Test
    @DisplayName("calculateCompatibility() - Should calculate score for valid users")
    void testCalculateCompatibilitySuccess() {
        // Arrange
        Long user1Id = 1L;
        Long user2Id = 2L;

        UserProfile user1 = createUserProfile(user1Id, "User1", "Test", AcademicLevel.BACHELOR);
        user1.setSubjects(createSubjects(1L, 2L, 3L));

        UserProfile user2 = createUserProfile(user2Id, "User2", "Test", AcademicLevel.BACHELOR);
        user2.setSubjects(createSubjects(1L, 2L));

        when(userProfileRepository.findByUserId(user1Id)).thenReturn(Optional.of(user1));
        when(userProfileRepository.findByUserId(user2Id)).thenReturn(Optional.of(user2));

        // Act
        BigDecimal compatibility = matchingService.calculateCompatibility(user1Id, user2Id);

        // Assert
        assertNotNull(compatibility);
        assertGreaterThanOrEqual(compatibility, BigDecimal.ZERO);
        assertLessThanOrEqual(compatibility, new BigDecimal("100"));
    }

    // ============= GET SUGGESTIONS BY SUBJECT TESTS =============

    @Test
    @DisplayName("getSuggestionsBySubject() - Should return suggestions filtered by subject")
    void testGetSuggestionsBySubjectSuccess() {
        // Arrange
        Long userId = 1L;
        Long subjectId = 1L;
        int limit = 5;

        UserProfile currentUser = createUserProfile(userId, "John", "Doe", AcademicLevel.BACHELOR);
        currentUser.setSubjects(createSubjects(subjectId, 2L));

        UserProfile candidate = createUserProfile(2L, "Jane", "Smith", AcademicLevel.BACHELOR);
        candidate.setSubjects(createSubjects(subjectId));

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(currentUser));
        when(connectionRepository.findConnectedUserIds(userId)).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findBySenderIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findByReceiverIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(userProfileRepository.findBySubject(subjectId, Collections.singletonList(userId)))
                .thenReturn(List.of(candidate));
        when(profileService.toPresignedUrl(anyString())).thenReturn("https://presigned-url.com");

        // Act
        List<MatchSuggestionResponse> suggestions = matchingService.getSuggestionsBySubject(userId, subjectId, limit);

        // Assert
        assertNotNull(suggestions);
        verify(userProfileRepository).findByUserId(userId);
        verify(userProfileRepository).findBySubject(eq(subjectId), any());
    }

    @Test
    @DisplayName("getSuggestionsBySubject() - Should throw exception when user not found")
    void testGetSuggestionsBySubjectUserNotFound() {
        // Arrange
        Long userId = 999L;
        Long subjectId = 1L;
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> matchingService.getSuggestionsBySubject(userId, subjectId, 5)
        );
    }

    @Test
    @DisplayName("getSuggestionsBySubject() - Should return suggestions sorted by compatibility descending")
    void testGetSuggestionsBySubjectSorted() {
        // Arrange
        Long userId = 1L;
        Long subjectId = 1L;

        UserProfile currentUser = createUserProfile(userId, "John", "Doe", AcademicLevel.BACHELOR);
        currentUser.setSubjects(createSubjects(subjectId));

        UserProfile candidate1 = createUserProfile(2L, "Candidate1", "Test", AcademicLevel.BACHELOR);
        candidate1.setSubjects(createSubjects(subjectId));

        UserProfile candidate2 = createUserProfile(3L, "Candidate2", "Test", AcademicLevel.MASTER);
        candidate2.setSubjects(createSubjects(subjectId));

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(currentUser));
        when(connectionRepository.findConnectedUserIds(userId)).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findBySenderIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(connectionRequestRepository.findByReceiverIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(userProfileRepository.findBySubject(eq(subjectId), any())).thenReturn(List.of(candidate1, candidate2));
        when(profileService.toPresignedUrl(anyString())).thenReturn("https://presigned-url.com");

        // Act
        List<MatchSuggestionResponse> suggestions = matchingService.getSuggestionsBySubject(userId, subjectId, 10);

        // Assert
        assertNotNull(suggestions);
        if (suggestions.size() >= 2) {
            assertTrue(suggestions.get(0).getCompatibilityScore()
                    .compareTo(suggestions.get(1).getCompatibilityScore()) >= 0);
        }
    }

    // ============= HELPER METHODS =============

    private UserProfile createUserProfile(Long userId, String firstName, String lastName, AcademicLevel level) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setAcademicLevel(level);
        profile.setProfilePictureUrl("https://example.com/pic.jpg");
        profile.setBio("Test bio");
        return profile;
    }

    private List<StudentSubject> createSubjects(Long... subjectIds) {
        List<StudentSubject> subjects = new ArrayList<>();
        for (Long subjectId : subjectIds) {
            StudentSubject subject = new StudentSubject();
            subject.setId(subjectId);
            subjects.add(subject);
        }
        return subjects;
    }

    private void assertGreaterThanOrEqual(BigDecimal actual, BigDecimal expected) {
        assertTrue(actual.compareTo(expected) >= 0, 
                "Expected " + actual + " to be >= " + expected);
    }

    private void assertLessThanOrEqual(BigDecimal actual, BigDecimal expected) {
        assertTrue(actual.compareTo(expected) <= 0, 
                "Expected " + actual + " to be <= " + expected);
    }

    private UserProfileResponse createUserProfileResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getBio(),
                profile.getProfilePictureUrl(),
                Collections.emptyList(),
                profile.getAcademicLevel()
        );
    }
}
