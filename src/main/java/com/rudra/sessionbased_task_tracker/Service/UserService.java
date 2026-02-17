package com.rudra.sessionbased_task_tracker.Service;

import com.rudra.sessionbased_task_tracker.Domain.User;
import com.rudra.sessionbased_task_tracker.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public boolean checkIfUserExists(String email) {
        if (email == null) return false;
        return userRepository.existsByEmail(email);
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}