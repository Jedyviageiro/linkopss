package com.linkops.user.service;

import com.linkops.common.exception.ResourceNotFoundException;
import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ConflictException;
import com.linkops.provider.repository.ProviderProfileRepository;
import com.linkops.user.domain.User;
import com.linkops.user.dto.UpdateUserRequest;
import com.linkops.user.dto.UserResponse;
import com.linkops.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;

    public UserService(
            UserRepository userRepository,
            ProviderProfileRepository providerProfileRepository
    ) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
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

    @Transactional(readOnly = true)
    public Page<UserResponse> listAll(Pageable pageable) {
        return userRepository.findAll(adminPageable(pageable)).map(UserResponse::from);
    }

    @Transactional
    public UserResponse suspend(UUID administratorId, UUID userId) {
        if (administratorId.equals(userId)) {
            throw new BadRequestException("Não pode suspender a sua própria conta administrativa.");
        }
        User user = findUser(userId);
        if (user.getStatus() == com.linkops.user.domain.UserStatus.DEACTIVATED) {
            throw new ConflictException("Uma conta desativada não pode ser suspensa.");
        }
        user.suspend();
        providerProfileRepository.findByUserId(userId).ifPresent(profile -> profile.suspend());
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse reactivate(UUID userId) {
        User user = findUser(userId);
        if (user.getStatus() == com.linkops.user.domain.UserStatus.DEACTIVATED) {
            throw new ConflictException("Uma conta desativada não pode ser reativada.");
        }
        user.reactivate();
        providerProfileRepository.findByUserId(userId)
                .ifPresent(profile -> profile.reactivateAfterUserSuspension());
        return UserResponse.from(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado."));
    }

    private Pageable adminPageable(Pageable pageable) {
        Map<String, String> allowed = Map.of(
                "createdAt", "createdAt",
                "firstName", "firstName",
                "lastName", "lastName",
                "email", "email",
                "role", "role",
                "status", "status"
        );
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    String property = allowed.get(order.getProperty());
                    if (property == null) {
                        throw new BadRequestException(
                                "Ordenação inválida. Use createdAt, firstName, lastName, email, role ou status."
                        );
                    }
                    return new Sort.Order(order.getDirection(), property);
                })
                .toList();
        Sort sort = orders.isEmpty() ? Sort.by(Sort.Order.desc("createdAt")) : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
