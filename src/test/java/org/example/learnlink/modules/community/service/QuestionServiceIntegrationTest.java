package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AskQuestionRequest;
import org.example.learnlink.modules.community.dto.QuestionResponse;
import org.example.learnlink.modules.community.entity.Question;
import org.example.learnlink.modules.community.mapper.QuestionMapper;
import org.example.learnlink.modules.community.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for QuestionService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class QuestionServiceIntegrationTest {

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    private AskQuestionRequest askQuestionRequest;

    @BeforeEach
    public void setUp() {
        // Clear database before each test
        questionRepository.deleteAll();

        askQuestionRequest = AskQuestionRequest.builder()
            .title("How to solve quadratic equations?")
            .content("I am struggling with solving quadratic equations. Can someone help me understand the process?")
            .build();
    }

    @Test
    public void testAskQuestion_Success() {
        QuestionResponse response = questionService.askQuestion(1L, askQuestionRequest);

        assertNotNull(response.getId());
        assertEquals("How to solve quadratic equations?", response.getTitle());
        assertEquals(1L, response.getUserId());
        assertEquals(false, response.getIsResolved());
        assertNull(response.getAcceptedAnswerId());
    }

    @Test
    public void testGetQuestionById_Success() {
        // Create a question first
        QuestionResponse created = questionService.askQuestion(1L, askQuestionRequest);

        // Retrieve it
        QuestionResponse retrieved = questionService.getQuestionById(created.getId());

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals("How to solve quadratic equations?", retrieved.getTitle());
    }

    @Test
    public void testGetQuestionById_IncrementViewCount() {
        QuestionResponse created = questionService.askQuestion(1L, askQuestionRequest);

        // Get view count before
        assertEquals(0L, created.getViewCount());

        // Retrieve the question
        QuestionResponse retrieved = questionService.getQuestionById(created.getId());

        // View count should increment on retrieval
        assertEquals(1L, retrieved.getViewCount());
    }

    @Test
    public void testGetUnresolvedQuestions() {
        // Create an unresolved question
        questionService.askQuestion(1L, askQuestionRequest);

        // Create another unresolved question
        AskQuestionRequest request2 = AskQuestionRequest.builder()
            .title("Another question?")
            .content("This is another question with sufficient content length for validation")
            .build();
        questionService.askQuestion(2L, request2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionResponse> unresolvedQuestions = questionService.getUnresolvedQuestions(pageable);

        assertEquals(2, unresolvedQuestions.getContent().size());
        assertTrue(unresolvedQuestions.getContent().stream()
            .allMatch(q -> !q.getIsResolved()));
    }

    @Test
    public void testGetUserQuestions() {
        // User 1 creates 2 questions
        questionService.askQuestion(1L, askQuestionRequest);

        AskQuestionRequest request2 = AskQuestionRequest.builder()
            .title("Different question?")
            .content("This is a different question with sufficient content to pass validation")
            .build();
        questionService.askQuestion(1L, request2);

        // User 2 creates 1 question
        questionService.askQuestion(2L, askQuestionRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionResponse> user1Questions = questionService.getUserQuestions(1L, pageable);

        assertEquals(2, user1Questions.getContent().size());
        assertTrue(user1Questions.getContent().stream()
            .allMatch(q -> q.getUserId().equals(1L)));
    }

    @Test
    public void testUpdateQuestion_Success() {
        QuestionResponse created = questionService.askQuestion(1L, askQuestionRequest);

        AskQuestionRequest updateRequest = AskQuestionRequest.builder()
            .title("Updated question title")
            .content("Updated content with sufficient length for validation requirements")
            .build();

        QuestionResponse updated = questionService.updateQuestion(created.getId(), 1L, updateRequest);

        assertEquals("Updated question title", updated.getTitle());
        assertEquals("Updated content with sufficient length for validation requirements", updated.getContent());
    }

    @Test
    public void testUpdateQuestion_Unauthorized() {
        QuestionResponse created = questionService.askQuestion(1L, askQuestionRequest);

        AskQuestionRequest updateRequest = AskQuestionRequest.builder()
            .title("Updated title")
            .content("Updated content with sufficient length for validation")
            .build();

        // User 2 tries to update User 1's question
        assertThrows(RuntimeException.class, () ->
            questionService.updateQuestion(created.getId(), 2L, updateRequest)
        );
    }

    @Test
    public void testDeleteQuestion_Success() {
        QuestionResponse created = questionService.askQuestion(1L, askQuestionRequest);

        questionService.deleteQuestion(created.getId(), 1L);

        // Verify it's deleted
        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionResponse> allQuestions = questionService.getAllQuestions(pageable);
        assertTrue(allQuestions.getContent().isEmpty());
    }

    @Test
    public void testSearchQuestions() {
        questionService.askQuestion(1L, askQuestionRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionResponse> results = questionService.searchQuestions("quadratic", pageable);

        assertEquals(1, results.getContent().size());
        assertEquals("How to solve quadratic equations?", results.getContent().get(0).getTitle());
    }

    @Test
    public void testGetMostViewedQuestions() {
        QuestionResponse q1 = questionService.askQuestion(1L, askQuestionRequest);

        // View q1 multiple times
        for (int i = 0; i < 5; i++) {
            questionService.getQuestionById(q1.getId());
        }

        // Create another question with fewer views
        AskQuestionRequest request2 = AskQuestionRequest.builder()
            .title("Less popular question")
            .content("This question will have fewer views and should rank lower")
            .build();
        QuestionResponse q2 = questionService.askQuestion(2L, request2);

        // View q2 once
        questionService.getQuestionById(q2.getId());

        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionResponse> mostViewed = questionService.getMostViewedQuestions(pageable);

        // q1 should come first (more views)
        assertTrue(mostViewed.getContent().size() >= 1);
        assertEquals(q1.getId(), mostViewed.getContent().get(0).getId());
    }
}


