package org.example.learnlink.modules.gamification.service;

import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.dto.UserScoreResponse;
import org.example.learnlink.modules.gamification.entity.UserScore;


public interface GamificationService {
    public UserScore addPoints(Long userId, AddPointsRequest request );
    public UserScoreResponse getUserScore(Long userId);

}
