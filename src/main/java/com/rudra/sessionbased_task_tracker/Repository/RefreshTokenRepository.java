package com.rudra.sessionbased_task_tracker.Repository;

import com.rudra.sessionbased_task_tracker.Domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String token);
}
