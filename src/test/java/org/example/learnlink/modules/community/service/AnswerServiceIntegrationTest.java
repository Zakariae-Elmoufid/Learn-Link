package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AskQuestionRequest;
import org.example.learnlink.modules.community.dto.AnswerResponse;
import org.example.learnlink.modules.community.dto.ProvideAnswerRequest;
import org.example.learnlink.modules.community.dto.QuestionResponse;
import org.example.learnlink.modules.community.entity.VoteType;
import org.example.learnlink.modules.community.repository.AnswerRepository;
import org.example.learnlink.modules.community.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AnswerService
 */
@SpringBootTest
public class AnswerServiceIntegrationTest {

    @Autowired
    private IAnswerService answerService;

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private QuestionResponse testQuestion;
    private ProvideAnswerRequest provideAnswerRequest;

    @BeforeEach
    public void setUp() {
        // Clear databases
        answerRepository.deleteAll();
        questionRepository.deleteAll();

        // Create a test question
        AskQuestionRequest questionRequest = AskQuestionRequest.builder()
            .title("How to solve quadratic equations?")
            .content("I am struggling with solving quadratic equations. Can someone help?")
            .build();
        testQuestion = questionService.askQuestion(1L, questionRequest);

        provideAnswerRequest = ProvideAnswerRequest.builder()
            .content("You can use the quadratic formula: x = (-b ± √(b²-4ac)) / 2a. Let me explain each part...")
            .build();
    }

    @Test
    public void testProvideAnswer_Success() {
        AnswerResponse response = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        assertNotNull(response.getId());
        assertEquals(testQuestion.getId(), response.getQuestionId());
        assertEquals(2L, response.getUserId());
        assertEquals(0L, response.getVoteCount());
        assertEquals(false, response.getIsAccepted());
    }

    @Test
    public void testGetAnswerById_Success() {
        AnswerResponse created = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        AnswerResponse retrieved = answerService.getAnswerById(created.getId());

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(testQuestion.getId(), retrieved.getQuestionId());
    }

    @Test
    public void testGetAnswersByQuestion() {
        // Provide multiple answers
        answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        ProvideAnswerRequest answer2 = ProvideAnswerRequest.builder()
            .content("Another method is to factor the equation if possible")
            .build();
        answerService.provideAnswer(testQuestion.getId(), 3L, answer2);

        List<AnswerResponse> answers = answerService.getAnswersByQuestion(testQuestion.getId());

        assertEquals(2, answers.size());
        assertTrue(answers.stream().allMatch(a -> a.getQuestionId().equals(testQuestion.getId())));
    }

    @Test
    public void testAcceptAnswer_Success() {
        AnswerResponse answer = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        // Accept the answer (only question author can do this)
        answerService.acceptAnswer(testQuestion.getId(), answer.getId(), 1L);

        AnswerResponse acceptedAnswer = answerService.getAnswerById(answer.getId());

        assertTrue(acceptedAnswer.getIsAccepted());

        // Question should be marked as resolved
        QuestionResponse updatedQuestion = questionService.getQuestionById(testQuestion.getId());
        assertTrue(updatedQuestion.getIsResolved());
        assertEquals(answer.getId(), updatedQuestion.getAcceptedAnswerId());
    }

    @Test
    public void testAcceptAnswer_OnlyAskerCanAccept() {
        AnswerResponse answer = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        // User 3 tries to accept answer (not the question asker)
        assertThrows(RuntimeException.class, () ->
            answerService.acceptAnswer(testQuestion.getId(), answer.getId(), 3L)
        );
    }

    @Test
    public void testVoteAnswer_Upvote() {
        AnswerResponse answer = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        answerService.voteAnswer(answer.getId(), 3L, VoteType.UPVOTE);

        AnswerResponse votedAnswer = answerService.getAnswerById(answer.getId());
        assertEquals(1L, votedAnswer.getVoteCount());
        assertEquals(1L, votedAnswer.getUpvoteCount());
        assertEquals(0L, votedAnswer.getDownvoteCount());
    }

    @Test
    public void testVoteAnswer_Downvote() {
        AnswerResponse answer = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        answerService.voteAnswer(answer.getId(), 3L, VoteType.DOWNVOTE);

        AnswerResponse votedAnswer = answerService.getAnswerById(answer.getId());
        assertEquals(-1L, votedAnswer.getVoteCount());
        assertEquals(0L, votedAnswer.getUpvoteCount());
        assertEquals(1L, votedAnswer.getDownvoteCount());
    }

    @Test
    public void testVoteAnswer_CannotVoteOwnAnswer() {
        AnswerResponse answer = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        // User 2 (answer author) tries to vote on own answer
        assertThrows(RuntimeException.class, () ->
            answerService.voteAnswer(answer.getId(), 2L, VoteType.UPVOTE)
        );
    }

    @Test
    public void testRemoveVote() {
        AnswerResponse answer = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        // Vote first
        answerService.voteAnswer(answer.getId(), 3L, VoteType.UPVOTE);
        AnswerResponse afterVote = answerService.getAnswerById(answer.getId());
        assertEquals(1L, afterVote.getVoteCount());

        // Remove vote
        answerService.removeVote(answer.getId(), 3L);
        AnswerResponse afterRemoval = answerService.getAnswerById(answer.getId());
        assertEquals(0L, afterRemoval.getVoteCount());
    }

    @Test
    public void testUpdateAnswer_Success() {
        AnswerResponse created = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        ProvideAnswerRequest updateRequest = ProvideAnswerRequest.builder()
            .content("Updated answer with better explanation of the quadratic formula")
            .build();

        AnswerResponse updated = answerService.updateAnswer(created.getId(), 2L, updateRequest);

        assertEquals("Updated answer with better explanation of the quadratic formula", updated.getContent());
    }

    @Test
    public void testUpdateAnswer_Unauthorized() {
        AnswerResponse created = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        ProvideAnswerRequest updateRequest = ProvideAnswerRequest.builder()
            .content("Unauthorized update attempt")
            .build();

        // User 3 tries to update User 2's answer
        assertThrows(RuntimeException.class, () ->
            answerService.updateAnswer(created.getId(), 3L, updateRequest)
        );
    }

    @Test
    public void testDeleteAnswer_Success() {
        AnswerResponse created = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        answerService.deleteAnswer(created.getId(), 2L);

        // Verify deletion
        List<AnswerResponse> answers = answerService.getAnswersByQuestion(testQuestion.getId());
        assertTrue(answers.isEmpty());
    }

    @Test
    public void testGetUserAnswers() {
        // User 2 provides 2 answers
        answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        AskQuestionRequest q2Request = AskQuestionRequest.builder()
            .title("Another question?")
            .content("Another question with sufficient length for validation")
            .build();
        QuestionResponse q2 = questionService.askQuestion(1L, q2Request);

        ProvideAnswerRequest answer2 = ProvideAnswerRequest.builder()
            .content("Answer to second question with sufficient content for validation")
            .build();
        answerService.provideAnswer(q2.getId(), 2L, answer2);

        // User 3 provides 1 answer
        answerService.provideAnswer(testQuestion.getId(), 3L, provideAnswerRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AnswerResponse> user2Answers = answerService.getUserAnswers(2L, pageable);

        assertEquals(2, user2Answers.getContent().size());
        assertTrue(user2Answers.getContent().stream()
            .allMatch(a -> a.getUserId().equals(2L)));
    }

    @Test
    public void testGetTopAnswers() {
        // Create answer with upvotes
        AnswerResponse answer1 = answerService.provideAnswer(testQuestion.getId(), 2L, provideAnswerRequest);

        // Vote it up 5 times
        for (int i = 0; i < 5; i++) {
            answerService.voteAnswer(answer1.getId(), (100 + i), VoteType.UPVOTE);
        }

        // Create answer with fewer upvotes
        ProvideAnswerRequest answer2Request = ProvideAnswerRequest.builder()
            .content("Another answer with fewer votes")
            .build();
        AnswerResponse answer2 = answerService.provideAnswer(testQuestion.getId(), 3L, answer2Request);

        // Vote it up 2 times
        answerService.voteAnswer(answer2.getId(), 101, VoteType.UPVOTE);
        answerService.voteAnswer(answer2.getId(), 102, VoteType.UPVOTE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AnswerResponse> topAnswers = answerService.getTopAnswers(pageable);

        assertTrue(topAnswers.getContent().size() >= 1);
        // answer1 should be first (5 upvotes)
        assertEquals(answer1.getId(), topAnswers.getContent().get(0).getId());
    }
}

