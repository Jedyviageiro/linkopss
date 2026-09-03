package com.linkops.auth.service;

import com.linkops.auth.dto.AuthResponse;
import com.linkops.auth.dto.ForgotPasswordRequest;
import com.linkops.auth.dto.LoginRequest;
import com.linkops.auth.dto.MessageResponse;
import com.linkops.auth.dto.RefreshTokenRequest;
import com.linkops.auth.dto.RegisterRequest;
import com.linkops.auth.dto.ResetPasswordRequest;
import com.linkops.auth.domain.PasswordResetToken;
import com.linkops.auth.repository.PasswordResetTokenRepository;
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
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final Duration passwordResetExpiration;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordResetEmailService passwordResetEmailService,
            @Value("${linkops.security.password-reset.expiration}") Duration passwordResetExpiration
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetEmailService = passwordResetEmailService;
        if (passwordResetExpiration.isNegative() || passwordResetExpiration.isZero()) {
            throw new IllegalArgumentException("A expiração do token de recuperação deve ser positiva.");
        }
        this.passwordResetExpiration = passwordResetExpiration;
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

    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Sessão inválida."));
        user.invalidateTokens();
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCaseForUpdate(normalizeEmail(request.email())).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserIdAndUsedAtIsNull(user.getId());

            String rawToken = generateResetToken();
            passwordResetTokenRepository.saveAndFlush(new PasswordResetToken(
                    user,
                    hashToken(rawToken),
                    Instant.now().plus(passwordResetExpiration)
            ));
            passwordResetEmailService.send(user.getEmail(), rawToken);
        });

        return new MessageResponse(
                "Se existir uma conta associada a este e-mail, enviaremos instruções para redefinir a palavra-passe."
        );
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNull(hashToken(request.token()))
                .orElseThrow(() -> new BadRequestException("O token de recuperação é inválido ou expirou."));

        Instant now = Instant.now();
        if (token.isExpired(now)) {
            token.markAsUsed(now);
            throw new BadRequestException("O token de recuperação é inválido ou expirou.");
        }

        if (passwordEncoder.matches(request.password(), token.getUser().getPasswordHash())) {
            throw new BadRequestException("A nova palavra-passe deve ser diferente da atual.");
        }

        token.getUser().changePasswordHash(passwordEncoder.encode(request.password()));
        token.markAsUsed(now);

        return new MessageResponse("A palavra-passe foi redefinida com sucesso.");
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

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }
}
