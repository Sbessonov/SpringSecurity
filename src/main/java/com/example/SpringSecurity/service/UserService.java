package com.example.SpringSecurity.service;

import com.example.SpringSecurity.entity.UserEntity;
import com.example.SpringSecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByName(username);
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    public void resetFailedAttempts(String username) {
        userRepository.findByName(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            userRepository.save(user);
        });
    }

    public void incrementFailedAttempts(String username) {
        userRepository.findByName(username).ifPresent(user -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= 5) {
                user.setIsLocked(true);
            }
            userRepository.save(user);
        });
    }

    public void unlockAccount(String username) {
        userRepository.findByName(username).ifPresent(user -> {
            user.setIsLocked(false);
            user.setFailedAttempts(0);
            userRepository.save(user);
        });
    }

}
