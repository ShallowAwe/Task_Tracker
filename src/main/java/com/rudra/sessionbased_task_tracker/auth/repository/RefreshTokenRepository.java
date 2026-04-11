package com.rudra.sessionbased_task_tracker.auth.repository;

import com.rudra.sessionbased_task_tracker.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);


    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true " +
            "WHERE r.familyId = :familyId AND r.revoked = false")
    int revokeAllByFamilyId(@Param("familyId") UUID familyId);

    @Modifying
    @Query(
            "UPDATE RefreshToken r SET r.revoked = true " +
                    "WHERE r.userId = :userId AND r.revoked = false"
    )
    int revokeAllByUserId(@Param("userId") Long userId);
}
