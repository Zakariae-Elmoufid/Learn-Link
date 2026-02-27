package org.example.learnlink.modules.matching.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.matching.dto.request.SendConnectionRequestDto;
import org.example.learnlink.modules.matching.dto.response.ConnectionRequestResponse;
import org.example.learnlink.modules.matching.dto.response.ConnectionResponse;
import org.example.learnlink.modules.matching.service.IConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing connections between users.
 * Handles connection requests (send, accept, reject) and established connections.
 */
@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final IConnectionService connectionService;

    // ==================== Connection Requests ====================

    /**
     * Send a connection request to another user.
     * POST /api/connections/requests
     *
     * @param userDetails the authenticated user details
     * @param dto    the request body containing receiver ID and optional message
     * @return the created connection request
     */
    @PostMapping("/requests")
    public ResponseEntity<ConnectionRequestResponse> sendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SendConnectionRequestDto dto) {
        Long userId = userDetails.getId();
        ConnectionRequestResponse response = connectionService.sendConnectionRequest(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all pending connection requests received by the current user.
     * GET /api/connections/requests/pending
     *
     * @param userDetails the authenticated user details
     * @return list of pending connection requests
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ConnectionRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<ConnectionRequestResponse> requests = connectionService.getPendingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get the count of pending requests (for notification badge).
     * GET /api/connections/requests/pending/count
     *
     * @param userDetails the authenticated user details
     * @return count of pending requests
     */
    @GetMapping("/requests/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingRequestsCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        long count = connectionService.getPendingRequestsCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Get all connection requests sent by the current user.
     * GET /api/connections/requests/sent
     *
     * @param userDetails the authenticated user details
     * @return list of sent connection requests
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<List<ConnectionRequestResponse>> getSentRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<ConnectionRequestResponse> requests = connectionService.getSentRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Accept a pending connection request.
     * POST /api/connections/requests/{requestId}/accept
     *
     * @param userDetails the authenticated user details (must be the receiver)
     * @param requestId the ID of the request to accept
     * @return the newly created connection
     */
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<ConnectionResponse> acceptRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = userDetails.getId();
        ConnectionResponse response = connectionService.acceptRequest(userId, requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a pending connection request.
     * POST /api/connections/requests/{requestId}/reject
     *
     * @param userDetails the authenticated user details (must be the receiver)
     * @param requestId the ID of the request to reject
     * @return 204 No Content on success
     */
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = userDetails.getId();
        connectionService.rejectRequest(userId, requestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancel a connection request that was sent by the current user.
     * DELETE /api/connections/requests/{requestId}
     *
     * @param userDetails the authenticated user details (must be the sender)
     * @param requestId the ID of the request to cancel
     * @return 204 No Content on success
     */
    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = userDetails.getId();
        connectionService.cancelRequest(userId, requestId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Connections ====================

    /**
     * Get all active connections for the current user.
     * GET /api/connections
     *
     * @param userDetails the authenticated user details
     * @return list of active connections with user details
     */
    @GetMapping
    public ResponseEntity<List<ConnectionResponse>> getMyConnections(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<ConnectionResponse> connections = connectionService.getActiveConnections(userId);
        return ResponseEntity.ok(connections);
    }

    /**
     * Get the count of active connections.
     * GET /api/connections/count
     *
     * @param userDetails the authenticated user details
     * @return count of active connections
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getConnectionsCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        long count = connectionService.getConnectionsCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Get a specific connection by ID.
     * GET /api/connections/{connectionId}
     *
     * @param userDetails the authenticated user details
     * @param connectionId the ID of the connection
     * @return the connection details
     */
    @GetMapping("/{connectionId}")
    public ResponseEntity<ConnectionResponse> getConnectionById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long connectionId) {
        Long userId = userDetails.getId();
        ConnectionResponse response = connectionService.getConnectionById(userId, connectionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove/delete a connection.
     * DELETE /api/connections/{connectionId}
     *
     * @param userDetails the authenticated user details
     * @param connectionId the ID of the connection to remove
     * @return 204 No Content on success
     */
    @DeleteMapping("/{connectionId}")
    public ResponseEntity<Void> removeConnection(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long connectionId) {
        Long userId = userDetails.getId();
        connectionService.removeConnection(userId, connectionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if the current user is connected with another user.
     * GET /api/connections/check/{otherUserId}
     *
     * @param userDetails the authenticated user details
     * @param otherUserId the ID of the other user to check
     * @return true if connected, false otherwise
     */
    @GetMapping("/check/{otherUserId}")
    public ResponseEntity<Map<String, Boolean>> checkConnection(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long otherUserId) {
        Long userId = userDetails.getId();
        boolean connected = connectionService.areConnected(userId, otherUserId);
        return ResponseEntity.ok(Map.of("connected", connected));
    }
}
