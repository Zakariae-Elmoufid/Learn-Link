package org.example.learnlink.modules.matching.service;

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
import org.example.learnlink.modules.matching.mapper.ConnectionMapper;
import org.example.learnlink.modules.matching.repository.ConnectionRepository;
import org.example.learnlink.modules.matching.repository.ConnectionRequestRepository;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.example.learnlink.modules.user.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConnectionService Unit Tests - AAA Pattern")
class ConnectionServiceImplTest {

    @Mock
    private ConnectionRequestRepository requestRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ConnectionMapper connectionMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProfileService profileService;

    private ConnectionServiceImpl connectionService;

    @BeforeEach
    void setUp() {
        connectionService = new ConnectionServiceImpl(
                requestRepository,
                connectionRepository,
                userProfileRepository,
                connectionMapper,
                eventPublisher,
                profileService
        );
    }

    // ============= SEND CONNECTION REQUEST TESTS =============

    @Test
    @DisplayName("sendConnectionRequest() - Should successfully send connection request")
    void testSendConnectionRequestSuccess() {
        // Arrange
        Long senderId = 1L;
        Long receiverId = 2L;
        SendConnectionRequestDto dto = new SendConnectionRequestDto(receiverId, "Let's study together!");

        UserProfileResponse senderProfile = new UserProfileResponse(
                "John", "Doe", "Bio", "https://pic.jpg", Collections.emptyList(), null
        );
        UserProfileResponse receiverProfile = new UserProfileResponse(
                "Jane", "Smith", "Bio", "https://pic.jpg", Collections.emptyList(), null
        );

        ConnectionRequest savedRequest = ConnectionRequest.builder()
                .id(1L)
                .senderId(senderId)
                .receiverId(receiverId)
                .message(dto.getMessage())
                .status(RequestStatus.PENDING)
                .compatibilityScore(new BigDecimal("85.00"))
                .build();

        ConnectionRequestResponse expectedResponse = new ConnectionRequestResponse();

        when(connectionRepository.existsActiveConnectionBetween(senderId, receiverId)).thenReturn(false);
        when(profileService.getProfileByUserId(receiverId)).thenReturn(receiverProfile);
        when(requestRepository.findPendingBetweenUsers(senderId, receiverId)).thenReturn(Optional.empty());
        when(requestRepository.save(any(ConnectionRequest.class))).thenReturn(savedRequest);
        when(profileService.getProfileByUserId(senderId)).thenReturn(senderProfile);
        when(connectionMapper.toRequestResponseWithProfiles(any(), any(), any())).thenReturn(expectedResponse);

        // Act
        ConnectionRequestResponse response = connectionService.sendConnectionRequest(senderId, dto);

        // Assert
        assertNotNull(response);
        verify(connectionRepository).existsActiveConnectionBetween(senderId, receiverId);
        verify(profileService).getProfileByUserId(receiverId);
        verify(requestRepository).save(any(ConnectionRequest.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("sendConnectionRequest() - Should throw exception when sender equals receiver")
    void testSendConnectionRequestToSelf() {
        // Arrange
        Long userId = 1L;
        SendConnectionRequestDto dto = new SendConnectionRequestDto(userId, "Let's study");

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.sendConnectionRequest(userId, dto)
        );
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendConnectionRequest() - Should throw exception when users already connected")
    void testSendConnectionRequestAlreadyConnected() {
        // Arrange
        Long senderId = 1L;
        Long receiverId = 2L;
        SendConnectionRequestDto dto = new SendConnectionRequestDto(receiverId, "Let's study");

        when(connectionRepository.existsActiveConnectionBetween(senderId, receiverId)).thenReturn(true);

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.sendConnectionRequest(senderId, dto)
        );
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendConnectionRequest() - Should throw exception when pending request exists")
    void testSendConnectionRequestPendingExists() {
        // Arrange
        Long senderId = 1L;
        Long receiverId = 2L;
        SendConnectionRequestDto dto = new SendConnectionRequestDto(receiverId, "Let's study");

        UserProfileResponse receiverProfile = new UserProfileResponse(
                "Jane", "Smith", "Bio", "https://pic.jpg", Collections.emptyList(), null
        );

        ConnectionRequest existingRequest = ConnectionRequest.builder()
                .id(1L)
                .senderId(senderId)
                .receiverId(receiverId)
                .status(RequestStatus.PENDING)
                .build();

        when(connectionRepository.existsActiveConnectionBetween(senderId, receiverId)).thenReturn(false);
        when(profileService.getProfileByUserId(receiverId)).thenReturn(receiverProfile);
        when(requestRepository.findPendingBetweenUsers(senderId, receiverId)).thenReturn(Optional.of(existingRequest));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.sendConnectionRequest(senderId, dto)
        );
        verify(requestRepository, never()).save(any(ConnectionRequest.class));
    }

    // ============= ACCEPT REQUEST TESTS =============

    @Test
    @DisplayName("acceptRequest() - Should successfully accept connection request")
    void testAcceptRequestSuccess() {
        // Arrange
        Long userId = 2L;
        Long requestId = 1L;
        Long senderId = 1L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(requestId)
                .senderId(senderId)
                .receiverId(userId)
                .status(RequestStatus.PENDING)
                .compatibilityScore(new BigDecimal("85.00"))
                .build();

        Connection savedConnection = Connection.builder()
                .id(1L)
                .user1Id(senderId)
                .user2Id(userId)
                .status(ConnectionStatus.ACTIVE)
                .build();

        UserProfileResponse senderProfile = new UserProfileResponse(
                "John", "Doe", "Bio", "https://pic.jpg", Collections.emptyList(), null
        );

        ConnectionResponse expectedResponse = new ConnectionResponse();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(ConnectionRequest.class))).thenReturn(request);
        when(connectionRepository.save(any(Connection.class))).thenReturn(savedConnection);
        when(profileService.getProfileByUserId(senderId)).thenReturn(senderProfile);
        when(connectionMapper.toConnectionResponseWithProfile(any(), any(), any())).thenReturn(expectedResponse);

        // Act
        ConnectionResponse response = connectionService.acceptRequest(userId, requestId);

        // Assert
        assertNotNull(response);
        verify(requestRepository).findById(requestId);
        verify(connectionRepository).save(any(Connection.class));
        verify(eventPublisher).publishEvent(any(ConnectionAcceptedEvent.class));
    }

    @Test
    @DisplayName("acceptRequest() - Should throw exception when request not found")
    void testAcceptRequestNotFound() {
        // Arrange
        Long userId = 2L;
        Long requestId = 999L;
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> connectionService.acceptRequest(userId, requestId)
        );
        verify(connectionRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptRequest() - Should throw exception when user is not receiver")
    void testAcceptRequestNotReceiver() {
        // Arrange
        Long userId = 3L;
        Long requestId = 1L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(requestId)
                .senderId(1L)
                .receiverId(2L)
                .status(RequestStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.acceptRequest(userId, requestId)
        );
        verify(connectionRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptRequest() - Should throw exception when request is not pending")
    void testAcceptRequestNotPending() {
        // Arrange
        Long userId = 2L;
        Long requestId = 1L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(requestId)
                .senderId(1L)
                .receiverId(userId)
                .status(RequestStatus.REJECTED)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.acceptRequest(userId, requestId)
        );
        verify(connectionRepository, never()).save(any());
    }

    // ============= REJECT REQUEST TESTS =============

    @Test
    @DisplayName("rejectRequest() - Should successfully reject connection request")
    void testRejectRequestSuccess() {
        // Arrange
        Long userId = 2L;
        Long requestId = 1L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(requestId)
                .senderId(1L)
                .receiverId(userId)
                .status(RequestStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(ConnectionRequest.class))).thenReturn(request);

        // Act
        connectionService.rejectRequest(userId, requestId);

        // Assert
        verify(requestRepository).save(any(ConnectionRequest.class));
        verify(eventPublisher).publishEvent(any(ConnectionRejectedEvent.class));
    }

    @Test
    @DisplayName("rejectRequest() - Should throw exception when request not found")
    void testRejectRequestNotFound() {
        // Arrange
        Long userId = 2L;
        Long requestId = 999L;
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> connectionService.rejectRequest(userId, requestId)
        );
    }

    @Test
    @DisplayName("rejectRequest() - Should throw exception when user is not receiver")
    void testRejectRequestNotReceiver() {
        // Arrange
        Long userId = 3L;
        Long requestId = 1L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(requestId)
                .senderId(1L)
                .receiverId(2L)
                .status(RequestStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.rejectRequest(userId, requestId)
        );
        verify(requestRepository, never()).save(any());
    }

    // ============= CANCEL REQUEST TESTS =============

    @Test
    @DisplayName("cancelRequest() - Should successfully cancel connection request")
    void testCancelRequestSuccess() {
        // Arrange
        Long userId = 1L;
        Long requestId = 1L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(requestId)
                .senderId(userId)
                .receiverId(2L)
                .status(RequestStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act
        connectionService.cancelRequest(userId, requestId);

        // Assert
        verify(requestRepository).delete(request);
    }

    @Test
    @DisplayName("cancelRequest() - Should throw exception when user is not sender")
    void testCancelRequestNotSender() {
        // Arrange
        Long userId = 2L;
        Long requestId = 1L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(requestId)
                .senderId(1L)
                .receiverId(userId)
                .status(RequestStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.cancelRequest(userId, requestId)
        );
        verify(requestRepository, never()).delete(any());
    }

    // ============= GET PENDING REQUESTS TESTS =============

    @Test
    @DisplayName("getPendingRequests() - Should return pending requests for user")
    void testGetPendingRequestsSuccess() {
        // Arrange
        Long userId = 2L;

        ConnectionRequest request = ConnectionRequest.builder()
                .id(1L)
                .senderId(1L)
                .receiverId(userId)
                .status(RequestStatus.PENDING)
                .build();

        UserProfileResponse senderProfile = new UserProfileResponse(
                "John", "Doe", "Bio", "https://pic.jpg", Collections.emptyList(), null
        );

        ConnectionRequestResponse expectedResponse = new ConnectionRequestResponse();

        when(requestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING))
                .thenReturn(List.of(request));
        when(profileService.getProfileByUserId(1L)).thenReturn(senderProfile);
        when(connectionMapper.toRequestResponseWithSender(request, senderProfile)).thenReturn(expectedResponse);

        // Act
        List<ConnectionRequestResponse> requests = connectionService.getPendingRequests(userId);

        // Assert
        assertNotNull(requests);
        verify(requestRepository).findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
    }

    // ============= GET ACTIVE CONNECTIONS TESTS =============

    @Test
    @DisplayName("getActiveConnections() - Should return active connections for user")
    void testGetActiveConnectionsSuccess() {
        // Arrange
        Long userId = 1L;

        Connection connection = Connection.builder()
                .id(1L)
                .user1Id(userId)
                .user2Id(2L)
                .status(ConnectionStatus.ACTIVE)
                .build();

        UserProfileResponse connectedUserProfile = new UserProfileResponse(
                "Jane", "Smith", "Bio", "https://pic.jpg", Collections.emptyList(), null
        );

        ConnectionResponse expectedResponse = new ConnectionResponse();

        when(connectionRepository.findActiveByUserId(userId)).thenReturn(List.of(connection));
        when(profileService.getProfileByUserId(2L)).thenReturn(connectedUserProfile);
        when(connectionMapper.toConnectionResponseWithProfile(any(), any(), any())).thenReturn(expectedResponse);

        // Act
        List<ConnectionResponse> connections = connectionService.getActiveConnections(userId);

        // Assert
        assertNotNull(connections);
        verify(connectionRepository).findActiveByUserId(userId);
    }

    @Test
    @DisplayName("getActiveConnections() - Should return empty list when no connections")
    void testGetActiveConnectionsEmpty() {
        // Arrange
        Long userId = 1L;
        when(connectionRepository.findActiveByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<ConnectionResponse> connections = connectionService.getActiveConnections(userId);

        // Assert
        assertNotNull(connections);
        assertTrue(connections.isEmpty());
    }

    // ============= GET CONNECTION BY ID TESTS =============

    @Test
    @DisplayName("getConnectionById() - Should return connection when user is part of it")
    void testGetConnectionByIdSuccess() {
        // Arrange
        Long userId = 1L;
        Long connectionId = 1L;

        Connection connection = Connection.builder()
                .id(connectionId)
                .user1Id(userId)
                .user2Id(2L)
                .status(ConnectionStatus.ACTIVE)
                .build();

        UserProfileResponse connectedUserProfile = new UserProfileResponse(
                "Jane", "Smith", "Bio", "https://pic.jpg", Collections.emptyList(), null
        );

        ConnectionResponse expectedResponse = new ConnectionResponse();

        when(connectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));
        when(profileService.getProfileByUserId(2L)).thenReturn(connectedUserProfile);
        when(connectionMapper.toConnectionResponseWithProfile(any(), any(), any())).thenReturn(expectedResponse);

        // Act
        ConnectionResponse response = connectionService.getConnectionById(userId, connectionId);

        // Assert
        assertNotNull(response);
        verify(connectionRepository).findById(connectionId);
    }

    @Test
    @DisplayName("getConnectionById() - Should throw exception when user not part of connection")
    void testGetConnectionByIdNotInvolved() {
        // Arrange
        Long userId = 3L;
        Long connectionId = 1L;

        Connection connection = Connection.builder()
                .id(connectionId)
                .user1Id(1L)
                .user2Id(2L)
                .status(ConnectionStatus.ACTIVE)
                .build();

        when(connectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.getConnectionById(userId, connectionId)
        );
    }

    // ============= REMOVE CONNECTION TESTS =============

    @Test
    @DisplayName("removeConnection() - Should successfully remove connection")
    void testRemoveConnectionSuccess() {
        // Arrange
        Long userId = 1L;
        Long connectionId = 1L;

        Connection connection = Connection.builder()
                .id(connectionId)
                .user1Id(userId)
                .user2Id(2L)
                .status(ConnectionStatus.ACTIVE)
                .build();

        when(connectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));

        // Act
        connectionService.removeConnection(userId, connectionId);

        // Assert
        verify(connectionRepository).findById(connectionId);
        verify(connectionRepository).delete(connection);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("removeConnection() - Should throw exception when user not part of connection")
    void testRemoveConnectionNotInvolved() {
        // Arrange
        Long userId = 3L;
        Long connectionId = 1L;

        Connection connection = Connection.builder()
                .id(connectionId)
                .user1Id(1L)
                .user2Id(2L)
                .status(ConnectionStatus.ACTIVE)
                .build();

        when(connectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> connectionService.removeConnection(userId, connectionId)
        );
        verify(connectionRepository, never()).delete(any());
    }
}
