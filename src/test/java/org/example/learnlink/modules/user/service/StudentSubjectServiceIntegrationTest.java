package org.example.learnlink.modules.user.service;

import org.example.learnlink.modules.user.dto.StudentSubjectRequest;
import org.example.learnlink.modules.user.dto.StudentSubjectResponse;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.repository.StudentSubjectRepository;
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
@DisplayName("StudentSubjectService Integration Tests")
class StudentSubjectServiceIntegrationTest {

    @Autowired
    private StudentSubjectService studentSubjectService;

    @Autowired
    private StudentSubjectRepository studentSubjectRepository;

    private StudentSubject testSubject;

    @BeforeEach
    void setUp() {
        // Clean up repository before each test
        studentSubjectRepository.deleteAll();
    }

    // ============= GET ALL SUBJECTS TESTS =============

    @Test
    @DisplayName("getAllSubjects() - Should retrieve all subjects")
    void testGetAllSubjectsSuccess() {
        // Arrange
        StudentSubject subject1 = StudentSubject.builder().name("Mathematics").build();
        StudentSubject subject2 = StudentSubject.builder().name("Physics").build();
        studentSubjectRepository.save(subject1);
        studentSubjectRepository.save(subject2);

        // Act
        List<StudentSubjectResponse> responses = studentSubjectService.getAllSubjects();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> "Mathematics".equals(r.getName())));
        assertTrue(responses.stream().anyMatch(r -> "Physics".equals(r.getName())));
    }

    @Test
    @DisplayName("getAllSubjects() - Should return empty list when no subjects exist")
    void testGetAllSubjectsEmpty() {
        // Act
        List<StudentSubjectResponse> responses = studentSubjectService.getAllSubjects();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("getAllSubjects() - Should return multiple subjects in consistent order")
    void testGetAllSubjectsMultiple() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            StudentSubject subject = StudentSubject.builder()
                    .name("Subject" + i)
                    .build();
            studentSubjectRepository.save(subject);
        }

        // Act
        List<StudentSubjectResponse> responses = studentSubjectService.getAllSubjects();

        // Assert
        assertEquals(5, responses.size());
    }

    // ============= GET SUBJECT BY ID TESTS =============

    @Test
    @DisplayName("getSubjectById() - Should retrieve subject by ID")
    void testGetSubjectByIdSuccess() {
        // Arrange
        StudentSubject subject = StudentSubject.builder().name("Chemistry").build();
        StudentSubject savedSubject = studentSubjectRepository.save(subject);

        // Act
        StudentSubjectResponse response = studentSubjectService.getSubjectById(savedSubject.getId());

        // Assert
        assertNotNull(response);
        assertEquals("Chemistry", response.getName());
    }

    @Test
    @DisplayName("getSubjectById() - Should throw exception when subject not found")
    void testGetSubjectByIdNotFound() {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> studentSubjectService.getSubjectById(nonExistentId)
        );
    }

    @Test
    @DisplayName("getSubjectById() - Should throw exception with correct message")
    void testGetSubjectByIdNotFoundMessage() {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> studentSubjectService.getSubjectById(nonExistentId)
        );
        assertTrue(exception.getMessage().contains("Subject not found"));
        assertTrue(exception.getMessage().contains("999"));
    }

    // ============= CREATE SUBJECT TESTS =============

    @Test
    @DisplayName("createSubject() - Should create new subject successfully")
    void testCreateSubjectSuccess() {
        // Arrange
        StudentSubjectRequest request = new StudentSubjectRequest("Biology");

        // Act
        StudentSubjectResponse response = studentSubjectService.createSubject(request);

        // Assert
        assertNotNull(response);
        assertEquals("Biology", response.getName());

        // Verify persistence
        assertTrue(studentSubjectRepository.findAll().stream()
                .anyMatch(s -> "Biology".equals(s.getName())));
    }

    @Test
    @DisplayName("createSubject() - Should create multiple subjects independently")
    void testCreateMultipleSubjectsSuccess() {
        // Arrange
        StudentSubjectRequest request1 = new StudentSubjectRequest("History");
        StudentSubjectRequest request2 = new StudentSubjectRequest("Geography");

        // Act
        StudentSubjectResponse response1 = studentSubjectService.createSubject(request1);
        StudentSubjectResponse response2 = studentSubjectService.createSubject(request2);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals("History", response1.getName());
        assertEquals("Geography", response2.getName());

        // Verify both are persisted
        List<StudentSubject> all = studentSubjectRepository.findAll();
        assertEquals(2, all.size());
    }

    // ============= UPDATE SUBJECT TESTS =============

    @Test
    @DisplayName("updateSubject() - Should update subject name successfully")
    void testUpdateSubjectSuccess() {
        // Arrange
        StudentSubject subject = StudentSubject.builder().name("Old Name").build();
        StudentSubject savedSubject = studentSubjectRepository.save(subject);

        StudentSubjectRequest updateRequest = new StudentSubjectRequest("New Name");

        // Act
        StudentSubjectResponse response = studentSubjectService.updateSubject(savedSubject.getId(), updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("New Name", response.getName());

        // Verify persistence
        StudentSubject updatedSubject = studentSubjectRepository.findById(savedSubject.getId()).orElse(null);
        assertNotNull(updatedSubject);
        assertEquals("New Name", updatedSubject.getName());
    }

    @Test
    @DisplayName("updateSubject() - Should throw exception when subject not found")
    void testUpdateSubjectNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        StudentSubjectRequest updateRequest = new StudentSubjectRequest("New Name");

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> studentSubjectService.updateSubject(nonExistentId, updateRequest)
        );
    }

    // ============= DELETE SUBJECT TESTS =============

    @Test
    @DisplayName("deleteSubject() - Should delete subject successfully")
    void testDeleteSubjectSuccess() {
        // Arrange
        StudentSubject subject = StudentSubject.builder().name("To Delete").build();
        StudentSubject savedSubject = studentSubjectRepository.save(subject);

        // Act
        studentSubjectService.deleteSubject(savedSubject.getId());

        // Assert
        assertFalse(studentSubjectRepository.existsById(savedSubject.getId()));
    }

    @Test
    @DisplayName("deleteSubject() - Should throw exception when subject not found")
    void testDeleteSubjectNotFound() {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> studentSubjectService.deleteSubject(nonExistentId)
        );
    }

    // ============= INTEGRATION TESTS =============

    @Test
    @DisplayName("Subject workflow - Create, retrieve, update, delete")
    void testCompleteSubjectWorkflow() {
        // Arrange - Create
        StudentSubjectRequest createRequest = new StudentSubjectRequest("Literature");

        // Act - Create
        StudentSubjectResponse createdResponse = studentSubjectService.createSubject(createRequest);
        Long subjectId = createdResponse.getId();

        // Assert - Created
        assertNotNull(createdResponse);
        assertEquals("Literature", createdResponse.getName());

        // Act - Retrieve
        StudentSubjectResponse retrievedResponse = studentSubjectService.getSubjectById(subjectId);

        // Assert - Retrieved
        assertEquals("Literature", retrievedResponse.getName());

        // Act - Update
        StudentSubjectRequest updateRequest = new StudentSubjectRequest("English Literature");
        StudentSubjectResponse updatedResponse = studentSubjectService.updateSubject(subjectId, updateRequest);

        // Assert - Updated
        assertEquals("English Literature", updatedResponse.getName());

        // Verify updated in database
        StudentSubjectResponse verifyResponse = studentSubjectService.getSubjectById(subjectId);
        assertEquals("English Literature", verifyResponse.getName());

        // Act - Delete
        studentSubjectService.deleteSubject(subjectId);

        // Assert - Deleted
        assertThrows(
                RuntimeException.class,
                () -> studentSubjectService.getSubjectById(subjectId)
        );
    }

    @Test
    @DisplayName("getAllSubjects() - Should reflect all CRUD operations")
    void testGetAllSubjectsReflectsCrudOperations() {
        // Arrange & Act
        studentSubjectService.createSubject(new StudentSubjectRequest("Math"));
        studentSubjectService.createSubject(new StudentSubjectRequest("Science"));

        // Assert first count
        assertEquals(2, studentSubjectService.getAllSubjects().size());

        // Create another
        studentSubjectService.createSubject(new StudentSubjectRequest("History"));

        // Assert updated count
        assertEquals(3, studentSubjectService.getAllSubjects().size());
    }
}
