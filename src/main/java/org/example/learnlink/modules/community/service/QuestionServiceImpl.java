package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AskQuestionRequest;
import org.example.learnlink.modules.community.dto.QuestionResponse;
import org.example.learnlink.modules.community.entity.Question;
import org.example.learnlink.modules.community.event.QuestionAskedEvent;
import org.example.learnlink.modules.community.mapper.QuestionMapper;
import org.example.learnlink.modules.community.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of QuestionService
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements IQuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final IAnswerService answerService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public QuestionResponse askQuestion(Long userId, AskQuestionRequest request) {
        Question question = Question.builder()
            .userId(userId)
            .title(request.getTitle())
            .content(request.getContent())
            .build();

        question = questionRepository.save(question);

        // Publish event for gamification
        eventPublisher.publishEvent(new QuestionAskedEvent(this, userId, question.getId()));

        return convertToResponse(question);
    }

    @Override
    @Transactional
    public QuestionResponse getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        // Increment view count
        question.incrementViewCount();
        questionRepository.save(question);

        return convertToResponse(question);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> getUserQuestions(Long userId, Pageable pageable) {
        return questionRepository.findByUserId(userId, pageable)
            .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> getUnresolvedQuestions(Pageable pageable) {
        return questionRepository.findByIsResolvedFalse(pageable)
            .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> getResolvedQuestions(Pageable pageable) {
        return questionRepository.findByIsResolvedTrue(pageable)
            .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> searchQuestions(String keyword, Pageable pageable) {
        return questionRepository.searchByKeyword(keyword, pageable)
            .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> getAllQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable)
            .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, Long userId, AskQuestionRequest request) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        if (!question.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only question author can update");
        }

        question.setTitle(request.getTitle());
        question.setContent(request.getContent());

        question = questionRepository.save(question);
        return convertToResponse(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        if (!question.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only question author can delete");
        }

        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> getMostViewedQuestions(Pageable pageable) {
        return questionRepository.findMostViewedQuestions(pageable)
            .map(this::convertToResponse);
    }

    /**
     * Convert Question entity to QuestionResponse DTO
     */
    private QuestionResponse convertToResponse(Question question) {
        return QuestionResponse.builder()
            .id(question.getId())
            .userId(question.getUserId())
            .title(question.getTitle())
            .content(question.getContent())
            .viewCount(question.getViewCount())
            .isResolved(question.getIsResolved())
            .acceptedAnswerId(question.getAcceptedAnswerId())
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
            .answers(null) // Load separately if needed
            .build();
    }
}

