package org.example.learnlink.modules.matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.matching.dto.response.MatchSuggestionResponse;
import org.example.learnlink.modules.matching.entity.enums.RequestStatus;
import org.example.learnlink.modules.matching.repository.ConnectionRepository;
import org.example.learnlink.modules.matching.repository.ConnectionRequestRepository;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the matching algorithm service.
 * Provides study partner suggestions based on compatibility scoring.
 * 
 * Compatibility Algorithm:
 * - 60% Common subjects (weighted higher as primary matching criteria)
 * - 40% Academic level proximity
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MatchingServiceImpl implements IMatchingService {

    private final UserProfileRepository userProfileRepository;
    private final ConnectionRepository connectionRepository;
    private final ConnectionRequestRepository connectionRequestRepository;

    // Weights for compatibility scoring
    private static final BigDecimal SUBJECT_WEIGHT = new BigDecimal("0.60");
    private static final BigDecimal LEVEL_WEIGHT = new BigDecimal("0.40");

    @Override
    public List<MatchSuggestionResponse> getSuggestions(Long userId, int limit) {
        log.info("Getting match suggestions for user: {}, limit: {}", userId, limit);

        // Get current user profile
        UserProfile currentUser = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        // Get IDs to exclude (self + already connected users)
        List<Long> excludedUserIds = getExcludedUserIds(userId);

        // Get candidate profiles
        List<UserProfile> candidates = getCandidates(currentUser, excludedUserIds);

        if (candidates.isEmpty()) {
            log.info("No candidates found for user {}", userId);
            return Collections.emptyList();
        }

        // Get pending request info for UI display
        Set<Long> pendingRequestUserIds = getPendingRequestUserIds(userId);

        // Calculate compatibility and build suggestions
        List<MatchSuggestionResponse> suggestions = candidates.stream()
                .map(candidate -> buildSuggestion(currentUser, candidate, pendingRequestUserIds))
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getCompatibilityScore().compareTo(a.getCompatibilityScore()))
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Found {} suggestions for user {}", suggestions.size(), userId);
        return suggestions;
    }

    @Override
    public BigDecimal calculateCompatibility(Long user1Id, Long user2Id) {
        UserProfile profile1 = userProfileRepository.findByUserId(user1Id).orElse(null);
        UserProfile profile2 = userProfileRepository.findByUserId(user2Id).orElse(null);

        if (profile1 == null || profile2 == null) {
            return BigDecimal.ZERO;
        }

        return calculateCompatibilityScore(profile1, profile2);
    }

    @Override
    public List<MatchSuggestionResponse> getSuggestionsBySubject(Long userId, Long subjectId, int limit) {
        log.info("Getting suggestions for user {} filtered by subject {}", userId, subjectId);

        UserProfile currentUser = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        List<Long> excludedUserIds = getExcludedUserIds(userId);

        // Get candidates with specific subject
        List<UserProfile> candidates = userProfileRepository.findBySubject(subjectId, excludedUserIds);

        Set<Long> pendingRequestUserIds = getPendingRequestUserIds(userId);

        return candidates.stream()
                .map(candidate -> buildSuggestion(currentUser, candidate, pendingRequestUserIds))
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getCompatibilityScore().compareTo(a.getCompatibilityScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== Private Methods ====================

    /**
     * Get list of user IDs to exclude from suggestions.
     * Includes: self, already connected users
     */
    private List<Long> getExcludedUserIds(Long userId) {
        List<Long> excludedIds = new ArrayList<>();
        excludedIds.add(userId); // Exclude self

        // Add connected users
        List<Long> connectedUserIds = connectionRepository.findConnectedUserIds(userId);
        excludedIds.addAll(connectedUserIds);

        return excludedIds;
    }

    /**
     * Get candidate profiles for matching.
     * Prioritizes users with similar subjects.
     */
    private List<UserProfile> getCandidates(UserProfile currentUser, List<Long> excludedUserIds) {
        // Get subject IDs from current user
        Set<Long> subjectIds = currentUser.getSubjects().stream()
                .map(StudentSubject::getId)
                .collect(Collectors.toSet());

        if (subjectIds.isEmpty()) {
            // If user has no subjects, return all non-excluded profiles
            return userProfileRepository.findAllExcluding(excludedUserIds);
        }

        // Get users with similar subjects
        return userProfileRepository.findBySimilarSubjects(subjectIds, excludedUserIds);
    }

    /**
     * Get user IDs with pending connection requests (sent or received).
     */
    private Set<Long> getPendingRequestUserIds(Long userId) {
        Set<Long> pendingUserIds = new HashSet<>();

        // Sent requests
        connectionRequestRepository.findBySenderIdAndStatus(userId, RequestStatus.PENDING)
                .forEach(req -> pendingUserIds.add(req.getReceiverId()));

        // Received requests
        connectionRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING)
                .forEach(req -> pendingUserIds.add(req.getSenderId()));

        return pendingUserIds;
    }

    /**
     * Build a suggestion response for a candidate.
     */
    private MatchSuggestionResponse buildSuggestion(UserProfile currentUser,
                                                     UserProfile candidate,
                                                     Set<Long> pendingRequestUserIds) {
        // Calculate scores
        BigDecimal compatibilityScore = calculateCompatibilityScore(currentUser, candidate);
        List<String> commonSubjects = findCommonSubjects(currentUser, candidate);
        int subjectMatchPct = calculateSubjectMatchPercentage(currentUser, candidate);
        int levelMatchPct = calculateLevelMatchPercentage(currentUser, candidate);

        return MatchSuggestionResponse.builder()
                .userId(candidate.getUserId())
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .profilePictureUrl(candidate.getProfilePictureUrl())
                .bio(candidate.getBio())
                .academicLevel(candidate.getAcademicLevel() != null
                        ? candidate.getAcademicLevel().name()
                        : null)
                .compatibilityScore(compatibilityScore)
                .commonSubjects(commonSubjects)
                .subjectMatchPercentage(subjectMatchPct)
                .levelMatchPercentage(levelMatchPct)
                .hasPendingRequest(pendingRequestUserIds.contains(candidate.getUserId()))
                .isConnected(false) // Already filtered out connected users
                .build();
    }

    /**
     * Calculate overall compatibility score between two users.
     */
    private BigDecimal calculateCompatibilityScore(UserProfile user1, UserProfile user2) {
        // Subject score (0-100)
        BigDecimal subjectScore = BigDecimal.valueOf(calculateSubjectMatchPercentage(user1, user2));

        // Level score (0-100)
        BigDecimal levelScore = BigDecimal.valueOf(calculateLevelMatchPercentage(user1, user2));

        // Weighted average
        BigDecimal totalScore = subjectScore.multiply(SUBJECT_WEIGHT)
                .add(levelScore.multiply(LEVEL_WEIGHT))
                .setScale(2, RoundingMode.HALF_UP);

        return totalScore;
    }

    /**
     * Calculate subject match percentage.
     * Based on: (common subjects / user1's subjects) * 100
     */
    private int calculateSubjectMatchPercentage(UserProfile user1, UserProfile user2) {
        if (user1.getSubjects() == null || user1.getSubjects().isEmpty() ||
                user2.getSubjects() == null || user2.getSubjects().isEmpty()) {
            return 0;
        }

        Set<Long> subjects1 = user1.getSubjects().stream()
                .map(StudentSubject::getId)
                .collect(Collectors.toSet());

        Set<Long> subjects2 = user2.getSubjects().stream()
                .map(StudentSubject::getId)
                .collect(Collectors.toSet());

        // Count common subjects
        long commonCount = subjects1.stream()
                .filter(subjects2::contains)
                .count();

        // Calculate percentage based on user1's subjects
        return (int) ((commonCount * 100) / subjects1.size());
    }

    /**
     * Calculate academic level match percentage.
     * Closer levels = higher score.
     * Same level = 100%, 1 level diff = 75%, 2 = 50%, 3 = 25%, 4+ = 10%
     */
    private int calculateLevelMatchPercentage(UserProfile user1, UserProfile user2) {
        if (user1.getAcademicLevel() == null || user2.getAcademicLevel() == null) {
            return 50; // Neutral score if level not specified
        }

        int level1 = user1.getAcademicLevel().ordinal();
        int level2 = user2.getAcademicLevel().ordinal();
        int diff = Math.abs(level1 - level2);

        return switch (diff) {
            case 0 -> 100;
            case 1 -> 75;
            case 2 -> 50;
            case 3 -> 25;
            default -> 10;
        };
    }

    /**
     * Find common subjects between two users.
     */
    private List<String> findCommonSubjects(UserProfile user1, UserProfile user2) {
        if (user1.getSubjects() == null || user2.getSubjects() == null) {
            return Collections.emptyList();
        }

        Set<Long> subjects2Ids = user2.getSubjects().stream()
                .map(StudentSubject::getId)
                .collect(Collectors.toSet());

        return user1.getSubjects().stream()
                .filter(s -> subjects2Ids.contains(s.getId()))
                .map(StudentSubject::getName)
                .collect(Collectors.toList());
    }
}
