package org.example.learnlink.modules.user.repository;

import org.example.learnlink.modules.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(long userId);

    /**
     * Find all user profiles except the excluded user IDs.
     * Used for matching suggestions.
     *
     * @param excludedUserIds user IDs to exclude (self + already connected)
     * @return list of potential match profiles
     */
    @Query("SELECT up FROM UserProfile up WHERE up.userId NOT IN :excludedUserIds")
    List<UserProfile> findAllExcluding(@Param("excludedUserIds") List<Long> excludedUserIds);

    /**
     * Find user profiles that have at least one subject in common.
     * Excludes specified user IDs.
     *
     * @param subjectIds      set of subject IDs to match
     * @param excludedUserIds user IDs to exclude
     * @param limit           maximum number of results
     * @return list of matching profiles
     */
    @Query("SELECT DISTINCT up FROM UserProfile up " +
            "JOIN up.subjects s " +
            "WHERE s.id IN :subjectIds " +
            "AND up.userId NOT IN :excludedUserIds")
    List<UserProfile> findBySimilarSubjects(
            @Param("subjectIds") Set<Long> subjectIds,
            @Param("excludedUserIds") List<Long> excludedUserIds);

    /**
     * Find user profiles by a specific subject.
     *
     * @param subjectId       the subject ID to filter by
     * @param excludedUserIds user IDs to exclude
     * @return list of profiles with the specified subject
     */
    @Query("SELECT DISTINCT up FROM UserProfile up " +
            "JOIN up.subjects s " +
            "WHERE s.id = :subjectId " +
            "AND up.userId NOT IN :excludedUserIds")
    List<UserProfile> findBySubject(
            @Param("subjectId") Long subjectId,
            @Param("excludedUserIds") List<Long> excludedUserIds);

    /**
     * Find multiple user profiles by user IDs.
     *
     * @param userIds list of user IDs
     * @return list of user profiles
     */
    @Query("SELECT up FROM UserProfile up WHERE up.userId IN :userIds")
    List<UserProfile> findByUserIds(@Param("userIds") List<Long> userIds);


}

