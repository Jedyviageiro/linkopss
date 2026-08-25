package com.linkops.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigurationValidatorTests {

    private static final String JWT_SECRET =
            "production-secret-with-at-least-thirty-two-bytes";

    @Test
    void shouldAcceptCompleteProductionConfiguration() {
        assertThatCode(() -> validator(
                "jdbc:postgresql://database:5432/linkops",
                "linkops",
                "database-secret",
                JWT_SECRET,
                "linkops-cloud",
                "cloud-key",
                "cloud-secret"
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUnsafeDatabaseOrMediaConfiguration() {
        assertThatThrownBy(() -> validator(
                "postgres://database/linkops",
                "linkops",
                "database-secret",
                JWT_SECRET,
                "linkops-cloud",
                "cloud-key",
                "cloud-secret"
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jdbc:postgresql://");

        assertThatThrownBy(() -> validator(
                "jdbc:postgresql://database:5432/linkops",
                "linkops",
                "database-secret",
                JWT_SECRET,
                " ",
                "cloud-key",
                "cloud-secret"
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOUDINARY_CLOUD_NAME");
    }

    private ProductionConfigurationValidator validator(
            String databaseUrl,
            String databaseUsername,
            String databasePassword,
            String jwtSecret,
            String cloudName,
            String cloudinaryApiKey,
            String cloudinaryApiSecret
    ) {
        return new ProductionConfigurationValidator(
                databaseUrl,
                databaseUsername,
                databasePassword,
                jwtSecret,
                cloudName,
                cloudinaryApiKey,
                cloudinaryApiSecret
        );
    }
}
