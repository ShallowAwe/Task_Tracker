package com.rudra.sessionbased_task_tracker.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_token", columnList = "token", unique = true)
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_token_user_family", columnNames = {"user_id", "family_id"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "family_id", nullable = false)
    private java.util.UUID familyId;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;
}