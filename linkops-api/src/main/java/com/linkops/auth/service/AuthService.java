package com.linkops.auth.service;

import com.linkops.auth.dto.AuthResponse;
import com.linkops.auth.dto.LoginRequest;
import com.linkops.auth.dto.RefreshTokenRequest;
import com.linkops.auth.dto.RegisterRequest;
import com.linkops.common.exception.BadRequestException;
import com.linkops.common.exception.ConflictException;
import com.linkops.security.AuthenticatedUser;
import com.linkops.security.CustomUserDetailsService;
import com.linkops.security.JwtService;
import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import com.linkops.user.dto.UserResponse;
import com.linkops.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == UserRole.ADMIN) {
            throw new BadRequestException("Não é permitido criar uma conta de administrador por este endpoint.");
        }

        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Já existe uma conta associada a este e-mail.");
        }

        User user = new User(
                request.firstName(),
                request.lastName(),
                email,
                request.phone(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return createAuthResponse(AuthenticatedUser.from(savedUser), savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Já existe uma conta associada a este e-mail.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalizeEmail(request.email()),
                        request.password()
                )
        );

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        User user = userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        return createAuthResponse(authenticatedUser, user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String email = jwtService.extractEmailFromRefreshToken(request.refreshToken());
        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) userDetailsService.loadUserByUsername(email);

        if (!jwtService.isRefreshTokenValid(request.refreshToken(), authenticatedUser)) {
            throw new BadCredentialsException("Token inválido ou expirado.");
        }

        User user = userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new BadCredentialsException("Token inválido ou expirado."));
        return createAuthResponse(authenticatedUser, user);
    }

    private AuthResponse createAuthResponse(AuthenticatedUser authenticatedUser, User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(authenticatedUser),
                jwtService.generateRefreshToken(authenticatedUser),
                "Bearer",
                jwtService.getAccessTokenExpiresInSeconds(),
                UserResponse.from(user)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
