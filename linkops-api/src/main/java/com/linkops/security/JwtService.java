package com.linkops.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final byte[] secret;
    private final String issuer;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtService(
            @Value("${linkops.security.jwt.secret}") String secret,
            @Value("${linkops.security.jwt.issuer}") String issuer,
            @Value("${linkops.security.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${linkops.security.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET é obrigatório.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve ter pelo menos 32 bytes.");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("O emissor dos tokens JWT é obrigatório.");
        }
        if (accessTokenExpiration <= 0) {
            throw new IllegalArgumentException("A expiração do access token deve ser positiva.");
        }
        if (refreshTokenExpiration <= accessTokenExpiration) {
            throw new IllegalArgumentException(
                    "A expiração do refresh token deve ser superior à do access token."
            );
        }
        this.issuer = issuer.trim();
        this.accessTokenExpiration = Duration.ofMillis(accessTokenExpiration);
        this.refreshTokenExpiration = Duration.ofMillis(refreshTokenExpiration);
    }

    public String generateAccessToken(AuthenticatedUser user) {
        return generateToken(user, ACCESS_TOKEN_TYPE, accessTokenExpiration);
    }

    public String generateRefreshToken(AuthenticatedUser user) {
        return generateToken(user, REFRESH_TOKEN_TYPE, refreshTokenExpiration);
    }

    public boolean isAccessTokenValid(String token, AuthenticatedUser user) {
        JWTClaimsSet claims = validate(token, ACCESS_TOKEN_TYPE);
        return user.id().toString().equals(claims.getSubject())
                && user.email().equalsIgnoreCase(stringClaim(claims, "email"))
                && user.isEnabled()
                && user.isAccountNonLocked();
    }

    public boolean isRefreshTokenValid(String token, AuthenticatedUser user) {
        JWTClaimsSet claims = validate(token, REFRESH_TOKEN_TYPE);
        return user.id().toString().equals(claims.getSubject())
                && user.email().equalsIgnoreCase(stringClaim(claims, "email"))
                && user.isEnabled()
                && user.isAccountNonLocked();
    }

    public String extractEmailFromAccessToken(String token) {
        return stringClaim(validate(token, ACCESS_TOKEN_TYPE), "email");
    }

    public String extractEmailFromRefreshToken(String token) {
        return stringClaim(validate(token, REFRESH_TOKEN_TYPE), "email");
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpiration.toSeconds();
    }

    private String generateToken(
            AuthenticatedUser user,
            String tokenType,
            Duration expiration
    ) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(user.id().toString())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(expiration)))
                .claim("email", user.email())
                .claim("role", user.role().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Não foi possível gerar o token de autenticação.", exception);
        }
    }

    private JWTClaimsSet validate(String token, String expectedType) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())
                    || !jwt.verify(new MACVerifier(secret))) {
                throw invalidToken(null);
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Instant now = Instant.now();
            if (!issuer.equals(claims.getIssuer())
                    || claims.getSubject() == null
                    || claims.getIssueTime() == null
                    || claims.getIssueTime().toInstant().isAfter(now.plusSeconds(30))
                    || claims.getExpirationTime() == null
                    || !claims.getExpirationTime().toInstant().isAfter(now)
                    || !claims.getExpirationTime().after(claims.getIssueTime())
                    || !expectedType.equals(claims.getStringClaim(TOKEN_TYPE_CLAIM))) {
                throw invalidToken(null);
            }
            return claims;
        } catch (ParseException | JOSEException exception) {
            throw invalidToken(exception);
        }
    }

    private String stringClaim(JWTClaimsSet claims, String claim) {
        try {
            return claims.getStringClaim(claim);
        } catch (ParseException exception) {
            throw invalidToken(exception);
        }
    }

    private BadCredentialsException invalidToken(Exception cause) {
        return new BadCredentialsException("Token inválido ou expirado.", cause);
    }
}
