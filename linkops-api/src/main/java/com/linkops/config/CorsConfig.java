package com.linkops.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${linkops.cors.allowed-origins}") List<String> allowedOrigins
    ) {
        List<String> safeOrigins = allowedOrigins.stream()
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
        if (safeOrigins.isEmpty()) {
            throw new IllegalArgumentException("Configure pelo menos uma origem CORS permitida.");
        }
        if (safeOrigins.stream().anyMatch(origin -> origin.equals("*"))) {
            throw new IllegalArgumentException("Não é permitido utilizar '*' nas origens CORS.");
        }
        safeOrigins.forEach(this::validateOrigin);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(safeOrigins);
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.ORIGIN,
                "X-Requested-With"
        ));
        configuration.setExposedHeaders(List.of(HttpHeaders.LOCATION));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void validateOrigin(String origin) {
        try {
            URI uri = new URI(origin);
            boolean http = "http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme());
            boolean hasInvalidComponents = uri.getHost() == null
                    || uri.getUserInfo() != null
                    || uri.getQuery() != null
                    || uri.getFragment() != null
                    || (uri.getPath() != null && !uri.getPath().isEmpty());
            if (!http || hasInvalidComponents) {
                throw new IllegalArgumentException(
                        "Origem CORS inválida: " + origin
                );
            }
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Origem CORS inválida: " + origin, exception);
        }
    }
}
