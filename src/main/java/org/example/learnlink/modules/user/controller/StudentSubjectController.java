package org.example.learnlink.modules.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.user.dto.StudentSubjectRequest;
import org.example.learnlink.modules.user.dto.StudentSubjectResponse;
import org.example.learnlink.modules.user.service.StudentSubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for StudentSubject CRUD operations
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class StudentSubjectController {

    private final StudentSubjectService studentSubjectService;

    /**
     * Get all subjects
     * GET /api/subjects
     */
    @GetMapping
    public ResponseEntity<List<StudentSubjectResponse>> getAllSubjects() {
        List<StudentSubjectResponse> subjects = studentSubjectService.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }

    /**
     * Get subject by ID
     * GET /api/subjects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentSubjectResponse> getSubjectById(@PathVariable Long id) {
        StudentSubjectResponse subject = studentSubjectService.getSubjectById(id);
        return ResponseEntity.ok(subject);
    }

    /**
     * Create a new subject
     * POST /api/subjects
     */
    @PostMapping
    public ResponseEntity<StudentSubjectResponse> createSubject(
            @Valid @RequestBody StudentSubjectRequest request) {
        StudentSubjectResponse subject = studentSubjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subject);
    }

    /**
     * Update an existing subject
     * PUT /api/subjects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentSubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody StudentSubjectRequest request) {
        StudentSubjectResponse subject = studentSubjectService.updateSubject(id, request);
        return ResponseEntity.ok(subject);
    }

    /**
     * Delete a subject
     * DELETE /api/subjects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        studentSubjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}
