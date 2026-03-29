package org.example.learnlink.modules.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis-based implementation of PresenceService.
 * 
 * Uses Redis SET for fast O(1) lookup of online status.
 * Each online user is stored with a TTL to auto-expire if heartbeat is missed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceServiceImpl implements PresenceService {

    private static final String ONLINE_USERS_KEY = "presence:online";
    private static final String USER_PRESENCE_PREFIX = "presence:user:";
    private static final Duration PRESENCE_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setOnline(Long userId) {
        try {
            // Add to online users set
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
            
            // Set individual key with TTL (acts as heartbeat)
            String userKey = USER_PRESENCE_PREFIX + userId;
            redisTemplate.opsForValue().set(userKey, "online", PRESENCE_TTL);
            
            log.debug("User {} marked as online", userId);
        } catch (Exception e) {
            log.error("Failed to set user {} online: {}", userId, e.getMessage());
        }
    }

    @Override
    public void setOffline(Long userId) {
        try {
            // Remove from online users set
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
            
            // Delete individual presence key
            String userKey = USER_PRESENCE_PREFIX + userId;
            redisTemplate.delete(userKey);
            
            log.debug("User {} marked as offline", userId);
        } catch (Exception e) {
            log.error("Failed to set user {} offline: {}", userId, e.getMessage());
        }
    }

    @Override
    public boolean isOnline(Long userId) {
        try {
            String userKey = USER_PRESENCE_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(userKey));
        } catch (Exception e) {
            log.error("Failed to check online status for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public Set<Long> getOnlineUsers() {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
            if (members == null) {
                return Collections.emptySet();
            }
            return members.stream()
                    .map(obj -> Long.parseLong(obj.toString()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get online users: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    @Override
    public Set<Long> getOnlineUsers(Set<Long> userIds) {
        try {
            return userIds.stream()
                    .filter(this::isOnline)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to filter online users: {}", e.getMessage());
            return Collections.emptySet();
        }
    }
}
