package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AnswerResponse;
import org.example.learnlink.modules.community.dto.ProvideAnswerRequest;
import org.example.learnlink.modules.community.entity.Answer;
import org.example.learnlink.modules.community.entity.AnswerVote;
import org.example.learnlink.modules.community.entity.Question;
import org.example.learnlink.modules.community.entity.VoteType;
import org.example.learnlink.modules.community.event.AnswerAcceptedEvent;
import org.example.learnlink.modules.community.event.AnswerProvidedEvent;
import org.example.learnlink.modules.community.event.AnswerUpvotedEvent;
import org.example.learnlink.modules.community.mapper.AnswerMapper;
import org.example.learnlink.modules.community.repository.AnswerRepository;
import org.example.learnlink.modules.community.repository.AnswerVoteRepository;
import org.example.learnlink.modules.community.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AnswerService
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements IAnswerService {

    private final AnswerRepository answerRepository;
    private final AnswerVoteRepository answerVoteRepository;
    private final QuestionRepository questionRepository;
    private final AnswerMapper answerMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AnswerResponse provideAnswer(Long questionId, Long userId, ProvideAnswerRequest request) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        Answer answer = Answer.builder()
            .questionId(questionId)
            .userId(userId)
            .content(request.getContent())
            .build();

        answer = answerRepository.save(answer);

        // Publish event for gamification
        eventPublisher.publishEvent(new AnswerProvidedEvent(this, userId, answer.getId(), questionId));

        return answerMapper.answerToResponse(answer);
    }

    @Override
    @Transactional(readOnly = true)
    public AnswerResponse getAnswerById(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("Answer not found with id: " + answerId));

        return answerMapper.answerToResponse(answer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionId(questionId)
            .stream()
            .map(answerMapper::answerToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnswerResponse> getUserAnswers(Long userId, Pageable pageable) {
        return answerRepository.findByUserIdAndHiddenIsFalse(userId, pageable)
            .map(answerMapper::answerToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnswerResponse> getTopAnswers(Pageable pageable) {
        return answerRepository.findTopAnswers(pageable)
            .map(answerMapper::answerToResponse);
    }

    @Override
    @Transactional
    public AnswerResponse updateAnswer(Long answerId, Long userId, ProvideAnswerRequest request) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("Answer not found with id: " + answerId));

        if (!answer.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only answer author can update");
        }

        answer.setContent(request.getContent());

        answer = answerRepository.save(answer);
        return answerMapper.answerToResponse(answer);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("Answer not found with id: " + answerId));

        if (!answer.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only answer author can delete");
        }

        answerRepository.delete(answer);
    }

    @Override
    @Transactional
    public void acceptAnswer(Long questionId, Long answerId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        if (!question.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only question author can accept answers");
        }

        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("Answer not found with id: " + answerId));

        if (!answer.getQuestionId().equals(questionId)) {
            throw new RuntimeException("Answer does not belong to this question");
        }

        answer.markAsAccepted();
        answerRepository.save(answer);

        question.acceptAnswer(answerId);
        questionRepository.save(question);

        // Publish event for gamification
        eventPublisher.publishEvent(
            new AnswerAcceptedEvent(this, answerId, answer.getUserId(), userId)
        );
    }

    @Override
    @Transactional
    public void voteAnswer(Long answerId, Long userId, VoteType voteType) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("Answer not found with id: " + answerId));

        if (answer.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot vote on your own answer");
        }

        // Check if user already voted
        var existingVote = answerVoteRepository.findByAnswerIdAndUserId(answerId, userId);

        if (existingVote.isPresent()) {
            AnswerVote vote = existingVote.get();
            // If voting same type, remove vote
            if (vote.getVoteType() == voteType) {
                removeVoteInternal(answer, vote);
            } else {
                // Change vote type
                removeVoteInternal(answer, vote);
                addVoteInternal(answer, userId, answerId, voteType);
            }
        } else {
            // Add new vote
            addVoteInternal(answer, userId, answerId, voteType);
        }

        answerRepository.save(answer);
    }

    @Override
    @Transactional
    public void removeVote(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("Answer not found with id: " + answerId));

        var existingVote = answerVoteRepository.findByAnswerIdAndUserId(answerId, userId);

        if (existingVote.isPresent()) {
            removeVoteInternal(answer, existingVote.get());
            answerRepository.save(answer);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean answerExistsByQuestionAndUser(Long questionId, Long userId) {
        return answerRepository.existsByQuestionIdAndUserId(questionId, userId);
    }

    /**
     * Add vote to answer
     */
    private void addVoteInternal(Answer answer, Long userId, Long answerId, VoteType voteType) {
        AnswerVote vote = AnswerVote.builder()
            .answerId(answerId)
            .userId(userId)
            .voteType(voteType)
            .build();

        answerVoteRepository.save(vote);

        if (voteType == VoteType.UPVOTE) {
            answer.incrementUpvoteCount();
            // Publish upvote event for gamification
            eventPublisher.publishEvent(
                new AnswerUpvotedEvent(this, answerId, answer.getUserId(), userId)
            );
        } else {
            answer.incrementDownvoteCount();
        }
    }

    /**
     * Remove vote from answer
     */
    private void removeVoteInternal(Answer answer, AnswerVote vote) {
        if (vote.getVoteType() == VoteType.UPVOTE) {
            answer.decrementUpvoteCount();
        } else {
            answer.decrementDownvoteCount();
        }

        answerVoteRepository.delete(vote);
    }
}

