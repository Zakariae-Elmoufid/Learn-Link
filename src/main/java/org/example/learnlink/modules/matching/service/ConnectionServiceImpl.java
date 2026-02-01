package org.example.learnlink.modules.matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.matching.dto.request.SendConnectionRequestDto;
import org.example.learnlink.modules.matching.dto.response.ConnectionRequestResponse;
import org.example.learnlink.modules.matching.dto.response.ConnectionResponse;
import org.example.learnlink.modules.matching.entity.Connection;
import org.example.learnlink.modules.matching.entity.ConnectionRequest;
import org.example.learnlink.modules.matching.entity.enums.ConnectionStatus;
import org.example.learnlink.modules.matching.entity.enums.RequestStatus;
import org.example.learnlink.modules.matching.event.ConnectionAcceptedEvent;
import org.example.learnlink.modules.matching.event.ConnectionRejectedEvent;
import org.example.learnlink.modules.matching.event.ConnectionRemovedEvent;
import org.example.learnlink.modules.matching.event.ConnectionRequestSentEvent;
import org.example.learnlink.modules.matching.mapper.ConnectionMapper;
import org.example.learnlink.modules.matching.repository.ConnectionRepository;
import org.example.learnlink.modules.matching.repository.ConnectionRequestRepository;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the connection service.
 * Manages connection requests and established connections between users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConnectionServiceImpl implements IConnectionService {

    private final ConnectionRequestRepository requestRepository;
    private final ConnectionRepository connectionRepository;
    private final UserProfileRepository userProfileRepository;
    private final ConnectionMapper connectionMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== Connection Requests ====================

    @Override
    public ConnectionRequestResponse sendConnectionRequest(Long senderId, SendConnectionRequestDto dto) {
        log.info("User {} sending connection request to user {}", senderId, dto.getReceiverId());

        // Validate sender is not requesting themselves
        if (senderId.equals(dto.getReceiverId())) {
            throw new IllegalStateException("Cannot send connection request to yourself");
        }

        // Check if receiver exists
        UserProfile receiverProfile = userProfileRepository.findByUserId(dto.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", dto.getReceiverId()));

        // Check if already connected
        if (connectionRepository.existsActiveConnectionBetween(senderId, dto.getReceiverId())) {
            throw new IllegalStateException("Users are already connected");
        }

        // Check if a pending request already exists
        requestRepository.findPendingBetweenUsers(senderId, dto.getReceiverId())
                .ifPresent(req -> {
                    throw new IllegalStateException("A pending connection request already exists between these users");
                });

        // Calculate compatibility score (simplified for now - can integrate matching algorithm later)
        BigDecimal compatibilityScore = calculateCompatibilityScore(senderId, dto.getReceiverId());

        // Create the connection request
        ConnectionRequest request = ConnectionRequest.builder()
                .senderId(senderId)
                .receiverId(dto.getReceiverId())
                .message(dto.getMessage())
                .status(RequestStatus.PENDING)
                .compatibilityScore(compatibilityScore)
                .build();

        ConnectionRequest savedRequest = requestRepository.save(request);
        log.info("Connection request {} created successfully", savedRequest.getId());

        // Publish event for notifications
        eventPublisher.publishEvent(new ConnectionRequestSentEvent(
                this,
                savedRequest.getId(),
                senderId,
                dto.getReceiverId(),
                compatibilityScore
        ));

        // Get sender profile for response
        UserProfile senderProfile = userProfileRepository.findByUserId(senderId).orElse(null);

        return connectionMapper.toRequestResponseWithProfiles(savedRequest, senderProfile, receiverProfile);
    }

    @Override
    public ConnectionResponse acceptRequest(Long userId, Long requestId) {
        log.info("User {} accepting connection request {}", userId, requestId);

        ConnectionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionRequest", "id", requestId));

        // Validate the user is the receiver
        if (!request.isReceiver(userId)) {
            throw new IllegalStateException("Only the receiver can accept a connection request");
        }

        // Validate request is pending
        if (!request.isPending()) {
            throw new IllegalStateException("Connection request is not pending. Current status: " + request.getStatus());
        }

        // Update request status
        request.setStatus(RequestStatus.ACCEPTED);
        requestRepository.save(request);

        // Create the connection
        Connection connection = Connection.builder()
                .user1Id(request.getSenderId())
                .user2Id(request.getReceiverId())
                .compatibilityScore(request.getCompatibilityScore())
                .status(ConnectionStatus.ACTIVE)
                .build();

        Connection savedConnection = connectionRepository.save(connection);
        log.info("Connection {} established between users {} and {}",
                savedConnection.getId(), request.getSenderId(), request.getReceiverId());

        // Publish event for notifications and gamification
        eventPublisher.publishEvent(new ConnectionAcceptedEvent(
                this,
                savedConnection.getId(),
                request.getId(),
                request.getSenderId(),
                request.getReceiverId(),
                request.getCompatibilityScore()
        ));

        // Get the sender's profile for the response (connected user from receiver's perspective)
        UserProfile connectedUserProfile = userProfileRepository.findByUserId(request.getSenderId()).orElse(null);

        return connectionMapper.toConnectionResponseWithProfile(
                savedConnection,
                request.getSenderId(),
                connectedUserProfile
        );
    }

    @Override
    public void rejectRequest(Long userId, Long requestId) {
        log.info("User {} rejecting connection request {}", userId, requestId);

        ConnectionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionRequest", "id", requestId));

        // Validate the user is the receiver
        if (!request.isReceiver(userId)) {
            throw new IllegalStateException("Only the receiver can reject a connection request");
        }

        // Validate request is pending
        if (!request.isPending()) {
            throw new IllegalStateException("Connection request is not pending. Current status: " + request.getStatus());
        }

        // Update request status
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        log.info("Connection request {} rejected", requestId);

        // Publish event (optional notification)
        eventPublisher.publishEvent(new ConnectionRejectedEvent(
                this,
                request.getId(),
                request.getSenderId(),
                request.getReceiverId()
        ));
    }

    @Override
    public void cancelRequest(Long userId, Long requestId) {
        log.info("User {} cancelling connection request {}", userId, requestId);

        ConnectionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionRequest", "id", requestId));

        // Validate the user is the sender
        if (!request.isSender(userId)) {
            throw new IllegalStateException("Only the sender can cancel a connection request");
        }

        // Validate request is pending
        if (!request.isPending()) {
            throw new IllegalStateException("Connection request is not pending. Current status: " + request.getStatus());
        }

        // Update request status
        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        log.info("Connection request {} cancelled", requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getPendingRequests(Long userId) {
        log.debug("Getting pending requests for user {}", userId);

        List<ConnectionRequest> requests = requestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);

        return requests.stream()
                .map(request -> {
                    UserProfile senderProfile = userProfileRepository.findByUserId(request.getSenderId()).orElse(null);
                    return connectionMapper.toRequestResponseWithSender(request, senderProfile);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getSentRequests(Long userId) {
        log.debug("Getting sent requests for user {}", userId);

        List<ConnectionRequest> requests = requestRepository.findBySenderId(userId);

        return requests.stream()
                .map(request -> {
                    UserProfile receiverProfile = userProfileRepository.findByUserId(request.getReceiverId()).orElse(null);
                    ConnectionRequestResponse response = connectionMapper.toRequestResponse(request);
                    if (receiverProfile != null) {
                        response.setReceiverFirstName(receiverProfile.getFirstName());
                        response.setReceiverLastName(receiverProfile.getLastName());
                        response.setReceiverProfilePictureUrl(receiverProfile.getProfilePictureUrl());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingRequestsCount(Long userId) {
        return requestRepository.countByReceiverIdAndStatus(userId, RequestStatus.PENDING);
    }

    // ==================== Connections ====================

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponse> getActiveConnections(Long userId) {
        log.debug("Getting active connections for user {}", userId);

        List<Connection> connections = connectionRepository.findActiveByUserId(userId);

        return connections.stream()
                .map(connection -> {
                    Long connectedUserId = connection.getOtherUserId(userId);
                    UserProfile connectedUserProfile = userProfileRepository.findByUserId(connectedUserId).orElse(null);
                    return connectionMapper.toConnectionResponseWithProfile(connection, connectedUserId, connectedUserProfile);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectionResponse getConnectionById(Long userId, Long connectionId) {
        log.debug("Getting connection {} for user {}", connectionId, userId);

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection", "id", connectionId));

        // Validate user is part of this connection
        if (!connection.involvesUser(userId)) {
            throw new IllegalStateException("User is not part of this connection");
        }

        Long connectedUserId = connection.getOtherUserId(userId);
        UserProfile connectedUserProfile = userProfileRepository.findByUserId(connectedUserId).orElse(null);

        return connectionMapper.toConnectionResponseWithProfile(connection, connectedUserId, connectedUserProfile);
    }

    @Override
    public void removeConnection(Long userId, Long connectionId) {
        log.info("User {} removing connection {}", userId, connectionId);

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection", "id", connectionId));

        // Validate user is part of this connection
        if (!connection.involvesUser(userId)) {
            throw new IllegalStateException("User is not part of this connection");
        }

        // Delete the connection
        connectionRepository.delete(connection);

        log.info("Connection {} removed", connectionId);

        // Publish event
        eventPublisher.publishEvent(new ConnectionRemovedEvent(
                this,
                connectionId,
                connection.getUser1Id(),
                connection.getUser2Id(),
                userId
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areConnected(Long user1Id, Long user2Id) {
        return connectionRepository.existsActiveConnectionBetween(user1Id, user2Id);
    }

    @Override
    @Transactional(readOnly = true)
    public long getConnectionsCount(Long userId) {
        return connectionRepository.countActiveByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getConnectedUserIds(Long userId) {
        return connectionRepository.findConnectedUserIds(userId);
    }

    // ==================== Private Methods ====================

    /**
     * Calculate compatibility score between two users.
     * This is a simplified version - can be enhanced with the matching algorithm.
     *
     * @param user1Id first user ID
     * @param user2Id second user ID
     * @return compatibility score (0-100)
     */
    private BigDecimal calculateCompatibilityScore(Long user1Id, Long user2Id) {
        // Get profiles
        UserProfile profile1 = userProfileRepository.findByUserId(user1Id).orElse(null);
        UserProfile profile2 = userProfileRepository.findByUserId(user2Id).orElse(null);

        if (profile1 == null || profile2 == null) {
            return new BigDecimal("50.00"); // Default score if profiles not found
        }

        // Calculate subject match score
        if (profile1.getSubjects() == null || profile2.getSubjects() == null ||
                profile1.getSubjects().isEmpty() || profile2.getSubjects().isEmpty()) {
            return new BigDecimal("50.00");
        }

        long commonSubjects = profile1.getSubjects().stream()
                .filter(s -> profile2.getSubjects().stream()
                        .anyMatch(s2 -> s2.getId().equals(s.getId())))
                .count();

        int totalSubjects = profile1.getSubjects().size();
        double subjectScore = (double) commonSubjects / totalSubjects * 100;

        // Calculate level match score
        double levelScore = 50.0;
        if (profile1.getAcademicLevel() != null && profile2.getAcademicLevel() != null) {
            int levelDiff = Math.abs(profile1.getAcademicLevel().ordinal() - profile2.getAcademicLevel().ordinal());
            levelScore = Math.max(0, 100 - (levelDiff * 25));
        }

            // Weighted average: 60% subjects, 40% level
            double totalScore = (subjectScore * 0.6) + (levelScore * 0.4);

        return BigDecimal.valueOf(totalScore).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
