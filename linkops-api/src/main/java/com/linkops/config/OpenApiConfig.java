package com.linkops.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI linkOpsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LinkOps API")
                        .description("API REST para ligação entre clientes e prestadores de serviços.")
                        .version("v1")
                        .contact(new Contact().name("Equipa LinkOps"))
                        .license(new License().name("Uso interno")))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Introduza o access token JWT, sem o prefixo Bearer.")
                ));
    }
}
