package com.rudra.sessionbased_task_tracker.user.service;

import com.rudra.sessionbased_task_tracker.user.dto.UserResponse;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.exception.DuplicateEmailException;
import com.rudra.sessionbased_task_tracker.user.exception.InvalidPasswordException;
import com.rudra.sessionbased_task_tracker.user.exception.UserNotFoundException;
import com.rudra.sessionbased_task_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }


    @Transactional
    public User updateUserById(Long userId, String email, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (name != null && !name.isBlank()) {
            user.setName(name.trim());
        }

        if (email != null && !email.isBlank()) {
            String normalizedEmail = email.trim().toLowerCase();

            if (userRepository.existsByEmailAndUserId(normalizedEmail, userId)) {
                throw new DuplicateEmailException("Email already in use");
            }

            user.setEmail(normalizedEmail);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public boolean checkIfUserExists(String email) {
        if (email == null)
            return false;
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Page SERVICE

    public Page<UserResponse> listAll(Pageable pageable){
        return userRepository.findAll(pageable).map(UserResponse:: from);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("New password must be different from old password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

}