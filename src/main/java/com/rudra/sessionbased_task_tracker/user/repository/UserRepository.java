package com.rudra.sessionbased_task_tracker.user.repository;

import com.rudra.sessionbased_task_tracker.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Optional so we can .orElseThrow()

    Optional<User> findByName(String name); // Optional for consistency

    boolean existsByEmail(String email);

}
