package org.example.learnlink.modules.user.repository;

import org.example.learnlink.modules.user.entity.StudentSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSubjectRepository extends JpaRepository<StudentSubject, Long> {
    
    // Admin statistics queries
    
    /**
     * Find top subjects by number of students enrolled
     */
    @Query(value = "SELECT s.name, COUNT(up.id) as student_count " +
           "FROM student_subject s " +
           "LEFT JOIN user_profile_subjects ups ON s.id = ups.subjects_id " +
           "LEFT JOIN user_profile up ON ups.user_profile_id = up.id " +
           "GROUP BY s.id, s.name " +
           "ORDER BY student_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopSubjectsWithCount(@Param("limit") int limit);
    
    /**
     * Count total student-subject associations
     */
    @Query(value = "SELECT COUNT(*) FROM user_profile_subjects", nativeQuery = true)
    long countTotalStudentSubjectAssociations();
}
