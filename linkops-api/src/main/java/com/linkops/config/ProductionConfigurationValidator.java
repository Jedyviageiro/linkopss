package com.linkops.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionConfigurationValidator {

    private static final String DEVELOPMENT_JWT_SECRET =
            "linkops-development-secret-change-me-32-bytes";

    public ProductionConfigurationValidator(
            @Value("${spring.datasource.url}") String databaseUrl,
            @Value("${spring.datasource.username}") String databaseUsername,
            @Value("${spring.datasource.password}") String databasePassword,
            @Value("${linkops.security.jwt.secret}") String jwtSecret,
            @Value("${linkops.media.cloudinary.cloud-name}") String cloudName,
            @Value("${linkops.media.cloudinary.api-key}") String cloudinaryApiKey,
            @Value("${linkops.media.cloudinary.api-secret}") String cloudinaryApiSecret
    ) {
        requirePostgresqlUrl(databaseUrl);
        requireText("DATABASE_USERNAME", databaseUsername);
        requireText("DATABASE_PASSWORD", databasePassword);
        requireText("JWT_SECRET", jwtSecret);
        if (DEVELOPMENT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException(
                    "JWT_SECRET de desenvolvimento não pode ser usado em produção."
            );
        }
        requireText("CLOUDINARY_CLOUD_NAME", cloudName);
        requireText("CLOUDINARY_API_KEY", cloudinaryApiKey);
        requireText("CLOUDINARY_API_SECRET", cloudinaryApiSecret);
    }

    private void requirePostgresqlUrl(String databaseUrl) {
        requireText("DATABASE_URL", databaseUrl);
        if (!databaseUrl.startsWith("jdbc:postgresql://")) {
            throw new IllegalStateException(
                    "DATABASE_URL deve utilizar o formato jdbc:postgresql:// em produção."
            );
        }
    }

    private void requireText(String variable, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    variable + " é obrigatória no perfil de produção."
            );
        }
    }
}
