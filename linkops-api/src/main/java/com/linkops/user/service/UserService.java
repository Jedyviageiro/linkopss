package com.linkops.user.service;

import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.user.domain.User;
import com.linkops.user.dto.UpdateUserRequest;
import com.linkops.user.dto.UserResponse;
import com.linkops.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public UserResponse updateCurrentUser(UUID userId, UpdateUserRequest request) {
        User user = findUser(userId);
        user.updateProfile(request.firstName(), request.lastName(), request.phone());
        return UserResponse.from(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado."));
    }
}
