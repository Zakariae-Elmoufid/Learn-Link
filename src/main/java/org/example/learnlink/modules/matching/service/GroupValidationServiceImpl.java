package org.example.learnlink.modules.matching.service;

import lombok.RequiredArgsConstructor;
import org.example.learnlink.common.service.GroupValidationService;
import org.example.learnlink.modules.matching.repository.GroupMembershipRepository;
import org.example.learnlink.modules.matching.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of GroupValidationService.
 * Provides group validation functionality for other modules.
 */
@Service
@RequiredArgsConstructor
public class GroupValidationServiceImpl implements GroupValidationService {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository membershipRepository;

    @Override
    public boolean groupExists(Long groupId) {
        return studyGroupRepository.existsById(groupId);
    }

    @Override
    public boolean isActiveMember(Long groupId, Long userId) {
        return membershipRepository.findActiveMembership(groupId, userId).isPresent();
    }

    @Override
    public List<Long> getActiveMemberIds(Long groupId) {
        return membershipRepository.findActiveMemberUserIds(groupId);
    }
}
