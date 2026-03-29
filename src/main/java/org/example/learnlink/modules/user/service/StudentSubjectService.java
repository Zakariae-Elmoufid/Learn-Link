package org.example.learnlink.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.user.dto.StudentSubjectRequest;
import org.example.learnlink.modules.user.dto.StudentSubjectResponse;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.repository.StudentSubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for StudentSubject CRUD operations
 */
@Service
@RequiredArgsConstructor
public class StudentSubjectService {

    private final StudentSubjectRepository studentSubjectRepository;

    /**
     * Get all subjects
     */
    public List<StudentSubjectResponse> getAllSubjects() {
        return studentSubjectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get subject by ID
     */
    public StudentSubjectResponse getSubjectById(Long id) {
        StudentSubject subject = studentSubjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
        return toResponse(subject);
    }

    /**
     * Create a new subject
     */
    @Transactional
    public StudentSubjectResponse createSubject(StudentSubjectRequest request) {
        StudentSubject subject = StudentSubject.builder()
                .name(request.name())
                .build();
        StudentSubject saved = studentSubjectRepository.save(subject);
        return toResponse(saved);
    }

    /**
     * Update an existing subject
     */
    @Transactional
    public StudentSubjectResponse updateSubject(Long id, StudentSubjectRequest request) {
        StudentSubject subject = studentSubjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
        
        subject.setName(request.name());
        StudentSubject updated = studentSubjectRepository.save(subject);
        return toResponse(updated);
    }

    /**
     * Delete a subject
     */
    @Transactional
    public void deleteSubject(Long id) {
        if (!studentSubjectRepository.existsById(id)) {
            throw new RuntimeException("Subject not found with id: " + id);
        }
        studentSubjectRepository.deleteById(id);
    }

    /**
     * Convert entity to response DTO
     */
    private StudentSubjectResponse toResponse(StudentSubject subject) {
        return StudentSubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .build();
    }
}
