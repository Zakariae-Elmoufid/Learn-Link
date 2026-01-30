package org.example.learnlink.modules.user.repository;

import org.example.learnlink.modules.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository  extends JpaRepository<UserProfile ,Long> {
    Optional<UserProfile> findByUserId(long userId);

}
