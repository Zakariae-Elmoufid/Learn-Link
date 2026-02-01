package org.example.learnlink.modules.matching.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.matching.event.JoinRequestEvent;
import org.example.learnlink.modules.matching.event.MemberJoinedGroupEvent;
import org.example.learnlink.modules.matching.event.MemberLeftGroupEvent;
import org.example.learnlink.modules.matching.event.StudyGroupCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for study group events.
 * Handles notifications and integrations when group events occur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StudyGroupEventListener {

    /**
     * Handle study group creation.
     * Could trigger: gamification points, activity feed update
     */
    @Async
    @EventListener
    public void handleStudyGroupCreated(StudyGroupCreatedEvent event) {
        log.info("Study group created: {} (ID: {}) by owner {}",
                event.getGroupName(), event.getGroupId(), event.getOwnerId());

        // TODO: Award points for creating a group
        // TODO: Add to activity feed
    }

    /**
     * Handle member joining a group.
     * Could trigger: notification to owner/admins, activity feed update
     */
    @Async
    @EventListener
    public void handleMemberJoined(MemberJoinedGroupEvent event) {
        log.info("User {} joined group {} ({})",
                event.getUserId(), event.getGroupName(), event.isDirectJoin() ? "direct" : "approved");

        // TODO: Notify group owner/admins
        // TODO: Award points for joining a group
        // TODO: Add to activity feed
    }

    /**
     * Handle member leaving a group.
     * Could trigger: notification to owner
     */
    @Async
    @EventListener
    public void handleMemberLeft(MemberLeftGroupEvent event) {
        if (event.isVoluntary()) {
            log.info("User {} left group {}", event.getUserId(), event.getGroupName());
        } else {
            log.info("User {} was removed from group {} by admin {}",
                    event.getUserId(), event.getGroupName(), event.getRemovedByUserId());

            // TODO: Notify removed user
        }
    }

    /**
     * Handle join request for private group.
     * Could trigger: notification to owner/admins
     */
    @Async
    @EventListener
    public void handleJoinRequest(JoinRequestEvent event) {
        log.info("User {} requested to join group {}", event.getRequesterId(), event.getGroupName());

        // TODO: Notify group owner/admins about pending request
    }
}
